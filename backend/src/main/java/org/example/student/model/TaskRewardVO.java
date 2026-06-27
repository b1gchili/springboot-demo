package org.example.student.model;

import lombok.Data;

/**
 * 任务奖励结果展示对象。
 */
@Data
public class TaskRewardVO {
    private String userId;
    private String taskCode;
    private String bizId;
    private Long rewardPoints;
    private Long availablePoints;
    private Boolean rewarded;
}
