package com.health.canteen.service;

import com.health.canteen.entity.Notification;

import java.util.List;

public interface NotificationService {

    /**
     * 供其它业务在状态变更时调用，生成一条站内消息
     */
    void push(Integer userId, String type, String title, String content, Integer bizId);

    List<Notification> list(int limit);

    int unreadCount();

    void markRead(Integer id);

    void markAllRead();
}
