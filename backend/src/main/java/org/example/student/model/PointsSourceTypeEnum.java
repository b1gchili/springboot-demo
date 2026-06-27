package org.example.student.model;

/**
 * 积分来源类型。
 */
public enum PointsSourceTypeEnum {
    SIGN_IN("每日签到"),
    TASK("任务奖励"),
    EXCHANGE("兑换优惠券"),
    ADMIN("后台调整");

    private final String description;

    PointsSourceTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
