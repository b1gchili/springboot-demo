package org.example.student.controller;

import org.example.student.exception.BusinessException;
import org.example.student.model.CouponExchangeItemVO;
import org.example.student.model.ExchangeCouponRequest;
import org.example.student.model.ExchangeCouponVO;
import org.example.student.model.Result;
import org.example.student.service.PointsExchangeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/coupons/exchange")
public class CouponExchangeController {
    private final PointsExchangeService pointsExchangeService;

    public CouponExchangeController(PointsExchangeService pointsExchangeService) {
        this.pointsExchangeService = pointsExchangeService;
    }

    /**
     * 查询可兑换优惠券列表。
     */
    @GetMapping("/list")
    public Result<List<CouponExchangeItemVO>> listExchangeableCoupons() {
        return Result.success(pointsExchangeService.listExchangeableCoupons());
    }

    /**
     * 积分兑换优惠券。
     */
    @PostMapping
    public Result<ExchangeCouponVO> exchangeCoupon(@RequestBody @Valid ExchangeCouponRequest request,
                                                   HttpServletRequest httpServletRequest) {
        String userId = getCurrentUserId(httpServletRequest);
        return Result.success(pointsExchangeService.exchangeCoupon(userId, request));
    }

    private String getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return String.valueOf(userId);
    }
}
