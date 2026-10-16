package com.health.canteen.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单导出行
 */
@Data
@ColumnWidth(18)
public class OrderExportVO {
    @ExcelProperty("订单号")
    private String orderNo;
    @ExcelProperty("下单学生")
    private String userNickname;
    @ExcelProperty("金额")
    private BigDecimal totalAmount;
    @ExcelProperty("总热量(kcal)")
    private BigDecimal totalCalorie;
    @ExcelProperty("状态")
    private String statusText;
    @ExcelProperty("下单时间")
    private LocalDateTime createTime;
}
