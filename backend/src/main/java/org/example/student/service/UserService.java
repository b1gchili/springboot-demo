package org.example.student.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.student.exception.BusinessException;
import org.example.student.mapper.UserMapper;
import org.example.student.model.PageResult;
import org.example.student.model.UserListItem;
import org.example.student.model.UserRecord;
import org.example.student.model.UserRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PageResult<UserListItem> listUsers(Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(currentPage, currentSize);
        List<UserListItem> list = userMapper.listUsers();
        PageInfo<UserListItem> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), currentPage, currentSize);
    }

    /**
     * 新增用户。
     */
    public String addUser(UserRequest request) {
        validateUser(request, true);
        checkUnique(request.getUsername(), request.getPhone(), null);

        UserRecord user = new UserRecord();
        user.setUserId(UUID.randomUUID().toString().replace("-", ""));
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setDisplayName(request.getDisplayName());
        userMapper.insertUser(user);
        return user.getUserId();
    }

    /**
     * 修改用户。
     */
    public void updateUser(String userId, UserRequest request) {
        UserRecord existing = userMapper.findByUserId(userId);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }

        validateUser(request, false);
        checkUnique(request.getUsername(), request.getPhone(), userId);

        UserRecord user = new UserRecord();
        user.setUserId(userId);
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setDisplayName(request.getDisplayName());

        int rows = userMapper.updateUser(user);
        if (rows == 0) {
            throw new BusinessException(404, "用户不存在");
        }
    }

    /**
     * 删除用户。
     *
     * 这里使用软删除，把 enabled 设置为 0。
     */
    public void deleteUser(String userId) {
        int rows = userMapper.disableUser(userId);
        if (rows == 0) {
            throw new BusinessException(404, "用户不存在");
        }
    }

    private void validateUser(UserRequest request, boolean passwordRequired) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException(400, "账号不能为空");
        }
        if (request.getUsername().length() > 50) {
            throw new BusinessException(400, "账号不能超过50个字符");
        }
        if (!request.getUsername().matches("^[A-Za-z0-9_]+$")) {
            throw new BusinessException(400, "账号只能包含字母、数字和下划线");
        }
        if (!StringUtils.hasText(request.getDisplayName())) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (request.getDisplayName().length() > 50) {
            throw new BusinessException(400, "姓名不能超过50个字符");
        }
        if (!StringUtils.hasText(request.getPhone()) || !request.getPhone().matches("^1\\d{10}$")) {
            throw new BusinessException(400, "手机号格式错误");
        }
        if (passwordRequired && !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(400, "密码不能为空");
        }
        if (StringUtils.hasText(request.getPassword()) && request.getPassword().length() > 100) {
            throw new BusinessException(400, "密码不能超过100个字符");
        }
    }

    private void checkUnique(String username, String phone, String excludeUserId) {
        if (userMapper.countByUsername(username, excludeUserId) > 0) {
            throw new BusinessException(400, "账号已存在");
        }
        if (userMapper.countByPhone(phone, excludeUserId) > 0) {
            throw new BusinessException(400, "手机号已存在");
        }
    }
}
