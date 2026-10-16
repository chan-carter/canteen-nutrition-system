package com.health.canteen.mapper;

import com.health.canteen.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiChatMapper {

    void insert(AiChatMessage message);

    List<AiChatMessage> listBySession(@Param("userId") Integer userId,
                                      @Param("sessionId") String sessionId,
                                      @Param("limit") int limit);
}
