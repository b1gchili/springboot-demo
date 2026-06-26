package org.example.student.controller;

import org.example.student.model.LoginLogItem;
import org.example.student.model.Result;
import org.example.student.service.LoginLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {
    private final LoginLogService loginLogService;

    public LoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @GetMapping
    public Result<List<LoginLogItem>> listLoginLogs() {
        return Result.success(loginLogService.listLoginLogs());
    }
}
