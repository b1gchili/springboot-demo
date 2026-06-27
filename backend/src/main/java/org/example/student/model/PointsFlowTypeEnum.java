package org.example.student.model;

/**
 * 积分流水类型。
 */
public enum PointsFlowTypeEnum {
    INCOME("收入"),
    EXPENSE("支出");

    private final String description;

    PointsFlowTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
