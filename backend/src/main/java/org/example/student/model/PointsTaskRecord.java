package org.example.student.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分任务奖励记录表。
 */
@Data
@TableName("points_task_record")
public class PointsTaskRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private String userId;

    @TableField("task_code")
    private String taskCode;

    @TableField("biz_id")
    private String bizId;

    @TableField("reward_points")
    private Long rewardPoints;

    @TableField("reward_status")
    private String rewardStatus;

    @TableField("create_time")
    private LocalDateTime createTime;
}
