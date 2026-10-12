package com.health.canteen.controller.admin;

import com.health.canteen.annotation.Log;
import com.health.canteen.annotation.RequireRole;
import com.health.canteen.result.PageResult;
import com.health.canteen.dto.PasswordUpdateDTO;
import com.health.canteen.result.Result;
import com.health.canteen.entity.User;
import com.health.canteen.dto.UserPageQueryDTO;
import com.health.canteen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController("adminUserController")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户（仅管理员）
     */
    @GetMapping
    @RequireRole(3)
    public Result getUserPage(UserPageQueryDTO userQueryParam) {
        log.info("分页查询用户: {}", userQueryParam);
        PageResult<User> pageResult = userService.getUserPage(userQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 新增用户（仅管理员）
     */
    @PostMapping
    @RequireRole(3)
    @Log(module = "用户管理", operation = "新增用户")
    public Result addUser(@RequestBody User user) {
        log.info("新增用户: {}", user.getUsername());
        userService.addUser(user);
        return Result.success();
    }

    /**
     * 启用/禁用账号（仅管理员）
     */
    @PutMapping("/{id}/status")
    @RequireRole(3)
    @Log(module = "用户管理", operation = "变更账号状态")
    public Result updateStatus(@PathVariable Integer id, @RequestBody User user) {
        log.info("修改用户状态: {}, {}", id, user.getStatus());
        userService.updateStatus(id, user.getStatus());
        return Result.success();
    }

    /**
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/{ids}")
    @RequireRole(3)
    @Log(module = "用户管理", operation = "删除用户")
    public Result deleteUser(@PathVariable List<Integer> ids) {
        log.info("删除用户: {}", ids);
        userService.deleteUser(ids);
        return Result.success();
    }

}
