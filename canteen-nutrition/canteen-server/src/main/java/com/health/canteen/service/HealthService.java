package com.health.canteen.service;

import com.health.canteen.dto.DietRecordDTO;
import com.health.canteen.dto.HealthProfileDTO;
import com.health.canteen.dto.WeightLogDTO;
import com.health.canteen.entity.DietRecord;
import com.health.canteen.entity.HealthProfile;
import com.health.canteen.entity.WeightLog;
import com.health.canteen.vo.DailyIntakeVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养健康追踪：身体档案、每日摄入、体重趋势
 */
public interface HealthService {

    HealthProfile getProfile();

    HealthProfile saveProfile(HealthProfileDTO dto);

    DailyIntakeVO dailyIntake(LocalDate date);

    List<DietRecord> listDiet(LocalDate date);

    void addDiet(DietRecordDTO dto);

    void deleteDiet(Integer id);

    void addWeight(WeightLogDTO dto);

    List<WeightLog> weightTrend(int days);
}
