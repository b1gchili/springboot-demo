package org.example.student.model;

import lombok.Data;

/**
 * 积分兑换优惠券结果展示对象。
 */
@Data
public class ExchangeCouponVO {
    private String exchangeNo;
    private String requestNo;
    private String userId;
    private Long userCouponId;
    private String templateCode;
    private String couponNo;
    private Long pointsCost;
    private Long usedPoints;
    private Long availablePoints;
    private String exchangeStatus;
}
