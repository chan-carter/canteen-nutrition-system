package com.health.canteen.controller.common;

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
@RestController("commonUserController")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查询当前登录用户信息
     */
    @GetMapping("/me")
    public Result currentUser() {
        log.info("查询当前登录用户信息");
        return Result.success(userService.getCurrentUser());
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Integer id) {
        log.info("根据ID查询用户: {}", id);
        return Result.success(userService.getUserById(id));
    }

    /**
     * 修改用户：管理员可修改任意用户，其他角色只能修改本人
     */
    @PutMapping
    public Result updateUser(@RequestBody User user) {
        log.info("修改用户: {}", user.getId());
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateParam) {
        log.info("修改密码");
        userService.updatePassword(passwordUpdateParam);
        return Result.success();
    }

}
