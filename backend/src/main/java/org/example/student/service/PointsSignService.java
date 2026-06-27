package org.example.student.service;

import org.example.student.model.SignInVO;

/**
 * 积分签到服务。
 */
public interface PointsSignService {
    SignInVO signIn(Long userId);
}
