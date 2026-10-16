package com.health.canteen.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品导出行
 */
@Data
@ColumnWidth(16)
public class DishExportVO {
    @ExcelProperty("菜品名称")
    private String name;
    @ExcelProperty("分类")
    private String categoryName;
    @ExcelProperty("档口")
    private String stallName;
    @ExcelProperty("价格")
    private BigDecimal price;
    @ExcelProperty("销量")
    private Integer sales;
    @ExcelProperty("热量(kcal)")
    private BigDecimal calorie;
    @ExcelProperty("状态")
    private String statusText;
}
