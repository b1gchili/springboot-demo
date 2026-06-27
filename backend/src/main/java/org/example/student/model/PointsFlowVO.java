package org.example.student.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水展示对象。
 */
@Data
public class PointsFlowVO {
    private String flowNo;
    private String userId;
    private Long changePoints;
    private Long beforePoints;
    private Long afterPoints;
    private String flowType;
    private String sourceType;
    private String sourceBizId;
    private String reason;
    private LocalDateTime createTime;
}
