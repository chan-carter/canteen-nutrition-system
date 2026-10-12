package com.health.canteen.service;

import com.health.canteen.context.*;
import com.health.canteen.dto.*;
import com.health.canteen.entity.*;
import com.health.canteen.result.*;
import com.health.canteen.vo.*;

import java.util.List;

public interface UserService {

    LoginVO login(LoginDTO loginParam);

    void register(RegisterDTO registerParam);

    void logout();

    PageResult<User> getUserPage(UserPageQueryDTO userQueryParam);

    User getUserById(Integer id);

    void addUser(User user);

    void updateUser(User user);

    void deleteUser(List<Integer> ids);

    void updatePassword(PasswordUpdateDTO passwordUpdateParam);

    void updateStatus(Integer id, Integer status);

    User getCurrentUser();

}
