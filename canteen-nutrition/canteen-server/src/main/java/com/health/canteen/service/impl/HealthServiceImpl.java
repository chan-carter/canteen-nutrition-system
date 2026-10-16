package com.health.canteen.service.impl;

import com.health.canteen.dto.DietRecordDTO;
import com.health.canteen.dto.HealthProfileDTO;
import com.health.canteen.dto.WeightLogDTO;
import com.health.canteen.entity.DietRecord;
import com.health.canteen.entity.HealthProfile;
import com.health.canteen.entity.WeightLog;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DietRecordMapper;
import com.health.canteen.mapper.HealthProfileMapper;
import com.health.canteen.mapper.WeightLogMapper;
import com.health.canteen.service.HealthService;
import com.health.canteen.utils.CurrentUser;
import com.health.canteen.vo.DailyIntakeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/**
 * 营养健康追踪。热量目标用 Mifflin-St Jeor 估算 BMR，再按活动量得 TDEE，
 * 结合健康目标做热量增减，最后按"每克蛋白/碳水4kcal、脂肪9kcal"拆三大营养素。
 * 结果均为估算，仅供饮食参考，不作为医学建议。
 */
@Slf4j
@Service
public class HealthServiceImpl implements HealthService {

    private static final BigDecimal PROTEIN_KCAL = new BigDecimal("4");
    private static final BigDecimal CARB_KCAL = new BigDecimal("4");
    private static final BigDecimal FAT_KCAL = new BigDecimal("9");
    private static final BigDecimal MIN_CALORIE = new BigDecimal("1200");

    @Autowired
    private HealthProfileMapper healthProfileMapper;

    @Autowired
    private DietRecordMapper dietRecordMapper;

    @Autowired
    private WeightLogMapper weightLogMapper;

    @Override
    public HealthProfile getProfile() {
        return healthProfileMapper.getByUserId(CurrentUser.id());
    }

