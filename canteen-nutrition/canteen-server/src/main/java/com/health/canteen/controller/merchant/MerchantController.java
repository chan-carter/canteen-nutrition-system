package com.health.canteen.controller.merchant;

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
@RestController("merchantMerchantController")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 修改商户信息
     */
    @PutMapping
    public Result updateMerchant(@RequestBody Merchant merchant) {
        log.info("修改商户信息: {}", merchant);
        merchantService.updateMerchant(merchant);
        return Result.success();
    }

}
