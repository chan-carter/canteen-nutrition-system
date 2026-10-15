package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.dto.AnnouncementPageQueryDTO;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.AnnouncementMapper;
import com.health.canteen.entity.Announcement;
import com.health.canteen.result.PageResult;
import com.health.canteen.service.AnnouncementService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public PageResult<Announcement> getPage(AnnouncementPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        List<Announcement> list = announcementMapper.getAnnouncementPage(pageQueryDTO.getTitle(),
                pageQueryDTO.getStatus());
        Page<Announcement> result = (Page<Announcement>) list;
        return new PageResult<>(result.getTotal(), result.getResult());
    }

    @Override
    public List<Announcement> getPublished() {
        return announcementMapper.getPublished();
    }

    @Override
    public List<Announcement> getUnread() {
        return announcementMapper.getUnreadPublished(CurrentUser.id());
    }

    @Override
    @Transactional
    public int markAllRead() {
        int rows = announcementMapper.markAllRead(CurrentUser.id());
        log.info("标记公告已读, 用户: {}, 新增已读记录: {}", CurrentUser.id(), rows);
        return rows;
    }

    @Override
    public Announcement getById(Integer id) {
        Announcement announcement = announcementMapper.getById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }

    @Override
    public void add(Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            throw new BusinessException("公告标题不能为空");
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.add(announcement);
    }

    @Override
    public void update(Announcement announcement) {
        if (announcement.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        getById(announcement.getId());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.update(announcement);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态取值不正确");
        }
        getById(id);
        announcementMapper.updateStatus(id, status, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void delete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的公告");
        }
        announcementMapper.deleteReadByAnnouncementIds(ids);
        announcementMapper.delete(ids);
    }

}
