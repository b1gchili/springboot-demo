package org.example.student.service;

import org.example.student.exception.BusinessException;
import org.example.student.mapper.PointsSignRecordMapper;
import org.example.student.mapper.PointsTaskConfigMapper;
import org.example.student.model.PointsSignRecord;
import org.example.student.model.PointsSourceTypeEnum;
import org.example.student.model.PointsTaskConfig;
import org.example.student.model.SignInVO;
import org.example.student.model.UserPoints;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 每日签到积分服务。
 */
@Service
public class PointsSignServiceImpl implements PointsSignService {
    private static final String DAILY_SIGN_IN_TASK_CODE = "DAILY_SIGN_IN";
    private static final long DEFAULT_REWARD_POINTS = 10L;

    private final PointsSignRecordMapper pointsSignRecordMapper;
    private final PointsTaskConfigMapper pointsTaskConfigMapper;
    private final PointsAccountService pointsAccountService;

    public PointsSignServiceImpl(PointsSignRecordMapper pointsSignRecordMapper,
                                 PointsTaskConfigMapper pointsTaskConfigMapper,
                                 PointsAccountService pointsAccountService) {
        this.pointsSignRecordMapper = pointsSignRecordMapper;
        this.pointsTaskConfigMapper = pointsTaskConfigMapper;
        this.pointsAccountService = pointsAccountService;
    }

    /**
     * 每日签到：先写签到记录，再增加积分；两步在同一个事务中。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInVO signIn(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(400, "用户ID不能为空");
        }

        String userIdValue = userId.trim();
        LocalDate today = LocalDate.now();
        PointsTaskConfig taskConfig = pointsTaskConfigMapper.findByTaskCode(DAILY_SIGN_IN_TASK_CODE);
        if (taskConfig == null || taskConfig.getEnabled() == null || taskConfig.getEnabled() != 1) {
            throw new BusinessException(400, "签到任务未启用");
        }

        long rewardPoints = taskConfig.getRewardPoints() == null || taskConfig.getRewardPoints() <= 0
                ? DEFAULT_REWARD_POINTS
                : taskConfig.getRewardPoints();

        PointsSignRecord existing = pointsSignRecordMapper.findByUserIdAndSignDate(userIdValue, today);
        if (existing != null) {
            throw new BusinessException(400, "今日已签到");
        }

        PointsSignRecord record = new PointsSignRecord();
        record.setUserId(userIdValue);
        record.setSignDate(today);
        record.setRewardPoints(rewardPoints);
        record.setContinuousDays(1);
        try {
            pointsSignRecordMapper.insertSignRecord(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(400, "今日已签到");
        }

        UserPoints userPoints = pointsAccountService.addPoints(
                userId,
                Math.toIntExact(rewardPoints),
                PointsSourceTypeEnum.SIGN_IN.name(),
                today.toString(),
                "每日签到"
        );

        SignInVO vo = new SignInVO();
        vo.setUserId(userIdValue);
        vo.setSignDate(today);
        vo.setRewardPoints(rewardPoints);
        vo.setContinuousDays(record.getContinuousDays());
        vo.setAvailablePoints(userPoints.getAvailablePoints());
        return vo;
    }
}
