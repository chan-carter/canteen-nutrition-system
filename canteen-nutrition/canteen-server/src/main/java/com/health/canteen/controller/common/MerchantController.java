package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.entity.Merchant;
import com.health.canteen.dto.MerchantPageQueryDTO;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import com.health.canteen.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/merchants")
@RestController("commonMerchantController")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 分页查询商户信息
     */
    @GetMapping
    public Result getMerchantPage(MerchantPageQueryDTO merchantQueryParam) {
        log.info("分页查询商户信息: {}", merchantQueryParam);
        PageResult<Merchant> pageResult = merchantService.getMerchantPage(merchantQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询商户
     */
    @GetMapping("/{id}")
    public Result getMerchantById(@PathVariable Integer id) {
        log.info("根据ID查询商户: {}", id);
        return Result.success(merchantService.getMerchantById(id));
    }

}
