package org.example.student.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 优惠券和兑换单号生成工具。
 */
public class CouponNoGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private CouponNoGenerator() {
    }

    public static String generateCouponNo() {
        return generate("UC");
    }

    public static String generateExchangeNo() {
        return generate("EX");
    }

    private static String generate(String prefix) {
        return prefix + LocalDateTime.now().format(FORMATTER)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
