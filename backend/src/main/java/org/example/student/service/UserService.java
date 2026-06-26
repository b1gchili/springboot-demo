package org.example.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.student.exception.BusinessException;
import org.example.student.mapper.UserMapper;
import org.example.student.model.PageResult;
import org.example.student.model.UserListItem;
import org.example.student.model.UserRecord;
import org.example.student.model.UserRequest;
import org.springframework.dao.DataAccessException;
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

    /**
     * 复杂列表查询保留 MyBatis XML，分页使用 PageHelper。
     */
    public PageResult<UserListItem> listUsers(Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(currentPage, currentSize);
        List<UserListItem> list = userMapper.listUsers();
        PageInfo<UserListItem> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), currentPage, currentSize);
    }

    /**
     * 新增用户使用 MyBatis-Plus insert。
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
        user.setEnabled(1);
        try {
            userMapper.insert(user);
        } catch (DataAccessException e) {
            throw new BusinessException(500, "新增用户失败，请稍后再试");
        }
        return user.getUserId();
    }

    /**
     * 修改用户使用 MyBatis-Plus update + Wrapper。
     */
    public void updateUser(String userId, UserRequest request) {
        UserRecord existing = findEnabledUser(userId);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }

        validateUser(request, false);
        checkUnique(request.getUsername(), request.getPhone(), userId);

        LambdaUpdateWrapper<UserRecord> updateWrapper = new LambdaUpdateWrapper<UserRecord>()
                .eq(UserRecord::getUserId, userId)
                .eq(UserRecord::getEnabled, 1)
                .set(UserRecord::getUsername, request.getUsername())
                .set(UserRecord::getPhone, request.getPhone())
                .set(UserRecord::getDisplayName, request.getDisplayName());
        if (StringUtils.hasText(request.getPassword())) {
            updateWrapper.set(UserRecord::getPassword, request.getPassword());
        }

        int rows;
        try {
            rows = userMapper.update(null, updateWrapper);
        } catch (DataAccessException e) {
            throw new BusinessException(500, "修改用户失败，请稍后再试");
        }
        if (rows == 0) {
            throw new BusinessException(404, "用户不存在");
        }
    }

    /**
     * 删除用户使用 MyBatis-Plus 软删除 update。
     */
    public void deleteUser(String userId) {
        int rows;
        try {
            rows = userMapper.update(null, new LambdaUpdateWrapper<UserRecord>()
                    .eq(UserRecord::getUserId, userId)
                    .eq(UserRecord::getEnabled, 1)
                    .set(UserRecord::getEnabled, 0));
        } catch (DataAccessException e) {
            throw new BusinessException(500, "删除用户失败，请稍后再试");
        }
        if (rows == 0) {
            throw new BusinessException(404, "用户不存在");
        }
    }

    private UserRecord findEnabledUser(String userId) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserRecord>()
                .eq(UserRecord::getUserId, userId)
                .eq(UserRecord::getEnabled, 1));
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
        LambdaQueryWrapper<UserRecord> usernameWrapper = new LambdaQueryWrapper<UserRecord>()
                .eq(UserRecord::getUsername, username);
        if (StringUtils.hasText(excludeUserId)) {
            usernameWrapper.ne(UserRecord::getUserId, excludeUserId);
        }
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new BusinessException(400, "账号已存在");
        }

        LambdaQueryWrapper<UserRecord> phoneWrapper = new LambdaQueryWrapper<UserRecord>()
                .eq(UserRecord::getPhone, phone);
        if (StringUtils.hasText(excludeUserId)) {
            phoneWrapper.ne(UserRecord::getUserId, excludeUserId);
        }
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new BusinessException(400, "手机号已存在");
        }
    }
}
