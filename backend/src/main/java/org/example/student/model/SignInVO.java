package org.example.student.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * 每日签到结果展示对象。
 */
@Data
public class SignInVO {
    private String userId;
    private LocalDate signDate;
    private Long rewardPoints;
    private Integer continuousDays;
    private Long availablePoints;
}
