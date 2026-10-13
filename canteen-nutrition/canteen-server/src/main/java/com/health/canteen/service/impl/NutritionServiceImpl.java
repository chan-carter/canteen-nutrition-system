package com.health.canteen.service.impl;

import com.health.canteen.vo.NutritionAnalysisVO;
import com.health.canteen.ai.OpenAiNutritionAnalyzer;
import com.health.canteen.ai.RuleBasedNutritionAnalyzer;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.AiCallLogMapper;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.NutritionMapper;
import com.health.canteen.context.*;
import com.health.canteen.dto.*;
import com.health.canteen.entity.*;
import com.health.canteen.result.*;
import com.health.canteen.vo.*;
import com.health.canteen.service.NutritionService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.health.canteen.utils.JsonUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.health.canteen.constant.RedisConstant;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI营养分析：优先调用大模型，失败或超时时降级为内置规则估算，保证基础功能可用
 */
@Slf4j
@Service
public class NutritionServiceImpl implements NutritionService {

    private static final String CALL_TYPE_NUTRITION = "NUTRITION";

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private NutritionMapper nutritionMapper;

    @Autowired
    private AiCallLogMapper aiCallLogMapper;

    @Autowired
    private OpenAiNutritionAnalyzer openAiNutritionAnalyzer;

    @Autowired
    private RuleBasedNutritionAnalyzer ruleBasedNutritionAnalyzer;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @CacheEvict(cacheNames = RedisConstant.NUTRITION_CACHE, key = "#dishId")
    @Override
    public Nutrition analyze(Integer dishId, NutritionAnalyzeDTO analyzeParam) {
        Dish dish = dishMapper.getDishById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        CurrentUser.checkMerchantOwner(dish.getMerchantId());

        String dishName = analyzeParam != null && notBlank(analyzeParam.getDishName())
                ? analyzeParam.getDishName() : dish.getName();
        List<String> ingredients = resolveIngredients(analyzeParam, dish);
        if (ingredients.isEmpty()) {
            throw new BusinessException("请先完善菜品的主要食材信息，再进行营养分析");
        }
        String servingSize = analyzeParam == null ? null : analyzeParam.getServingSize();

        //同一道菜的同一组食材不重复调用大模型，避免产生额外费用
        String cacheKey = aiCacheKey(dishId, dishName, ingredients, servingSize);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        NutritionAnalysisVO result;
        if (cached != null) {
            result = JsonUtils.parseJson(cached, NutritionAnalysisVO.class);
            log.info("命中AI分析缓存, 菜品: {}", dishName);
        } else {
            result = openAiNutritionAnalyzer.analyze(dishName, ingredients, servingSize);
            writeLog(dishId, result.getModelName(), result.isSuccess(), result.getCostMs(),
                    result.getErrorMsg(), result.getRawContent());
            if (result.isSuccess()) {
                stringRedisTemplate.opsForValue().set(cacheKey, JsonUtils.toJson(result), RedisConstant.AI_ANALYZE_TTL);
            }
        }

        if (!result.isSuccess()) {
            log.warn("AI分析未成功，启用内置规则估算, 菜品: {}, 原因: {}", dishName, result.getErrorMsg());
            NutritionAnalysisVO fallback = ruleBasedNutritionAnalyzer.analyze(dishName, ingredients, servingSize);
            writeLog(dishId, fallback.getModelName(), fallback.isSuccess(), fallback.getCostMs(),
                    fallback.getErrorMsg(), null);
            if (!fallback.isSuccess()) {
                throw new BusinessException("营养分析失败：" + fallback.getErrorMsg());
            }
            result = fallback;
        }

        Nutrition nutrition = toNutrition(dishId, result);
        nutritionMapper.saveOrUpdate(nutrition);
        log.info("营养分析结果已保存, 菜品: {}, 数据来源: {}", dishName, nutrition.getSource());
        return nutritionMapper.getByDishId(dishId);
    }

    @Cacheable(cacheNames = RedisConstant.NUTRITION_CACHE, key = "#dishId")
    @Override
    public Nutrition getByDishId(Integer dishId) {
        if (dishMapper.getDishById(dishId) == null) {
            throw new BusinessException("菜品不存在");
        }
        Nutrition nutrition = nutritionMapper.getByDishId(dishId);
        if (nutrition == null) {
            throw new BusinessException("该菜品暂无营养分析数据，请联系食堂商户发起营养分析");
        }
        return nutrition;
    }

    /**
     * 优先使用请求中提交的食材，否则按顿号/逗号切分菜品已有食材字段
     */
    private List<String> resolveIngredients(NutritionAnalyzeDTO param, Dish dish) {
        if (param != null && param.getIngredients() != null && !param.getIngredients().isEmpty()) {
            List<String> list = new ArrayList<>();
            for (String item : param.getIngredients()) {
                if (notBlank(item)) {
                    list.add(item.trim());
                }
            }
            return list;
        }
        if (!notBlank(dish.getIngredients())) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        for (String item : Arrays.asList(dish.getIngredients().split("[、，,;；/]"))) {
            if (notBlank(item)) {
                list.add(item.trim());
            }
        }
        return list;
    }

    private Nutrition toNutrition(Integer dishId, NutritionAnalysisVO result) {
        Nutrition nutrition = new Nutrition();
        nutrition.setDishId(dishId);
        nutrition.setCalorie(result.getCalorie());
        nutrition.setProtein(result.getProtein());
        nutrition.setFat(result.getFat());
        nutrition.setCarbohydrate(result.getCarbohydrate());
        nutrition.setFiber(result.getFiber());
        nutrition.setSodium(result.getSodium());
        nutrition.setAnalysisText(result.getAnalysisText());
        nutrition.setSource(result.getSource());
        nutrition.setModelName(result.getModelName());
        nutrition.setCreateTime(LocalDateTime.now());
        nutrition.setUpdateTime(LocalDateTime.now());
        return nutrition;
    }

    private void writeLog(Integer dishId, String modelName, boolean success, Long costMs, String errorMsg, String rawContent) {
        AiCallLog callLog = new AiCallLog();
        callLog.setDishId(dishId);
        callLog.setCallType(CALL_TYPE_NUTRITION);
        callLog.setModelName(modelName);
        callLog.setSuccess(success ? 1 : 0);
        callLog.setCostMs(costMs);
        callLog.setErrorMsg(errorMsg);
        callLog.setRawContent(rawContent);
        callLog.setCreateTime(LocalDateTime.now());
        try {
            aiCallLogMapper.addLog(callLog);
        } catch (Exception e) {
            log.error("写入AI调用日志失败", e);
        }
    }

    private boolean notBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }


    /**
     * 缓存键同时包含菜品ID与分析入参指纹，保证商户改写食材后不会命中旧结果
     */
    private String aiCacheKey(Integer dishId, String dishName, List<String> ingredients, String servingSize) {
        String raw = dishName + "|" + String.join(",", ingredients) + "|" + (servingSize == null ? "" : servingSize);
        return RedisConstant.AI_ANALYZE_KEY + dishId + ":" + digest16(raw);
    }

    private String digest16(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 16);
        } catch (Exception e) {
            throw new IllegalStateException("JDK不支持SHA-256摘要算法", e);
        }
    }

}
