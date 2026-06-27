package org.example.student.service;

import org.example.student.model.CouponExchangeItemVO;
import org.example.student.model.ExchangeCouponRequest;
import org.example.student.model.ExchangeCouponVO;

import java.util.List;

/**
 * 积分兑换优惠券服务。
 */
public interface PointsExchangeService {
    List<CouponExchangeItemVO> listExchangeableCoupons();

    ExchangeCouponVO exchangeCoupon(String userId, ExchangeCouponRequest request);

    default ExchangeCouponVO exchangeCoupon(Long userId, ExchangeCouponRequest request) {
        return exchangeCoupon(userId == null ? null : String.valueOf(userId), request);
    }
}
