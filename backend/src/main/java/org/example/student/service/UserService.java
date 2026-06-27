package org.example.student.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.student.exception.BusinessException;
import org.example.student.mapper.UserMapper;
import org.example.student.model.PageResult;
import org.example.student.model.UserExportRow;
import org.example.student.model.UserListItem;
import org.example.student.model.UserRecord;
import org.example.student.model.UserRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final int ADD_USER_MAX_RETRY_TIMES = 3;

    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;

    public UserService(UserMapper userMapper, JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
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
     * 使用 EasyExcel 导出用户列表。
     */
    public void exportUsers(HttpServletResponse response) {
        List<UserExportRow> rows = userMapper.listUsers().stream()
                .map(UserExportRow::from)
                .collect(Collectors.toList());
        try {
            String fileName = URLEncoder.encode("用户列表.xlsx", "UTF-8").replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            EasyExcel.write(response.getOutputStream(), UserExportRow.class)
                    .sheet("用户列表")
                    .doWrite(rows);
        } catch (IOException e) {
            throw new BusinessException(500, "导出用户列表失败");
        }
    }

    /**
     * 新增用户使用 MyBatis-Plus insert。
     */
    public String addUser(UserRequest request) {
        validateUser(request, true);
        checkUnique(request.getUsername(), request.getPhone(), null);

        for (int i = 0; i < ADD_USER_MAX_RETRY_TIMES; i++) {
            UserRecord user = buildUserRecord(request);
            try {
                userMapper.insert(user);
                return user.getUserId();
            } catch (DuplicateKeyException e) {
                // 并发新增时如果 user_id 撞唯一索引，重新取最大数字 ID 后再试。
                if (i == ADD_USER_MAX_RETRY_TIMES - 1) {
                    throw new BusinessException(500, "新增用户失败，请稍后再试");
                }
            } catch (DataAccessException e) {
                throw new BusinessException(500, "新增用户失败，请稍后再试");
            }
        }
        throw new BusinessException(500, "新增用户失败，请稍后再试");
    }

    private UserRecord buildUserRecord(UserRequest request) {
        UserRecord user = new UserRecord();
        // 新增用户的业务 ID 改为数字递增，避免 UUID 影响展示和积分系统用户关联。
        user.setUserId(generateNextUserId());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setDisplayName(request.getDisplayName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setLoginCount(0L);
        user.setEnabled(1);
        return user;
    }

    private String generateNextUserId() {
        // sys_user.user_id 仍是业务 ID 字段；这里只统计纯数字 ID，兼容历史 UUID 数据。
        Long maxUserId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(CAST(user_id AS UNSIGNED)), 0) FROM sys_user WHERE user_id REGEXP '^[0-9]+$'",
                Long.class
        );
        return String.valueOf((maxUserId == null ? 0L : maxUserId) + 1);
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
                .set(UserRecord::getDisplayName, request.getDisplayName())
                .set(UserRecord::getAvatarUrl, request.getAvatarUrl());
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
        if (StringUtils.hasText(request.getAvatarUrl()) && request.getAvatarUrl().length() > 500) {
            throw new BusinessException(400, "头像地址不能超过500个字符");
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
