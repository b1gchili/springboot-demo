package org.example.student.model;

import lombok.Data;

/**
 * 用户积分账户展示对象。
 */
@Data
public class UserPointsVO {
    private String userId;
    private Long availablePoints;
    private Long totalEarnedPoints;
    private Long totalUsedPoints;
}
