package org.example.student.controller;

import org.example.student.exception.BusinessException;
import org.example.student.model.CompleteTaskRequest;
import org.example.student.model.PageResult;
import org.example.student.model.PointsFlowVO;
import org.example.student.model.PointsRankingPageVO;
import org.example.student.model.Result;
import org.example.student.model.SignInVO;
import org.example.student.model.TaskRewardVO;
import org.example.student.model.UserPointsVO;
import org.example.student.service.PointsQueryService;
import org.example.student.service.PointsSignService;
import org.example.student.service.PointsTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/points")
public class PointsController {
    private final PointsSignService pointsSignService;
    private final PointsTaskService pointsTaskService;
    private final PointsQueryService pointsQueryService;

    public PointsController(PointsSignService pointsSignService,
                            PointsTaskService pointsTaskService,
                            PointsQueryService pointsQueryService) {
        this.pointsSignService = pointsSignService;
        this.pointsTaskService = pointsTaskService;
        this.pointsQueryService = pointsQueryService;
    }

    /**
     * 查询当前登录用户积分账户。
     */
    @GetMapping("/me")
    public Result<UserPointsVO> getMyPoints(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        return Result.success(pointsQueryService.getMyPoints(userId));
    }

    /**
     * 分页查询当前登录用户积分流水。
     */
    @GetMapping("/flows")
    public Result<PageResult<PointsFlowVO>> listMyFlows(@RequestParam(required = false) Integer pageNum,
                                                        @RequestParam(required = false) Integer pageSize,
                                                        HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        return Result.success(pointsQueryService.listMyFlows(userId, pageNum, pageSize));
    }

    /**
     * 分页查询积分排行榜，并返回当前用户排名。
     */
    @GetMapping("/ranking")
    public Result<PointsRankingPageVO> listRanking(@RequestParam(required = false) Integer pageNum,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        return Result.success(pointsQueryService.listRanking(userId, pageNum, pageSize));
    }

    /**
     * 每日签到获取积分。
     */
    @PostMapping("/sign-in")
    public Result<SignInVO> signIn(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        return Result.success(pointsSignService.signIn(userId));
    }

    /**
     * 完成指定任务获取积分。
     */
    @PostMapping("/tasks/complete")
    public Result<TaskRewardVO> completeTask(@RequestBody @Valid CompleteTaskRequest completeTaskRequest,
                                             HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        return Result.success(pointsTaskService.completeTask(userId, completeTaskRequest));
    }

    private String getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return String.valueOf(userId);
    }
}
