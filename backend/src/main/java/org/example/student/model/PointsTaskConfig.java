package org.example.student.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分任务配置表。
 */
@Data
@TableName("points_task_config")
public class PointsTaskConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_code")
    private String taskCode;

    @TableField("task_name")
    private String taskName;

    @TableField("reward_points")
    private Long rewardPoints;

    @TableField("reward_limit_type")
    private String rewardLimitType;

    @TableField("daily_limit")
    private Integer dailyLimit;

    private Integer enabled;

    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
