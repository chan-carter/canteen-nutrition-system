package com.health.canteen.task;

import com.health.canteen.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：每 5 分钟取消超过 30 分钟仍未接单的订单。
 */
@Slf4j
@Component
public class OrderTimeoutTask {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void cancelTimeoutOrders() {
        int n = orderService.cancelTimeoutOrders(30);
        log.info("定时任务：扫描超时未接单订单，自动取消 {} 笔", n);
    }
}
