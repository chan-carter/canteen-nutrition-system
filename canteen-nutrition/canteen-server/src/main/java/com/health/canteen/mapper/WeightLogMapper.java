package com.health.canteen.mapper;

import com.health.canteen.entity.WeightLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WeightLogMapper {

    void add(WeightLog log);

    List<WeightLog> listSince(@Param("userId") Integer userId, @Param("since") LocalDate since);
}
