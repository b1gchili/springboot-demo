package org.example.student.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 可兑换优惠券展示对象。
 */
@Data
public class CouponExchangeItemVO {
    private Long couponTemplateId;
    private String templateCode;
    private String couponName;
    private String templateName;
    private String couponType;
    private BigDecimal faceValue;
    private BigDecimal thresholdAmount;
    private Long pointsCost;
    private Long exchangePoints;
    private Integer stock;
    private Integer validDays;
}
