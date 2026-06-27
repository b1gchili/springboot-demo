package org.example.student.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 积分兑换优惠券请求参数。
 */
@Data
public class ExchangeCouponRequest {
    @NotNull(message = "优惠券模板ID不能为空")
    private Long couponTemplateId;

    @NotBlank(message = "请求号不能为空")
    private String requestNo;
}
