package org.example.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 积分任务配置测试数据初始化。
 *
 * 该测试类用于给本地 MySQL 添加积分任务配置，方便前端“积分任务”模块联调。
 * SQL 使用 task_code 唯一索引做幂等更新，重复执行不会产生重复任务。
 */
@SpringBootTest
public class PointsTaskConfigSeedTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void addDemoPointsTaskConfigs() {
        // 完善个人资料：不可重复任务，同一个用户只能奖励一次。
        upsertTaskConfig(
                "COMPLETE_PROFILE",
                "完善个人资料",
                20,
                0,
                "ONCE",
                1,
                "用户首次完善个人资料后奖励积分"
        );

        // 首次下单：不可重复任务，后端会把 bizId 统一为 taskCode 防止重复奖励。
        upsertTaskConfig(
                "FIRST_ORDER",
                "首次下单",
                50,
                0,
                "ONCE",
                1,
                "用户完成首笔订单后奖励积分"
        );

        // 评价订单：可重复任务，但同一订单 ID 只能奖励一次。
        upsertTaskConfig(
                "COMMENT_ORDER",
                "评价订单",
                10,
                1,
                "UNLIMITED",
                10,
                "用户评价订单后奖励积分，同一订单只奖励一次"
        );

        // 分享应用：可重复任务，但同一分享业务 ID 只能奖励一次。
        upsertTaskConfig(
                "SHARE_APP",
                "分享应用",
                5,
                1,
                "UNLIMITED",
                10,
                "用户分享应用后奖励积分，同一分享记录只奖励一次"
        );
    }

    private void upsertTaskConfig(String taskCode,
                                  String taskName,
                                  Integer rewardPoints,
                                  Integer repeatable,
                                  String rewardLimitType,
                                  Integer dailyLimit,
                                  String description) {
        // enabled=1 表示任务启用，前端领取任务积分时后端会校验该状态。
        jdbcTemplate.update(
                "INSERT INTO points_task_config " +
                        "(task_code, task_name, reward_points, repeatable, reward_limit_type, daily_limit, enabled, description, create_time, update_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1, ?, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "task_name = VALUES(task_name), " +
                        "reward_points = VALUES(reward_points), " +
                        "repeatable = VALUES(repeatable), " +
                        "reward_limit_type = VALUES(reward_limit_type), " +
                        "daily_limit = VALUES(daily_limit), " +
                        "enabled = 1, " +
                        "description = VALUES(description), " +
                        "update_time = NOW()",
                taskCode,
                taskName,
                rewardPoints,
                repeatable,
                rewardLimitType,
                dailyLimit,
                description
        );
    }
}
