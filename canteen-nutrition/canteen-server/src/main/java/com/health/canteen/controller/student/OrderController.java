package com.health.canteen.controller.student;

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
@RestController("studentOrderController")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 学生提交点餐或预订
     */
    @PostMapping
    @RequireRole(1)
    public Result createOrder(@RequestBody OrderSubmitDTO orderSubmitDto) {
        log.info("创建订单: {}", orderSubmitDto);
        Orders orders = orderService.createOrder(orderSubmitDto);
        return Result.success(orders);
    }

}
