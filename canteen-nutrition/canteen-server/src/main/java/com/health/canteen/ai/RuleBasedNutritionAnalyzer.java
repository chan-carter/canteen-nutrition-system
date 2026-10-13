package com.health.canteen.ai;

import com.health.canteen.vo.NutritionAnalysisVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内置规则估算器：AI接口不可用时的降级方案
 * 依据常见食材每100克的可食部营养估算值与假定用量累加，结果为估算数据
 */
@Slf4j
@Component
public class RuleBasedNutritionAnalyzer implements NutritionAnalyzer {

    private static final String RULE_NAME = "rule-based-v1";

    /**
     * key：食材关键词，value：每100克 {热量kcal, 蛋白质g, 脂肪g, 碳水g, 膳食纤维g, 钠mg}
     */
    private static final Map<String, double[]> FOOD_TABLE = new LinkedHashMap<>();

    private static final String[] STAPLE_KEYS = {"面条", "米饭", "大米", "馍", "面包", "馒头", "燕麦", "藜麦", "土豆", "南瓜", "藕片", "全麦"};
    private static final String[] MEAT_EGG_KEYS = {"鸡胸肉", "鸡腿肉", "鸡肉", "牛肉", "猪肉", "五花肉", "午餐肉", "瘦肉", "虾", "鱼", "鸡蛋", "皮蛋", "豆腐"};
    private static final String[] OIL_KEYS = {"食用油", "橄榄油", "香油", "色拉油", "菜籽油"};
    private static final String[] SAUCE_KEYS = {"生抽", "老抽", "酱油", "蚝油"};

    static {
        FOOD_TABLE.put("番茄", new double[]{18, 0.9, 0.2, 4.0, 1.2, 5});
        FOOD_TABLE.put("鸡蛋", new double[]{143, 12.6, 8.8, 1.5, 0, 132});
        FOOD_TABLE.put("皮蛋", new double[]{171, 13.2, 10.7, 3.0, 0, 606});
        FOOD_TABLE.put("油麦菜", new double[]{15, 1.8, 0.6, 2.1, 1.4, 60});
        FOOD_TABLE.put("生菜", new double[]{15, 1.4, 0.4, 2.1, 1.1, 38});
        FOOD_TABLE.put("青菜", new double[]{25, 1.5, 0.3, 4.0, 1.6, 70});
        FOOD_TABLE.put("五花肉", new double[]{508, 14.6, 47.0, 1.5, 0, 70});
        FOOD_TABLE.put("猪肉", new double[]{290, 20.0, 22.0, 1.5, 0, 65});
        FOOD_TABLE.put("瘦肉", new double[]{143, 20.2, 6.2, 1.2, 0, 55});
        FOOD_TABLE.put("鸡胸肉", new double[]{133, 31.0, 1.2, 0, 0, 65});
        FOOD_TABLE.put("鸡肉", new double[]{167, 20.3, 9.9, 0, 0, 63});
        FOOD_TABLE.put("鸡腿肉", new double[]{181, 20.2, 11.0, 0, 0, 65});
        FOOD_TABLE.put("牛肉", new double[]{125, 19.9, 4.2, 0, 0, 84});
        FOOD_TABLE.put("虾", new double[]{93, 18.6, 0.8, 0.7, 0, 165});
        FOOD_TABLE.put("鱼", new double[]{104, 18.0, 3.0, 0, 0, 80});
        FOOD_TABLE.put("豆腐", new double[]{80, 8.1, 3.7, 2.7, 0.4, 60});
        FOOD_TABLE.put("花生", new double[]{574, 24.8, 49.2, 16.3, 5.5, 12});
        FOOD_TABLE.put("午餐肉", new double[]{220, 11.0, 17.0, 6.0, 0.5, 650});
        FOOD_TABLE.put("面条", new double[]{110, 3.5, 0.8, 21.0, 0.8, 60});
        FOOD_TABLE.put("大米", new double[]{116, 2.6, 0.3, 25.9, 0.3, 3});
        FOOD_TABLE.put("米饭", new double[]{116, 2.6, 0.3, 25.9, 0.3, 3});
        FOOD_TABLE.put("馍", new double[]{220, 7.0, 1.5, 44.0, 1.5, 380});
        FOOD_TABLE.put("面包", new double[]{250, 8.0, 3.5, 45.0, 2.0, 400});
        FOOD_TABLE.put("全麦", new double[]{246, 10.0, 3.0, 41.0, 7.0, 230});
        FOOD_TABLE.put("燕麦", new double[]{377, 15.0, 6.7, 61.0, 5.3, 3});
        FOOD_TABLE.put("藜麦", new double[]{368, 14.1, 6.1, 64.2, 7.0, 5});
        FOOD_TABLE.put("土豆", new double[]{77, 2.0, 0.1, 17.2, 0.7, 3});
        FOOD_TABLE.put("南瓜", new double[]{23, 0.7, 0.1, 5.3, 0.5, 1});
        FOOD_TABLE.put("藕片", new double[]{73, 1.0, 0.2, 16.4, 1.2, 4});
        FOOD_TABLE.put("小米", new double[]{361, 9.0, 3.1, 72.8, 1.6, 4});
        FOOD_TABLE.put("牛油果", new double[]{161, 2.0, 15.3, 7.4, 6.7, 7});
        FOOD_TABLE.put("圣女果", new double[]{25, 1.0, 0.2, 5.8, 1.2, 5});
        FOOD_TABLE.put("蓝莓", new double[]{57, 0.5, 0.3, 14.5, 1.4, 1});
        FOOD_TABLE.put("草莓", new double[]{32, 1.0, 0.3, 7.7, 2.0, 2});
        FOOD_TABLE.put("西瓜", new double[]{31, 0.5, 0.3, 7.6, 0.2, 3});
        FOOD_TABLE.put("紫菜", new double[]{250, 26.7, 1.1, 44.1, 21.6, 715});
        FOOD_TABLE.put("香菇", new double[]{26, 1.0, 0.3, 5.2, 3.3, 3});
        FOOD_TABLE.put("酸奶", new double[]{62, 2.5, 2.7, 7.5, 0, 40});
        FOOD_TABLE.put("干辣椒", new double[]{314, 13.0, 27.0, 52.0, 35.0, 60});
        FOOD_TABLE.put("花椒", new double[]{258, 7.0, 8.0, 46.0, 22.0, 5});
        FOOD_TABLE.put("无糖酸奶", new double[]{60, 3.2, 2.0, 6.0, 0, 42});
        FOOD_TABLE.put("冰糖", new double[]{397, 0.0, 0.0, 99.0, 0, 1});
        FOOD_TABLE.put("白醋", new double[]{21, 2.1, 0.3, 4.9, 0, 31});
    }

