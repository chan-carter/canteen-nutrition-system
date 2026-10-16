package com.health.canteen.controller.common;

import com.alibaba.excel.EasyExcel;
import com.health.canteen.annotation.Log;
import com.health.canteen.annotation.RequireRole;
import com.health.canteen.export.DishExportVO;
import com.health.canteen.export.OrderExportVO;
import com.health.canteen.mapper.ExportMapper;
import com.health.canteen.utils.CurrentUser;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * 报表导出（商户导出本商户数据，管理员导出全量数据）
 */
@Slf4j
@RequestMapping("/export")
@RestController("commonExportController")
@RequireRole({2, 3})
public class ExportController {

    @Autowired
    private ExportMapper exportMapper;

    @GetMapping("/dishes")
    @Log(module = "数据导出", operation = "导出菜品")
    public void exportDishes(HttpServletResponse response) throws IOException {
        List<DishExportVO> data = exportMapper.listDishes(scopedMerchantId());
        write(response, "菜品数据", DishExportVO.class, data);
    }

    @GetMapping("/orders")
    @Log(module = "数据导出", operation = "导出订单")
    public void exportOrders(HttpServletResponse response) throws IOException {
        List<OrderExportVO> data = exportMapper.listOrders(scopedMerchantId());
        write(response, "订单数据", OrderExportVO.class, data);
    }

    private Integer scopedMerchantId() {
        return CurrentUser.isMerchant() ? CurrentUser.merchantId() : null;
    }

    private <T> void write(HttpServletResponse response, String fileName, Class<T> clazz, List<T> data)
            throws IOException {
        String name = fileName + "_" + LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
        EasyExcel.write(response.getOutputStream(), clazz).sheet(fileName).doWrite(data);
    }

}
