package org.example.student.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板表。
 */
@Data
@TableName("coupon_template")
public class CouponTemplate {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("template_code")
    private String templateCode;

    @TableField("template_name")
    private String templateName;

    @TableField("coupon_type")
    private String couponType;

    @TableField("face_value")
    private BigDecimal faceValue;

    @TableField("threshold_amount")
    private BigDecimal thresholdAmount;

    @TableField("exchange_points")
    private Long exchangePoints;

    private Integer stock;

    @TableField("total_stock")
    private Integer totalStock;

    @TableField("valid_days")
    private Integer validDays;

    private Integer enabled;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