    @Override
    public NutritionAnalysisVO analyze(String dishName, List<String> ingredients, String servingSize) {
        long start = System.currentTimeMillis();
        NutritionAnalysisVO result = new NutritionAnalysisVO();
        result.setModelName(RULE_NAME);

        List<String> items = normalize(ingredients);
        double kcal = 0, protein = 0, fat = 0, carb = 0, fiber = 0, sodium = 0;
        List<String> matched = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        for (String item : items) {
            double[] food = findFood(item);
            double grams = guessGrams(item, dishName);
            if (food == null) {
                //未知食材按综合估算值处理
                food = new double[]{60, 3.0, 2.0, 8.0, 0.5, 50};
                unmatched.add(item);
            } else {
                matched.add(item);
            }
            kcal += food[0] * grams / 100;
            protein += food[1] * grams / 100;
            fat += food[2] * grams / 100;
            carb += food[3] * grams / 100;
            fiber += food[4] * grams / 100;
            sodium += food[5] * grams / 100;
        }

        if (items.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMsg("未提供主要食材，无法估算营养数据");
            return result;
        }

        result.setCalorie(round(kcal));
        result.setProtein(round(protein));
        result.setFat(round(fat));
        result.setCarbohydrate(round(carb));
        result.setFiber(round(fiber));
        result.setSodium(round(sodium));
        result.setAnalysisText(buildText(dishName, matched, unmatched, kcal, protein, fat, sodium));
        result.setSource("内置食材估算表计算，仅供参考");
        result.setSuccess(true);
        result.setCostMs(System.currentTimeMillis() - start);
        log.info("规则估算完成, 菜品: {}, 热量: {}, 匹配食材: {} 项, 未匹配: {}", dishName, result.getCalorie(), matched.size(), unmatched);
        return result;
    }

    /**
     * 依据食材类别与菜品形态假定单份用量（克）
     */
    private double guessGrams(String item, String dishName) {
        if (containsAny(item, OIL_KEYS)) {
            return 10;
        }
        if (containsAny(item, SAUCE_KEYS)) {
            return 10;
        }
        if (item.contains("食盐") || item.contains("盐")) {
            return 2;
        }
        if (item.contains("姜") || item.contains("蒜") || item.contains("葱") || item.contains("水") || item.contains("清汤")) {
            return 5;
        }
        //果汁、茶饮等以单个水果为主的饮品，单份用量明显大于普通配菜
        if (dishName != null && (dishName.contains("汁") || dishName.contains("饮"))) {
            return 250;
        }
        if (containsAny(item, MEAT_EGG_KEYS)) {
            return 100;
        }
        if (containsAny(item, STAPLE_KEYS)) {
            return 120;
        }
        return 80;
    }

    private double[] findFood(String item) {
        double[] exact = FOOD_TABLE.get(item);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<String, double[]> entry : FOOD_TABLE.entrySet()) {
            if (item.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean containsAny(String item, String[] keys) {
        for (String key : keys) {
            if (item.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalize(List<String> ingredients) {
        List<String> list = new ArrayList<>();
        if (ingredients == null) {
            return list;
        }
        for (String item : ingredients) {
            if (item != null && !item.trim().isEmpty()) {
                list.add(item.trim());
            }
        }
        return list;
    }

    private String buildText(String dishName, List<String> matched, List<String> unmatched,
                             double kcal, double protein, double fat, double sodium) {
        StringBuilder sb = new StringBuilder();
        sb.append("按").append(matched.size()).append("种可识别食材的常见营养估算值累加得出");
        if (!unmatched.isEmpty()) {
            sb.append("，其中").append(String.join("、", unmatched)).append("未命中估算表，按综合食材估算");
        }
        sb.append("。该菜品热量约").append(round(kcal)).append("千卡");
        if (kcal > 450) {
            sb.append("，热量偏高，建议控制份量或与他人分食");
        } else if (kcal < 150) {
            sb.append("，属于低热量选择");
        }
        if (protein >= 15) {
            sb.append("；蛋白质含量较好");
        }
        if (fat / Math.max(kcal, 1) > 0.4) {
            sb.append("；脂肪供能比较高，烹饪用油可适当减少");
        }
        if (sodium > 800) {
            sb.append("；钠含量偏高，建议搭配清淡菜品");
        }
        sb.append("。");
        return sb.toString();
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }

}