    @Override
    public HealthProfile saveProfile(HealthProfileDTO dto) {
        if (dto.getGender() == null || (dto.getGender() != 1 && dto.getGender() != 2)) {
            throw new BusinessException("请选择正确的性别");
        }
        if (dto.getBirthday() == null) {
            throw new BusinessException("请填写出生日期");
        }
        if (isMissing(dto.getHeightCm()) || isMissing(dto.getWeightKg())) {
            throw new BusinessException("身高和体重不能为空");
        }
        int activity = dto.getActivityLevel() == null ? 2 : dto.getActivityLevel();
        if (activity < 1 || activity > 4) {
            throw new BusinessException("活动量取值不正确");
        }
        String goal = normalizeGoal(dto.getHealthGoal());

        // 将来日期用 Period.between 会得到 age=0，绕过后面的年龄区间校验，必须先单独挡掉
        if (dto.getBirthday().isAfter(LocalDate.now())) {
            throw new BusinessException("出生日期不能晚于今天");
        }
        int age = Period.between(dto.getBirthday(), LocalDate.now()).getYears();
        if (age < 0 || age > 120) {
            throw new BusinessException("出生日期不合理");
        }

        BigDecimal bmr = calcBmr(dto.getGender(), dto.getHeightCm(), dto.getWeightKg(), age);
        BigDecimal tdee = bmr.multiply(activityFactor(activity)).setScale(1, RoundingMode.HALF_UP);
        BigDecimal targetCalorie = targetCalorie(tdee, goal);
        BigDecimal targetProtein = dto.getWeightKg().multiply(proteinPerKg(goal)).setScale(1, RoundingMode.HALF_UP);
        BigDecimal targetFat = targetCalorie.multiply(new BigDecimal("0.25"))
                .divide(FAT_KCAL, 1, RoundingMode.HALF_UP);
        BigDecimal targetCarb = targetCalorie
                .subtract(targetProtein.multiply(PROTEIN_KCAL))
                .subtract(targetFat.multiply(FAT_KCAL))
                .divide(CARB_KCAL, 1, RoundingMode.HALF_UP);
        if (targetCarb.compareTo(BigDecimal.ZERO) < 0) {
            targetCarb = BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        HealthProfile profile = new HealthProfile();
        profile.setUserId(CurrentUser.id());
        profile.setGender(dto.getGender());
        profile.setBirthday(dto.getBirthday());
        profile.setHeightCm(dto.getHeightCm());
        profile.setWeightKg(dto.getWeightKg());
        profile.setActivityLevel(activity);
        profile.setHealthGoal(goal);
        profile.setBmr(bmr);
        profile.setTdee(tdee);
        profile.setTargetCalorie(targetCalorie);
        profile.setTargetProtein(targetProtein);
        profile.setTargetFat(targetFat);
        profile.setTargetCarb(targetCarb);
        profile.setCreateTime(now);
        profile.setUpdateTime(now);
        healthProfileMapper.saveOrUpdate(profile);
        log.info("保存身体档案, 用户: {}, TDEE: {}, 目标热量: {}", profile.getUserId(), tdee, targetCalorie);
        return healthProfileMapper.getByUserId(CurrentUser.id());
    }

    @Override
    public DailyIntakeVO dailyIntake(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        DailyIntakeVO vo = dietRecordMapper.sumByUserDate(CurrentUser.id(), day);
        if (vo == null) {
            vo = new DailyIntakeVO();
        }
        vo.setDate(day);
        vo.setRecordCount(vo.getRecordCount() == null ? 0 : vo.getRecordCount());
        HealthProfile profile = healthProfileMapper.getByUserId(CurrentUser.id());
        if (profile != null) {
            vo.setHasProfile(true);
            vo.setTargetCalorie(profile.getTargetCalorie());
            vo.setTargetProtein(profile.getTargetProtein());
            vo.setTargetFat(profile.getTargetFat());
            vo.setTargetCarb(profile.getTargetCarb());
        } else {
            vo.setHasProfile(false);
        }
        return vo;
    }

    @Override
    public List<DietRecord> listDiet(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        return dietRecordMapper.listByUserDate(CurrentUser.id(), day);
    }

    @Override
    public void addDiet(DietRecordDTO dto) {
        if (dto.getCalorie() == null) {
            throw new BusinessException("请至少填写热量");
        }
        DietRecord record = new DietRecord();
        record.setUserId(CurrentUser.id());
        record.setDishId(dto.getDishId());
        record.setDishName(dto.getDishName() == null ? "自定义记录" : dto.getDishName());
        record.setRecordDate(dto.getRecordDate() == null ? LocalDate.now() : dto.getRecordDate());
        record.setMealType(dto.getMealType());
        record.setCalorie(dto.getCalorie());
        record.setProtein(dto.getProtein());
        record.setFat(dto.getFat());
        record.setCarbohydrate(dto.getCarbohydrate());
        record.setSodium(dto.getSodium());
        record.setSource("manual");
        record.setCreateTime(LocalDateTime.now());
        dietRecordMapper.add(record);
    }

    @Override
    public void deleteDiet(Integer id) {
        dietRecordMapper.deleteById(id, CurrentUser.id());
    }

    @Override
    public void addWeight(WeightLogDTO dto) {
        if (isMissing(dto.getWeightKg())) {
            throw new BusinessException("请填写体重");
        }
        WeightLog log = new WeightLog();
        log.setUserId(CurrentUser.id());
        log.setWeightKg(dto.getWeightKg());
        log.setRecordDate(dto.getRecordDate() == null ? LocalDate.now() : dto.getRecordDate());
        log.setNote(dto.getNote());
        log.setCreateTime(LocalDateTime.now());
        weightLogMapper.add(log);
    }

    @Override
    public List<WeightLog> weightTrend(int days) {
        int span = days <= 0 ? 30 : Math.min(days, 365);
        LocalDate since = LocalDate.now().minusDays(span);
        return weightLogMapper.listSince(CurrentUser.id(), since);
    }

    /**
     * Mifflin-St Jeor：男 10W+6.25H-5A+5，女 10W+6.25H-5A-161
     */
    private BigDecimal calcBmr(int gender, BigDecimal height, BigDecimal weight, int age) {
        BigDecimal base = weight.multiply(new BigDecimal("10"))
                .add(height.multiply(new BigDecimal("6.25")))
                .subtract(new BigDecimal(5L * age));
        BigDecimal offset = gender == 1 ? new BigDecimal("5") : new BigDecimal("-161");
        return base.add(offset).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal activityFactor(int activity) {
        switch (activity) {
            case 1:
                return new BigDecimal("1.2");
            case 3:
                return new BigDecimal("1.55");
            case 4:
                return new BigDecimal("1.725");
            default:
                return new BigDecimal("1.375");
        }
    }

    private BigDecimal targetCalorie(BigDecimal tdee, String goal) {
        BigDecimal target;
        if (goal.contains("减脂")) {
            target = tdee.subtract(new BigDecimal("400"));
        } else if (goal.contains("增肌")) {
            target = tdee.add(new BigDecimal("300"));
        } else {
            target = tdee;
        }
        if (target.compareTo(MIN_CALORIE) < 0) {
            target = MIN_CALORIE;
        }
        return target.setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal proteinPerKg(String goal) {
        if (goal.contains("增肌")) {
            return new BigDecimal("1.8");
        }
        if (goal.contains("减脂")) {
            return new BigDecimal("1.6");
        }
        return new BigDecimal("1.2");
    }

    private String normalizeGoal(String goal) {
        if (goal == null) {
            return "均衡";
        }
        if (goal.contains("减脂") || goal.contains("减肥") || goal.contains("控重")) {
            return "减脂";
        }
        if (goal.contains("增肌") || goal.contains("健身")) {
            return "增肌";
        }
        return "均衡";
    }

    private boolean isMissing(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }

}
