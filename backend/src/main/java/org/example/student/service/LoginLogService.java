package org.example.student.service;

import org.example.student.mapper.LoginLogMapper;
import org.example.student.model.LoginLogItem;
import org.example.student.model.LoginLogRecord;
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

    public List<LoginLogItem> listLoginLogs() {
        return loginLogMapper.listLoginLogs();
    }
}
