package org.example.student.controller;

import org.example.student.exception.BusinessException;
import org.example.student.model.CompleteTaskRequest;
import org.example.student.model.Result;
import org.example.student.model.SignInVO;
import org.example.student.model.TaskRewardVO;
import org.example.student.service.PointsSignService;
import org.example.student.service.PointsTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/points")
public class PointsController {
    private final PointsSignService pointsSignService;
    private final PointsTaskService pointsTaskService;

    public PointsController(PointsSignService pointsSignService, PointsTaskService pointsTaskService) {
        this.pointsSignService = pointsSignService;
        this.pointsTaskService = pointsTaskService;
    }

    /**
     * 每日签到获取积分。
     */
    @PostMapping("/sign-in")
    public Result<SignInVO> signIn(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(pointsSignService.signIn(userId));
    }

    /**
     * 完成指定任务获取积分。
     */
    @PostMapping("/tasks/complete")
    public Result<TaskRewardVO> completeTask(@RequestBody @Valid CompleteTaskRequest completeTaskRequest,
                                             HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(pointsTaskService.completeTask(userId, completeTaskRequest));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        try {
            return Long.valueOf(String.valueOf(userId));
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "登录用户信息无效");
        }
    }
}
