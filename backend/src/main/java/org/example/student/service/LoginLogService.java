package org.example.student.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.student.mapper.LoginLogMapper;
import org.example.student.model.LoginLogItem;
import org.example.student.model.LoginLogRecord;
import org.example.student.model.PageResult;
import org.example.student.model.UserRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginLogService {
    private final LoginLogMapper loginLogMapper;

    public LoginLogService(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    public void recordLogin(UserRecord user, String loginIp) {
        LoginLogRecord record = new LoginLogRecord();
        record.setUserId(user.getUserId());
        record.setUsername(user.getUsername());
        record.setDisplayName(user.getDisplayName());
        record.setPhone(user.getPhone());
        record.setLoginIp(loginIp);
        loginLogMapper.insertLoginLog(record);
    }

    public PageResult<LoginLogItem> listLoginLogs(Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(currentPage, currentSize);
        List<LoginLogItem> list = loginLogMapper.listLoginLogs();
        PageInfo<LoginLogItem> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), currentPage, currentSize);
    }
}
