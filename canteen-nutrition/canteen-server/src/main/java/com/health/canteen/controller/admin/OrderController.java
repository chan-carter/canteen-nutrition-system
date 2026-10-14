package com.health.canteen.controller.admin;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.dto.OrderPageQueryDTO;
import com.health.canteen.dto.OrderSubmitDTO;
import com.health.canteen.entity.Orders;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import com.health.canteen.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/orders")
@RestController("adminOrderController")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 删除订单（仅管理员）
     */
    @DeleteMapping("/{ids}")
    @RequireRole(3)
    public Result deleteOrders(@PathVariable List<Integer> ids) {
        log.info("删除订单: {}", ids);
        orderService.deleteOrders(ids);
        return Result.success();
    }

}
