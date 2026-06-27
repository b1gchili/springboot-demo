package org.example.student.service;

import org.example.student.exception.BusinessException;
import org.example.student.mapper.PointsTaskConfigMapper;
import org.example.student.mapper.PointsTaskRecordMapper;
import org.example.student.model.CompleteTaskRequest;
import org.example.student.model.PointsSourceTypeEnum;
import org.example.student.model.PointsTaskConfig;
import org.example.student.model.PointsTaskRecord;
import org.example.student.model.TaskRewardVO;
import org.example.student.model.UserPoints;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 积分任务奖励服务实现。
 */
@Service
public class PointsTaskServiceImpl implements PointsTaskService {
    private final PointsTaskConfigMapper pointsTaskConfigMapper;
    private final PointsTaskRecordMapper pointsTaskRecordMapper;
    private final PointsAccountService pointsAccountService;

    public PointsTaskServiceImpl(PointsTaskConfigMapper pointsTaskConfigMapper,
                                 PointsTaskRecordMapper pointsTaskRecordMapper,
                                 PointsAccountService pointsAccountService) {
        this.pointsTaskConfigMapper = pointsTaskConfigMapper;
        this.pointsTaskRecordMapper = pointsTaskRecordMapper;
        this.pointsAccountService = pointsAccountService;
    }

    /**
     * 完成指定任务并发放积分。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskRewardVO completeTask(String userId, CompleteTaskRequest request) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        if (request == null || !StringUtils.hasText(request.getTaskCode())) {
            throw new BusinessException(400, "任务编码不能为空");
        }

        PointsTaskConfig taskConfig = pointsTaskConfigMapper.findByTaskCode(request.getTaskCode());
        if (taskConfig == null || taskConfig.getEnabled() == null || taskConfig.getEnabled() != 1) {
            throw new BusinessException(400, "任务不存在或已禁用");
        }

        boolean repeatable = taskConfig.getRepeatable() != null && taskConfig.getRepeatable() == 1;
        String bizId = repeatable ? request.getBizId() : taskConfig.getTaskCode();
        if (repeatable && !StringUtils.hasText(bizId)) {
            throw new BusinessException(400, "业务ID不能为空");
        }

        String userIdValue = userId.trim();
        PointsTaskRecord existing = pointsTaskRecordMapper.findByUserIdAndTaskCodeAndBizId(
                userIdValue,
                taskConfig.getTaskCode(),
                bizId
        );
        if (existing != null) {
            throw new BusinessException(400, "该任务已奖励过积分");
        }

        long rewardPoints = taskConfig.getRewardPoints() == null ? 0L : taskConfig.getRewardPoints();
        PointsTaskRecord record = new PointsTaskRecord();
        record.setUserId(userIdValue);
        record.setTaskCode(taskConfig.getTaskCode());
        record.setBizId(bizId);
        record.setRewardPoints(rewardPoints);
        record.setRewardStatus("SUCCESS");
        try {
            pointsTaskRecordMapper.insertTaskRecord(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(400, "该任务已奖励过积分");
        }

        UserPoints userPoints = pointsAccountService.addPoints(
                userIdValue,
                Math.toIntExact(rewardPoints),
                PointsSourceTypeEnum.TASK.name(),
                taskConfig.getTaskCode() + ":" + bizId,
                taskConfig.getTaskName()
        );

        TaskRewardVO vo = new TaskRewardVO();
        vo.setUserId(userIdValue);
        vo.setTaskCode(taskConfig.getTaskCode());
        vo.setBizId(bizId);
        vo.setRewardPoints(rewardPoints);
        vo.setAvailablePoints(userPoints.getAvailablePoints());
        vo.setRewarded(true);
        return vo;
    }
}
