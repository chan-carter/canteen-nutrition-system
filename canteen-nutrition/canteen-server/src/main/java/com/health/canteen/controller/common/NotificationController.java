package com.health.canteen.controller.common;

import com.health.canteen.result.Result;
import com.health.canteen.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 站内消息中心（登录用户查看本人消息）
 */
@Slf4j
@RequestMapping("/notifications")
@RestController("commonNotificationController")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result list(@RequestParam(defaultValue = "30") int limit) {
        return Result.success(notificationService.list(limit));
    }

    @GetMapping("/unread")
    public Result unread() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", notificationService.unreadCount());
        return Result.success(data);
    }

    @PutMapping("/{id}/read")
    public Result markRead(@PathVariable Integer id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }

}
