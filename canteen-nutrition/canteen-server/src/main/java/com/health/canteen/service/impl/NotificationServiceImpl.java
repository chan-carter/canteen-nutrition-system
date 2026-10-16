package com.health.canteen.service.impl;

import com.health.canteen.entity.Notification;
import com.health.canteen.mapper.NotificationMapper;
import com.health.canteen.service.NotificationService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void push(Integer userId, String type, String title, String content, Integer bizId) {
        if (userId == null) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setBizId(bizId);
        n.setIsRead(0);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.add(n);
    }

    @Override
    public List<Notification> list(int limit) {
        int n = limit <= 0 ? 30 : Math.min(limit, 100);
        return notificationMapper.listByUser(CurrentUser.id(), n);
    }

    @Override
    public int unreadCount() {
        return notificationMapper.countUnread(CurrentUser.id());
    }

    @Override
    public void markRead(Integer id) {
        notificationMapper.markRead(id, CurrentUser.id());
    }

    @Override
    public void markAllRead() {
        notificationMapper.markAllRead(CurrentUser.id());
    }
}
