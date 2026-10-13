package com.health.canteen.mapper;

import com.health.canteen.entity.AiCallLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiCallLogMapper {

    /**
     * 记录一次AI接口调用
     */
    void addLog(AiCallLog aiCallLog);

    /**
     * 统计AI调用成功与失败次数
     */
    List<Map<String, Object>> getCallCountData();

}
