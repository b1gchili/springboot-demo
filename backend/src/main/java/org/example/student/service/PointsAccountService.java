package org.example.student.service;

import org.example.student.model.UserPoints;

/**
 * 积分账户服务。
 */
public interface PointsAccountService {
    UserPoints getOrCreateUserPoints(String userId);

    default UserPoints getOrCreateUserPoints(Long userId) {
        return getOrCreateUserPoints(userId == null ? null : String.valueOf(userId));
    }

    UserPoints changePoints(String userId,
                            Integer pointsChange,
                            String sourceType,
                            String sourceId,
                            String reason);

    default UserPoints changePoints(Long userId,
                                    Integer pointsChange,
                                    String sourceType,
                                    String sourceId,
                                    String reason) {
        return changePoints(userId == null ? null : String.valueOf(userId), pointsChange, sourceType, sourceId, reason);
    }

    UserPoints addPoints(String userId,
                         Integer points,
                         String sourceType,
                         String sourceId,
                         String reason);

    default UserPoints addPoints(Long userId,
                                 Integer points,
                                 String sourceType,
                                 String sourceId,
                                 String reason) {
        return addPoints(userId == null ? null : String.valueOf(userId), points, sourceType, sourceId, reason);
    }

    UserPoints deductPoints(String userId,
                            Integer points,
                            String sourceType,
                            String sourceId,
                            String reason);

    default UserPoints deductPoints(Long userId,
                                    Integer points,
                                    String sourceType,
                                    String sourceId,
                                    String reason) {
        return deductPoints(userId == null ? null : String.valueOf(userId), points, sourceType, sourceId, reason);
    }
}
