package com.health.canteen.service;

import com.health.canteen.context.BaseContext;
import com.health.canteen.context.LoginUser;
import com.health.canteen.dto.HealthProfileDTO;
import com.health.canteen.entity.HealthProfile;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.DietRecordMapper;
import com.health.canteen.mapper.HealthProfileMapper;
import com.health.canteen.mapper.WeightLogMapper;
import com.health.canteen.service.impl.HealthServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 营养目标计算的回归基线：BMR/TDEE/目标热量/三大营养素是推荐与追踪模块的地基，
 * 公式一改整条链路都会偏，所以把线上实测过的一组数值钉死在这里。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("健康档案营养计算")
class HealthServiceCalcTest {

    @Mock
    private HealthProfileMapper healthProfileMapper;

    @Mock
    private DietRecordMapper dietRecordMapper;

    @Mock
    private WeightLogMapper weightLogMapper;

    @InjectMocks
    private HealthServiceImpl healthService;

    @BeforeEach
    void login() {
        BaseContext.set(new LoginUser(1, "zhangsan", 1, null));
    }

    @AfterEach
    void logout() {
        BaseContext.remove();
    }

    private HealthProfile save(int gender, String height, String weight, int activity, String goal) {
        HealthProfileDTO dto = new HealthProfileDTO();
        dto.setGender(gender);
        dto.setBirthday(LocalDate.now().minusYears(26));
        dto.setHeightCm(new BigDecimal(height));
        dto.setWeightKg(new BigDecimal(weight));
        dto.setActivityLevel(activity);
        dto.setHealthGoal(goal);
        healthService.saveProfile(dto);

        ArgumentCaptor<HealthProfile> captor = ArgumentCaptor.forClass(HealthProfile.class);
        verify(healthProfileMapper, atLeastOnce()).saveOrUpdate(captor.capture());
        List<HealthProfile> saved = captor.getAllValues();
        return saved.get(saved.size() - 1);
    }

    @Test
    @DisplayName("男/175cm/70kg/中度活动/减脂：与线上实测数值一致")
    void maleCutting() {
        HealthProfile p = save(1, "175", "70", 3, "减脂");

        // 10*70 + 6.25*175 - 5*26 + 5
        assertThat(p.getBmr()).isEqualByComparingTo("1668.8");
        assertThat(p.getTdee()).isEqualByComparingTo("2586.6");
        assertThat(p.getTargetCalorie()).isEqualByComparingTo("2186.6");
        assertThat(p.getTargetProtein()).isEqualByComparingTo("112.0");
        assertThat(p.getTargetFat()).isEqualByComparingTo("60.7");
        assertThat(p.getTargetCarb()).isEqualByComparingTo("298.1");
        assertThat(p.getHealthGoal()).isEqualTo("减脂");
    }

    @Test
    @DisplayName("女/162cm/60kg/久坐/均衡：BMR 用 -161 分支")
    void femaleBalanced() {
        HealthProfile p = save(2, "162", "60", 1, "均衡饮食");

        assertThat(p.getBmr()).isEqualByComparingTo("1321.5");
        assertThat(p.getTdee()).isEqualByComparingTo("1585.8");
        assertThat(p.getTargetCalorie()).isEqualByComparingTo("1585.8");
        assertThat(p.getTargetProtein()).isEqualByComparingTo("72.0");
    }

    @Test
    @DisplayName("增肌在 TDEE 上加 300 kcal，蛋白按 1.8g/kg")
    void bulking() {
        HealthProfile p = save(1, "175", "70", 3, "增肌");

        assertThat(p.getTargetCalorie()).isEqualByComparingTo("2886.6");
        assertThat(p.getTargetProtein()).isEqualByComparingTo("126.0");
    }

    @Test
    @DisplayName("减脂后低于 1200 kcal 时按安全下限兜住")
    void floorsAtMinimumCalorie() {
        HealthProfile p = save(2, "150", "45", 1, "减脂");

        // BMR 1096.5 × 1.2 = 1315.8，减脂 -400 后本应 915.8，被抬到下限
        assertThat(p.getBmr()).isEqualByComparingTo("1096.5");
        assertThat(p.getTdee()).isEqualByComparingTo("1315.8");
        assertThat(p.getTargetCalorie()).isEqualByComparingTo("1200.0");
        assertThat(p.getTargetCarb()).isNotNegative();
    }

    @Test
    @DisplayName("目标同义词归一：减肥=减脂、健身=增肌、空值=均衡")
    void normalizesGoalSynonyms() {
        assertThat(save(1, "175", "70", 3, "减肥").getHealthGoal()).isEqualTo("减脂");
        assertThat(save(1, "175", "70", 3, "健身").getHealthGoal()).isEqualTo("增肌");
        assertThat(save(1, "175", "70", 3, null).getHealthGoal()).isEqualTo("均衡");
    }

    @Test
    @DisplayName("非法入参一律拒绝，不落库")
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> save(3, "175", "70", 3, "减脂"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("性别");
        assertThatThrownBy(() -> save(1, "0", "70", 3, "减脂"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("身高");
        assertThatThrownBy(() -> save(1, "175", "70", 9, "减脂"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("活动量");

        HealthProfileDTO future = new HealthProfileDTO();
        future.setGender(1);
        future.setBirthday(LocalDate.now().plusDays(1));
        future.setHeightCm(new BigDecimal("175"));
        future.setWeightKg(new BigDecimal("70"));
        assertThatThrownBy(() -> healthService.saveProfile(future))
                .isInstanceOf(BusinessException.class).hasMessageContaining("出生日期");

        verify(healthProfileMapper, never()).saveOrUpdate(any());
    }

}
