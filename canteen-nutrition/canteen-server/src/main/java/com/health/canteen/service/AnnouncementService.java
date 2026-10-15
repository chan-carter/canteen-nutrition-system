package com.health.canteen.service;

import com.health.canteen.dto.AnnouncementPageQueryDTO;
import com.health.canteen.entity.Announcement;
import com.health.canteen.result.PageResult;

import java.util.List;

public interface AnnouncementService {

    PageResult<Announcement> getPage(AnnouncementPageQueryDTO pageQueryDTO);

    List<Announcement> getPublished();

    List<Announcement> getUnread();

    int markAllRead();

    Announcement getById(Integer id);

    void add(Announcement announcement);

    void update(Announcement announcement);

    void updateStatus(Integer id, Integer status);

    void delete(List<Integer> ids);

}
