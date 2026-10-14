package com.health.canteen.controller.common;

import com.health.canteen.annotation.Log;
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
@RestController("commonOrderController")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 分页查询订单：学生只看本人订单，商户只看本商户订单
     */
    @GetMapping
    public Result getOrdersPage(OrderPageQueryDTO orderQueryParam) {
        log.info("分页查询订单: {}", orderQueryParam);
        PageResult<Orders> pageResult = orderService.getOrdersPage(orderQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{id}")
    public Result getOrdersById(@PathVariable Integer id) {
        log.info("查询订单详情: {}", id);
        return Result.success(orderService.getOrdersById(id));
    }

    /**
     * 修改订单状态
     */
    @PutMapping("/{id}/status")
    @Log(module = "订单管理", operation = "变更订单状态")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Orders orders) {
        log.info("修改订单状态: {}, {}", id, orders.getStatus());
        orderService.updateStatus(id, orders.getStatus());
        return Result.success();
    }

}
