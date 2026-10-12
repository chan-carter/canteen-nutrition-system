package com.health.canteen.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.health.canteen.constant.RedisConstant;
import com.health.canteen.constant.RoleConstant;
import com.health.canteen.exception.AccountDisabledException;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.exception.LoginFailedException;
import com.health.canteen.mapper.MerchantMapper;
import com.health.canteen.mapper.UserMapper;
import com.health.canteen.context.*;
import com.health.canteen.dto.*;
import com.health.canteen.entity.*;
import com.health.canteen.properties.JwtProperties;
import com.health.canteen.result.*;
import com.health.canteen.vo.*;
import com.health.canteen.service.CaptchaService;
import com.health.canteen.service.UserService;
import com.health.canteen.utils.CurrentUser;
import com.health.canteen.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CaptchaService captchaService;

    @Override
    public LoginVO login(LoginDTO loginParam) {
        if (loginParam == null || isBlank(loginParam.getUsername()) || isBlank(loginParam.getPassword())) {
            throw new BusinessException("账号和密码不能为空");
        }
        User user = userMapper.getByUsername(loginParam.getUsername());
        if (user == null || !passwordEncoder.matches(loginParam.getPassword(), user.getPassword())) {
            log.warn("登录失败, 账号: {}", loginParam.getUsername());
            throw new LoginFailedException();
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new AccountDisabledException();
        }

        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole(), user.getMerchantId());
        String token = jwtUtil.createToken(loginUser);
        //登录态写入Redis：同一账号只保留最新令牌，退出登录或重新登录后旧令牌立即失效
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_TOKEN_KEY + user.getId(), token,
                Duration.ofMillis(jwtProperties.getTtl()));
        log.info("登录成功, 账号: {}, 角色: {}", user.getUsername(), user.getRole());
        return new LoginVO(user.getId(), user.getUsername(), user.getNickname(), user.getRole(), user.getMerchantId(), token);
    }

    @Override
    public void logout() {
        Integer userId = CurrentUser.id();
        stringRedisTemplate.delete(RedisConstant.LOGIN_TOKEN_KEY + userId);
        log.info("退出登录，已清除Redis中的登录态, userId: {}", userId);
    }

    @Override
    public void register(RegisterDTO registerParam) {
        if (registerParam == null || isBlank(registerParam.getUsername()) || isBlank(registerParam.getPassword())) {
            throw new BusinessException("账号和密码不能为空");
        }
        //先校验图形验证码，避免脚本批量注册
        captchaService.verify(registerParam.getCaptchaKey(), registerParam.getCaptchaCode());
        if (registerParam.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        User user = new User();
        user.setUsername(registerParam.getUsername());
        user.setPassword(passwordEncoder.encode(registerParam.getPassword()));
        userNicknameCheck(registerParam.getNickname());
        user.setNickname(registerParam.getNickname());
        user.setPhone(registerParam.getPhone());
        user.setHealthGoal(registerParam.getHealthGoal());
        user.setPreference(registerParam.getPreference());
        user.setTaboo(registerParam.getTaboo());
        //自助注册只能创建学生账号，角色与状态由服务端固定
        user.setRole(RoleConstant.ROLE_STUDENT);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.addUser(user);
        log.info("学生注册成功, 账号: {}", user.getUsername());
    }

    @Override
    public PageResult<User> getUserPage(UserPageQueryDTO userQueryParam) {
        PageHelper.startPage(userQueryParam.getPage(), userQueryParam.getPageSize());
        List<User> users = userMapper.getUserPage(userQueryParam);
        Page<User> page = (Page<User>) users;
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    public User getUserById(Integer id) {
        boolean admin = CurrentUser.isAdmin();
        if (!admin && !id.equals(CurrentUser.id())) {
            throw new BusinessException("只能查看本人的信息");
        }
        User user = userMapper.getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public void addUser(User user) {
        if (isBlank(user.getUsername()) || isBlank(user.getPassword())) {
            throw new BusinessException("账号和密码不能为空");
        }
        userNicknameCheck(user.getNickname());
        if (user.getRole() == null || user.getRole() < RoleConstant.ROLE_STUDENT || user.getRole() > RoleConstant.ROLE_ADMIN) {
            throw new BusinessException("角色取值不正确");
        }
        if (user.getRole() == RoleConstant.ROLE_MERCHANT) {
            if (user.getMerchantId() == null) {
                throw new BusinessException("商户账号必须绑定所属商户");
            }
            if (merchantMapper.getMerchantById(user.getMerchantId()) == null) {
                throw new BusinessException("绑定的商户不存在");
            }
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.addUser(user);
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        boolean admin = CurrentUser.isAdmin();
        if (!admin && !user.getId().equals(CurrentUser.id())) {
            throw new BusinessException("只能修改本人的信息");
        }
        if (!admin) {
            //非管理员不允许通过该接口变更角色、状态和商户绑定关系
            user.setRole(null);
            user.setStatus(null);
            user.setMerchantId(null);
        }
        if (user.getNickname() != null) {
            userNicknameCheck(user.getNickname());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateUser(user);
    }

    @Override
    public void deleteUser(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        if (ids.contains(CurrentUser.id())) {
            throw new BusinessException("不能删除当前登录的账号");
        }
        userMapper.deleteUser(ids);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO param) {
        if (param == null || isBlank(param.getOldPassword()) || isBlank(param.getNewPassword())) {
            throw new BusinessException("原密码和新密码均不能为空");
        }
        if (param.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        Integer userId = CurrentUser.id();
        User user = userMapper.getByUsername(currentUsername());
        if (user == null || !passwordEncoder.matches(param.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(param.getNewPassword()), LocalDateTime.now());
        log.info("用户修改密码成功, userId: {}", userId);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态取值不正确");
        }
        if (id.equals(CurrentUser.id())) {
            throw new BusinessException("不能修改当前登录账号的状态");
        }
        if (userMapper.getUserById(id) == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.updateStatus(id, status, LocalDateTime.now());
    }

    @Override
    public User getCurrentUser() {
        return userMapper.getUserById(CurrentUser.id());
    }

    private String currentUsername() {
        return CurrentUser.get().getUsername();
    }

    private void userNicknameCheck(String nickname) {
        if (isBlank(nickname)) {
            throw new BusinessException("昵称不能为空");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
