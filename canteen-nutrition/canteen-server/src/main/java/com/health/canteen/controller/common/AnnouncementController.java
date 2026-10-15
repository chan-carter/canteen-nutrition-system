package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.entity.Announcement;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import com.health.canteen.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/announcements")
@RestController("commonAnnouncementController")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 查询已发布公告，供各角色首页展示
     */
    @GetMapping("/published")
    public Result getPublished() {
        log.info("查询已发布公告");
        return Result.success(announcementService.getPublished());
    }

    /**
     * 当前用户的未读公告，用于顶部消息提醒红点
     */
    @GetMapping("/unread")
    public Result getUnread() {
        log.info("查询未读公告");
        return Result.success(announcementService.getUnread());
    }

    /**
     * 把当前用户可见的已发布公告全部标记为已读
     */
    @PutMapping("/read")
    public Result markAllRead() {
        log.info("标记公告已读");
        return Result.success(announcementService.markAllRead());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        log.info("查询公告详情: {}", id);
        return Result.success(announcementService.getById(id));
    }

}
