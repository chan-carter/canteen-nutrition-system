package com.health.canteen.controller.admin;

import com.health.canteen.annotation.Log;
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
@RestController("adminMerchantController")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    /**
     * 新增商户（仅管理员）
     */
    @PostMapping
    @RequireRole(3)
    @Log(module = "商户管理", operation = "新增商户")
    public Result addMerchant(@RequestBody Merchant merchant) {
        log.info("新增商户: {}", merchant);
        merchantService.addMerchant(merchant);
        return Result.success();
    }

    /**
     * 修改商户状态（仅管理员）
     */
    @PutMapping("/{id}/status")
    @RequireRole(3)
    @Log(module = "商户管理", operation = "变更营业状态")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Merchant merchant) {
        log.info("修改商户状态: {}, {}", id, merchant.getStatus());
        merchantService.updateStatus(id, merchant.getStatus());
        return Result.success();
    }

    /**
     * 删除商户（仅管理员）
     */
    @DeleteMapping("/{ids}")
    @RequireRole(3)
    @Log(module = "商户管理", operation = "删除商户")
    public Result deleteMerchant(@PathVariable List<Integer> ids) {
        log.info("删除商户: {}", ids);
        merchantService.deleteMerchant(ids);
        return Result.success();
    }

}
