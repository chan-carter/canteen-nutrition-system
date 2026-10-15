package com.health.canteen.mapper;

import com.health.canteen.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AnnouncementMapper {

    List<Announcement> getAnnouncementPage(@Param("title") String title, @Param("status") Integer status);

    List<Announcement> getPublished();

    List<Announcement> getUnreadPublished(@Param("userId") Integer userId);

    int markAllRead(@Param("userId") Integer userId);

    Announcement getById(Integer id);

    void add(Announcement announcement);

    void update(Announcement announcement);

    void updateStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    void delete(List<Integer> ids);

    void deleteReadByAnnouncementIds(List<Integer> ids);

}
