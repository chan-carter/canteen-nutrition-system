package com.health.canteen.controller.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.health.canteen.annotation.RequireRole;
import com.health.canteen.entity.OperationLog;
import com.health.canteen.mapper.OperationLogMapper;
import com.health.canteen.result.PageResult;
import com.health.canteen.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志查询（仅管理员）
 */
@Slf4j
@RequestMapping("/logs")
@RestController("adminOperationLogController")
@RequireRole(3)
public class OperationLogController {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @GetMapping
    public Result page(@RequestParam(required = false) String module,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<OperationLog> list = operationLogMapper.list(module, keyword);
        Page<OperationLog> p = (Page<OperationLog>) list;
        return Result.success(new PageResult<>(p.getTotal(), p.getResult()));
    }

}
