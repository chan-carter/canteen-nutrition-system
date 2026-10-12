package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.mapper.MerchantMapper;
import com.health.canteen.mapper.StallMapper;
import com.health.canteen.mapper.UserMapper;
import com.health.canteen.entity.Merchant;
import com.health.canteen.dto.MerchantPageQueryDTO;
import com.health.canteen.result.PageResult;
import com.health.canteen.service.MerchantService;
import com.health.canteen.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private StallMapper stallMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<Merchant> getMerchantPage(MerchantPageQueryDTO merchantQueryParam) {
        PageHelper.startPage(merchantQueryParam.getPage(), merchantQueryParam.getPageSize());
        List<Merchant> merchants = merchantMapper.getMerchantPage(merchantQueryParam);
        Page<Merchant> page = (Page<Merchant>) merchants;
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public Merchant getMerchantById(Integer id) {
        Merchant merchant = merchantMapper.getMerchantById(id);
        if (merchant == null) {
            throw new BusinessException("商户不存在");
        }
        return merchant;
    }

    @Override
    public void addMerchant(Merchant merchant) {
        if (isBlank(merchant.getName())) {
            throw new BusinessException("商户名称不能为空");
        }
        if (merchant.getStatus() == null) {
            merchant.setStatus(1);
        }
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());
        merchantMapper.addMerchant(merchant);
    }

    @Override
    public void updateMerchant(Merchant merchant) {
        if (merchant.getId() == null) {
            throw new BusinessException("商户ID不能为空");
        }
        if (!CurrentUser.isAdmin()) {
            //商户账号只能维护自身信息，且不能变更经营状态
            merchant.setId(CurrentUser.merchantId());
            merchant.setStatus(null);
        }
        getMerchantById(merchant.getId());
        merchant.setUpdateTime(LocalDateTime.now());
        merchantMapper.updateMerchant(merchant);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态取值不正确");
        }
        getMerchantById(id);
        merchantMapper.updateMerchantStatus(id, status, LocalDateTime.now());
    }

    @Override
    public void deleteMerchant(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的商户");
        }
        for (Integer id : ids) {
            if (stallMapper.countByMerchantId(id) > 0) {
                throw new BusinessException("商户下仍存在档口，请先删除对应档口");
            }
            if (userMapper.countByMerchantId(id) > 0) {
                throw new BusinessException("商户下仍存在绑定账号，请先处理账号");
            }
        }
        merchantMapper.deleteMerchant(ids);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
