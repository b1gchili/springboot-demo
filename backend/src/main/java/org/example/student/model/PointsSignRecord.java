package org.example.student.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日签到记录表。
 */
@Data
@TableName("points_sign_record")
public class PointsSignRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private String userId;

    @TableField("sign_date")
    private LocalDate signDate;

    @TableField("reward_points")
    private Long rewardPoints;

    @TableField("continuous_days")
    private Integer continuousDays;

    @TableField("create_time")
    private LocalDateTime createTime;
}
