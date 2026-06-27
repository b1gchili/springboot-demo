package org.example.student.service;

import org.example.student.model.SignInVO;

/**
 * 积分签到服务。
 */
public interface PointsSignService {
    SignInVO signIn(String userId);

    default SignInVO signIn(Long userId) {
        return signIn(userId == null ? null : String.valueOf(userId));
    }
}
