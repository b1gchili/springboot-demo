package org.example.student.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 积分流水号生成工具。
 */
public class PointsFlowNoGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private PointsFlowNoGenerator() {
    }

    public static String generate() {
        return "PF" + LocalDateTime.now().format(FORMATTER)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
