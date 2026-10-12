package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.MerchantMapper;
import com.health.canteen.mapper.StallMapper;
import com.health.canteen.result.PageResult;
import com.health.canteen.entity.Stall;
import com.health.canteen.dto.StallPageQueryDTO;
import com.health.canteen.service.StallService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class StallServiceImpl implements StallService {

    @Autowired
    private StallMapper stallMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public PageResult<Stall> getStallPage(StallPageQueryDTO stallQueryParam) {
        if (CurrentUser.isMerchant()) {
            //商户只能查询本商户的档口
            stallQueryParam.setMerchantId(CurrentUser.merchantId());
        }
        PageHelper.startPage(stallQueryParam.getPage(), stallQueryParam.getPageSize());
        List<Stall> stalls = stallMapper.getStallPage(stallQueryParam);
        Page<Stall> page = (Page<Stall>) stalls;
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public Stall getStallById(Integer id) {
        Stall stall = stallMapper.getStallById(id);
        if (stall == null) {
            throw new BusinessException("档口不存在");
        }
        return stall;
    }

    @Override
    public void addStall(Stall stall) {
        if (isBlank(stall.getName())) {
            throw new BusinessException("档口名称不能为空");
        }
        if (CurrentUser.isMerchant()) {
            stall.setMerchantId(CurrentUser.merchantId());
        }
        if (stall.getMerchantId() == null) {
            throw new BusinessException("请选择所属商户");
        }
        if (merchantMapper.getMerchantById(stall.getMerchantId()) == null) {
            throw new BusinessException("所属商户不存在");
        }
        if (stall.getStatus() == null) {
            stall.setStatus(1);
        }
        stall.setCreateTime(LocalDateTime.now());
        stall.setUpdateTime(LocalDateTime.now());
        stallMapper.addStall(stall);
    }

    @Override
    public void updateStall(Stall stall) {
        if (stall.getId() == null) {
            throw new BusinessException("档口ID不能为空");
        }
        Stall old = getStallById(stall.getId());
        CurrentUser.checkMerchantOwner(old.getMerchantId());
        if (CurrentUser.isMerchant()) {
            //商户不能把档口转移到其他商户
            stall.setMerchantId(null);
        }
        stall.setUpdateTime(LocalDateTime.now());
        stallMapper.updateStall(stall);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态取值不正确");
        }
        Stall old = getStallById(id);
        CurrentUser.checkMerchantOwner(old.getMerchantId());
        stallMapper.updateStallStatus(id, status, LocalDateTime.now());
    }

    @Override
    public void deleteStall(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的档口");
        }
        for (Integer id : ids) {
            Stall stall = getStallById(id);
            CurrentUser.checkMerchantOwner(stall.getMerchantId());
            if (stallMapper.countDishByStallId(id) > 0) {
                throw new BusinessException("档口下仍存在菜品，请先删除对应菜品");
            }
        }
        stallMapper.deleteStall(ids);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
