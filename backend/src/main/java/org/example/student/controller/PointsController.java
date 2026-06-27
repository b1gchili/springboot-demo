package org.example.student.controller;

import org.example.student.exception.BusinessException;
import org.example.student.model.Result;
import org.example.student.model.SignInVO;
import org.example.student.service.PointsSignService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/points")
public class PointsController {
    private final PointsSignService pointsSignService;

    public PointsController(PointsSignService pointsSignService) {
        this.pointsSignService = pointsSignService;
    }

    /**
     * 每日签到获取积分。
     */
    @PostMapping("/sign-in")
    public Result<SignInVO> signIn(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return Result.success(pointsSignService.signIn(userId));
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
