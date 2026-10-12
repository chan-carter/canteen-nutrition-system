package com.health.canteen.service;

import com.health.canteen.entity.Merchant;
import com.health.canteen.dto.MerchantPageQueryDTO;
import com.health.canteen.result.PageResult;

import java.util.List;

public interface MerchantService {

    PageResult<Merchant> getMerchantPage(MerchantPageQueryDTO merchantQueryParam);

    Merchant getMerchantById(Integer id);

    void addMerchant(Merchant merchant);

    void updateMerchant(Merchant merchant);

    void updateStatus(Integer id, Integer status);

    void deleteMerchant(List<Integer> ids);

}
