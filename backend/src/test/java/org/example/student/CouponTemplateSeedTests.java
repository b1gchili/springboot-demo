package org.example.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

/**
 * 优惠券模板测试数据初始化。
 *
 * 该测试类用于给本地 MySQL 添加几条积分商城可兑换优惠券，方便前端联调积分兑换功能。
 * 插入 SQL 使用 template_code 唯一索引做幂等更新，重复执行不会产生重复模板。
 */
@SpringBootTest
public class CouponTemplateSeedTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void addDemoCouponTemplates() {
        // 满减券：适合验证低积分兑换、库存展示和兑换后扣库存。
        upsertCouponTemplate(
                "POINTS_COUPON_5",
                "5元无门槛券",
                "AMOUNT",
                new BigDecimal("5.00"),
                BigDecimal.ZERO,
                50L,
                30,
                30,
                15
        );

        // 满减券：适合验证较高积分消耗和有效期展示。
        upsertCouponTemplate(
                "POINTS_COUPON_20_100",
                "满100减20券",
                "AMOUNT",
                new BigDecimal("20.00"),
                new BigDecimal("100.00"),
                200L,
                20,
                20,
                30
        );

        // 折扣券：适合验证不同 coupon_type 下列表仍能正常展示。
        upsertCouponTemplate(
                "POINTS_COUPON_90_PERCENT",
                "9折优惠券",
                "DISCOUNT",
                new BigDecimal("0.90"),
                new BigDecimal("50.00"),
                120L,
                25,
                25,
                20
        );
    }

    private void upsertCouponTemplate(String templateCode,
                                      String templateName,
                                      String couponType,
                                      BigDecimal faceValue,
                                      BigDecimal thresholdAmount,
                                      Long exchangePoints,
                                      Integer stock,
                                      Integer totalStock,
                                      Integer validDays) {
        // enabled=1 表示上架，前端 GET /api/coupons/exchange/list 只会展示上架且有库存的模板。
        jdbcTemplate.update(
                "INSERT INTO coupon_template " +
                        "(template_code, template_name, coupon_type, face_value, threshold_amount, " +
                        "exchange_points, stock, total_stock, valid_days, enabled, create_time, update_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "template_name = VALUES(template_name), " +
                        "coupon_type = VALUES(coupon_type), " +
                        "face_value = VALUES(face_value), " +
                        "threshold_amount = VALUES(threshold_amount), " +
                        "exchange_points = VALUES(exchange_points), " +
                        "stock = VALUES(stock), " +
                        "total_stock = VALUES(total_stock), " +
                        "valid_days = VALUES(valid_days), " +
                        "enabled = 1, " +
                        "update_time = NOW()",
                templateCode,
                templateName,
                couponType,
                faceValue,
                thresholdAmount,
                exchangePoints,
                stock,
                totalStock,
                validDays
        );
    }
}
