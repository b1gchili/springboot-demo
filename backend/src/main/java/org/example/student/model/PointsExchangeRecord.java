package org.example.student.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换记录表。
 */
@Data
@TableName("points_exchange_record")
public class PointsExchangeRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("exchange_no")
    private String exchangeNo;

    @TableField("request_no")
    private String requestNo;

    @TableField("user_id")
    private String userId;

    @TableField("template_code")
    private String templateCode;

    @TableField("coupon_no")
    private String couponNo;

    @TableField("used_points")
    private Long usedPoints;

    @TableField("exchange_status")
    private String exchangeStatus;

    @TableField("fail_reason")
    private String failReason;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
