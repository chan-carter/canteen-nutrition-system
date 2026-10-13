package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DishCategoryMapper;
import com.health.canteen.mapper.DishMapper;
import com.health.canteen.mapper.NutritionMapper;
import com.health.canteen.mapper.StallMapper;
import com.health.canteen.entity.Dish;
import com.health.canteen.dto.DishPageQueryDTO;
import com.health.canteen.result.PageResult;
import com.health.canteen.entity.Stall;
import com.health.canteen.service.DishService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.health.canteen.constant.RedisConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    private static final int AUDIT_WAIT = 0;
    private static final int AUDIT_PASS = 1;
    private static final int AUDIT_REJECT = 2;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private StallMapper stallMapper;

    @Autowired
    private DishCategoryMapper dishCategoryMapper;

    @Autowired
    private NutritionMapper nutritionMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResult<Dish> getDishPage(DishPageQueryDTO dishQueryParam) {
        if (CurrentUser.isStudent()) {
            //学生端只展示审核通过且已上架的菜品
            dishQueryParam.setStatus(1);
            dishQueryParam.setAuditStatus(AUDIT_PASS);
        } else if (CurrentUser.isMerchant()) {
            dishQueryParam.setMerchantId(CurrentUser.merchantId());
        }
        PageHelper.startPage(dishQueryParam.getPage(), dishQueryParam.getPageSize());
        List<Dish> dishes = dishMapper.getDishPage(dishQueryParam);
        Page<Dish> page = (Page<Dish>) dishes;
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Cacheable(cacheNames = RedisConstant.DISH_CACHE,
            key = "T(com.health.canteen.utils.CurrentUser).role() + ':' "
                    + "+ T(com.health.canteen.utils.CurrentUser).cacheScope() + ':' + #id")
    @Override
    public Dish getDishById(Integer id) {
        Dish dish = dishMapper.getDishById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        if (CurrentUser.isStudent() && (dish.getStatus() == null || dish.getStatus() != 1
                || !Integer.valueOf(AUDIT_PASS).equals(dish.getAuditStatus()))) {
            throw new BusinessException("菜品已下架或正在审核中");
        }
        if (CurrentUser.isMerchant()) {
            CurrentUser.checkMerchantOwner(dish.getMerchantId());
        }
        return dish;
    }

    @CacheEvict(cacheNames = {RedisConstant.DISH_CACHE, RedisConstant.RECOMMEND_CACHE, RedisConstant.CATEGORY_CACHE}, allEntries = true)
    @Override
    public void addDish(Dish dish) {
        if (isBlank(dish.getName())) {
            throw new BusinessException("菜品名称不能为空");
        }
        if (isBlank(dish.getIngredients())) {
            throw new BusinessException("请填写主要食材，AI营养分析需要依赖食材信息");
        }
        if (dish.getPrice() == null || dish.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("菜品价格必须大于0");
        }
        Stall stall = requireStall(dish.getStallId());
        if (dishCategoryMapper.getCategoryById(dish.getCategoryId()) == null) {
            throw new BusinessException("菜品分类不存在");
        }
        if (CurrentUser.isMerchant()) {
            CurrentUser.checkMerchantOwner(stall.getMerchantId());
            //商户新增的菜品需要管理员审核后才会对学生端展示
            dish.setAuditStatus(AUDIT_WAIT);
        } else if (dish.getAuditStatus() == null) {
            dish.setAuditStatus(AUDIT_PASS);
        }
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        dish.setSales(0);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.addDish(dish);
        log.info("新增菜品: {}, 所属档口: {}", dish.getName(), dish.getStallId());
    }

    @CacheEvict(cacheNames = {RedisConstant.DISH_CACHE, RedisConstant.RECOMMEND_CACHE, RedisConstant.NUTRITION_CACHE}, allEntries = true)
    @Override
    public void updateDish(Dish dish) {
        if (dish.getId() == null) {
            throw new BusinessException("菜品ID不能为空");
        }
        Dish old = dishMapper.getDishById(dish.getId());
        if (old == null) {
            throw new BusinessException("菜品不存在");
        }
        CurrentUser.checkMerchantOwner(old.getMerchantId());
        if (dish.getStallId() != null && !dish.getStallId().equals(old.getStallId())) {
            Stall stall = requireStall(dish.getStallId());
            CurrentUser.checkMerchantOwner(stall.getMerchantId());
        }
        if (dish.getPrice() != null && dish.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("菜品价格必须大于0");
        }
        if (CurrentUser.isMerchant()) {
            //商户改菜品不变更审核结果，由管理员统一审核
            dish.setAuditStatus(null);
        }
        if (dish.getIngredients() != null && !dish.getIngredients().equals(old.getIngredients())) {
            log.warn("菜品食材发生变化，建议重新发起营养分析, 菜品ID: {}", dish.getId());
        }
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.updateDish(dish);
        cleanAiAnalyzeCache(dish.getId());
    }

    @CacheEvict(cacheNames = {RedisConstant.DISH_CACHE, RedisConstant.RECOMMEND_CACHE}, allEntries = true)
    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("上架状态取值不正确");
        }
        Dish dish = dishMapper.getDishById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        CurrentUser.checkMerchantOwner(dish.getMerchantId());
        dishMapper.updateDishStatus(id, status, LocalDateTime.now());
    }

    @CacheEvict(cacheNames = {RedisConstant.DISH_CACHE, RedisConstant.RECOMMEND_CACHE}, allEntries = true)
    @Override
    public void audit(Integer id, Integer auditStatus, String auditRemark) {
        if (auditStatus == null || (auditStatus != AUDIT_PASS && auditStatus != AUDIT_REJECT)) {
            throw new BusinessException("审核结果只能是通过或驳回");
        }
        if (dishMapper.getDishById(id) == null) {
            throw new BusinessException("菜品不存在");
        }
        dishMapper.updateDishAudit(id, auditStatus, auditRemark, LocalDateTime.now());
        log.info("菜品审核完成, ID: {}, 结果: {}", id, auditStatus == AUDIT_PASS ? "通过" : "驳回");
    }

    @CacheEvict(cacheNames = {RedisConstant.DISH_CACHE, RedisConstant.RECOMMEND_CACHE, RedisConstant.NUTRITION_CACHE}, allEntries = true)
    @Override
    @Transactional
    public void deleteDish(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的菜品");
        }
        for (Integer id : ids) {
            Dish dish = dishMapper.getDishById(id);
            if (dish == null) {
                throw new BusinessException("菜品不存在, ID: " + id);
            }
            CurrentUser.checkMerchantOwner(dish.getMerchantId());
        }
        dishMapper.deleteDish(ids);
        ids.forEach(this::cleanAiAnalyzeCache);
        nutritionMapper.deleteByDishIds(ids);
    }

    private Stall requireStall(Integer stallId) {
        if (stallId == null) {
            throw new BusinessException("请选择所属档口");
        }
        Stall stall = stallMapper.getStallById(stallId);
        if (stall == null) {
            throw new BusinessException("所属档口不存在");
        }
        return stall;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }


    /**
     * 菜品食材或名称变更后，按前缀清理AI分析结果缓存。
     * 缓存键带有分析入参指纹，无法枚举具体键名，只能按模式匹配删除。
     */
    private void cleanAiAnalyzeCache(Integer dishId) {
        if (dishId == null) {
            return;
        }
        Set<String> keys = stringRedisTemplate.keys(RedisConstant.AI_ANALYZE_KEY + dishId + ":*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.info("清理菜品AI分析缓存, dishId: {}, 键数量: {}", dishId, keys.size());
        }
    }

}
