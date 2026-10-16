package com.health.canteen.mapper;

import com.health.canteen.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    void add(OperationLog log);

    List<OperationLog> list(@Param("module") String module, @Param("keyword") String keyword);
}
