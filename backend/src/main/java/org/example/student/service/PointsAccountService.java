package org.example.student.service;

import org.example.student.model.UserPoints;

/**
 * 积分账户服务。
 */
public interface PointsAccountService {
    UserPoints getOrCreateUserPoints(Long userId);

    UserPoints changePoints(Long userId,
                            Integer pointsChange,
                            String sourceType,
                            String sourceId,
                            String reason);

    UserPoints addPoints(Long userId,
                         Integer points,
                         String sourceType,
                         String sourceId,
                         String reason);

    UserPoints deductPoints(Long userId,
                            Integer points,
                            String sourceType,
                            String sourceId,
                            String reason);
}
