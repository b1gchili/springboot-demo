package org.example.student.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水表。
 */
@Data
@TableName("points_flow")
public class PointsFlow {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("flow_no")
    private String flowNo;

    @TableField("user_id")
    private String userId;

    @TableField("change_points")
    private Long changePoints;

    @TableField("before_points")
    private Long beforePoints;

    @TableField("after_points")
    private Long afterPoints;

    @TableField("flow_type")
    private String flowType;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_biz_id")
    private String sourceBizId;

    private String reason;

    @TableField("create_time")
    private LocalDateTime createTime;
}
