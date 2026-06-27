package org.example.student.task;

import lombok.extern.slf4j.Slf4j;
import org.example.student.mapper.UserMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 用户登录次数统计任务。
 */
@Slf4j
@Component
public class LoginCountStatTask {
    private final UserMapper userMapper;

    public LoginCountStatTask(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 每分钟按登录日志重新统计一次用户登录次数。
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refreshLoginCountStats() {
        int rows = userMapper.refreshLoginCountStats();
        log.info("用户登录次数统计完成，更新用户数={}", rows);
    }
}
