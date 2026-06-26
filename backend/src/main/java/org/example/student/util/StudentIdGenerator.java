package org.example.student.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

// 学号生成器
public class StudentIdGenerator {

    private static final String PREFIX = "STU";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    /**
     * 生成学号，格式：STU + 时间戳 + 3位序号。
     *
     * 旧实现只精确到秒，同一秒新增多名学生时会和 MySQL 唯一索引冲突。
     */
    public static synchronized String generate() {
        int sequence = SEQUENCE.updateAndGet(value -> value >= 999 ? 1 : value + 1);
        return PREFIX + DATE_FORMAT.format(new Date()) + String.format("%03d", sequence);
    }
}
