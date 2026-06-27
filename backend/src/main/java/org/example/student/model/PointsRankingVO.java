package org.example.student.model;

import lombok.Data;

/**
 * 积分排行榜展示对象。
 */
@Data
public class PointsRankingVO {
    private Integer ranking;
    private String userId;
    private String username;
    private String displayName;
    private Long availablePoints;
    private Long totalEarnedPoints;
}
