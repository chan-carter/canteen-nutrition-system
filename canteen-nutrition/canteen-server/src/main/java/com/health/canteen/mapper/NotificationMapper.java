package com.health.canteen.mapper;

import com.health.canteen.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    void add(Notification notification);

    List<Notification> listByUser(@Param("userId") Integer userId, @Param("limit") int limit);

    int countUnread(@Param("userId") Integer userId);

    void markRead(@Param("id") Integer id, @Param("userId") Integer userId);

    void markAllRead(@Param("userId") Integer userId);
}
