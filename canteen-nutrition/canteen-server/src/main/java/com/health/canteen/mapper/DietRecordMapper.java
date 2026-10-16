package com.health.canteen.mapper;

import com.health.canteen.entity.DietRecord;
import com.health.canteen.vo.DailyIntakeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DietRecordMapper {

    void add(DietRecord record);

    /**
     * 下单成功后按订单明细批量落库（source=order）
     */
    void batchAdd(@Param("records") List<DietRecord> records);

    void deleteById(@Param("id") Integer id, @Param("userId") Integer userId);

    List<DietRecord> listByUserDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

    /**
     * 某天摄入合计（无记录时返回 null，由 service 兜底为 0）
     */
    DailyIntakeVO sumByUserDate(@Param("userId") Integer userId, @Param("date") LocalDate date);
}
