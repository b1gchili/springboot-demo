package org.example.student.service;

import org.example.student.model.CompleteTaskRequest;
import org.example.student.model.TaskRewardVO;

/**
 * 积分任务奖励服务。
 */
public interface PointsTaskService {
    TaskRewardVO completeTask(String userId, CompleteTaskRequest request);

    default TaskRewardVO completeTask(Long userId, CompleteTaskRequest request) {
        return completeTask(userId == null ? null : String.valueOf(userId), request);
    }
}
