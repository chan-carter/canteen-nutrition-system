package com.health.canteen.controller.admin;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.AnnouncementPageQueryDTO;
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
@RestController("adminAnnouncementController")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 分页查询公告（仅管理员）
     */
    @GetMapping
    @RequireRole(3)
    public Result getPage(AnnouncementPageQueryDTO pageQueryDTO) {
        log.info("分页查询公告: {}", pageQueryDTO);
        PageResult<Announcement> pageResult = announcementService.getPage(pageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @RequireRole(3)
    public Result add(@RequestBody Announcement announcement) {
        log.info("新增公告: {}", announcement.getTitle());
        announcementService.add(announcement);
        return Result.success();
    }

    @PutMapping
    @RequireRole(3)
    public Result update(@RequestBody Announcement announcement) {
        log.info("修改公告: {}", announcement.getId());
        announcementService.update(announcement);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequireRole(3)
    public Result updateStatus(@PathVariable Integer id, @RequestBody Announcement announcement) {
        log.info("修改公告状态: {}, {}", id, announcement.getStatus());
        announcementService.updateStatus(id, announcement.getStatus());
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    @RequireRole(3)
    public Result delete(@PathVariable List<Integer> ids) {
        log.info("删除公告: {}", ids);
        announcementService.delete(ids);
        return Result.success();
    }

}
