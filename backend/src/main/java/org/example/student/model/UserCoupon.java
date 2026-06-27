package org.example.student.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券表。
 */
@Data
@TableName("user_coupon")
public class UserCoupon {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("coupon_no")
    private String couponNo;

    @TableField("user_id")
    private String userId;

    @TableField("template_code")
    private String templateCode;

    @TableField("coupon_name")
    private String couponName;

    @TableField("coupon_type")
    private String couponType;

    @TableField("face_value")
    private BigDecimal faceValue;

    @TableField("threshold_amount")
    private BigDecimal thresholdAmount;

    private String status;

    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @TableField("use_time")
    private LocalDateTime useTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
