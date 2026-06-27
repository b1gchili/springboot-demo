package org.example.student.service;

import lombok.extern.slf4j.Slf4j;
import org.example.student.exception.BusinessException;
import org.example.student.mapper.PointsFlowMapper;
import org.example.student.mapper.UserPointsMapper;
import org.example.student.model.PointsFlow;
import org.example.student.model.PointsFlowTypeEnum;
import org.example.student.model.UserPoints;
import org.example.student.util.PointsFlowNoGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 积分账户核心服务，只负责账户增减和流水记录。
 */
@Slf4j
@Service
public class PointsAccountServiceImpl implements PointsAccountService {
    private static final int MAX_RETRY_TIMES = 3;

    private final UserPointsMapper userPointsMapper;
    private final PointsFlowMapper pointsFlowMapper;

    public PointsAccountServiceImpl(UserPointsMapper userPointsMapper, PointsFlowMapper pointsFlowMapper) {
        this.userPointsMapper = userPointsMapper;
        this.pointsFlowMapper = pointsFlowMapper;
    }

    /**
     * 获取用户积分账户；不存在则创建初始账户。
     *
     * user_points.user_id 有唯一索引，并发创建时如果插入冲突，重新查询已有账户。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPoints getOrCreateUserPoints(String userId) {
        String userIdValue = normalizeUserId(userId);
        UserPoints existing = userPointsMapper.findByUserId(userIdValue);
        if (existing != null) {
            return existing;
        }

        UserPoints initialAccount = new UserPoints();
        initialAccount.setUserId(userIdValue);
        initialAccount.setAvailablePoints(0L);
        initialAccount.setTotalEarnedPoints(0L);
        initialAccount.setTotalUsedPoints(0L);
        initialAccount.setVersion(0);

        try {
            userPointsMapper.insertUserPoints(initialAccount);
            log.info("创建用户积分账户: userId={}", userIdValue);
            return userPointsMapper.findByUserId(userIdValue);
        } catch (DuplicateKeyException e) {
            UserPoints concurrentCreated = userPointsMapper.findByUserId(userIdValue);
            if (concurrentCreated != null) {
                return concurrentCreated;
            }
            throw new BusinessException(500, "创建积分账户失败，请重试");
        }
    }

    /**
     * 变更积分并记录积分流水。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPoints changePoints(String userId,
                                   Integer pointsChange,
                                   String sourceType,
                                   String sourceId,
                                   String reason) {
        validateChangeRequest(userId, pointsChange, sourceType, reason);
        String userIdValue = normalizeUserId(userId);
        long change = pointsChange.longValue();

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            UserPoints current = getOrCreateUserPoints(userIdValue);
            long beforePoints = current.getAvailablePoints() == null ? 0L : current.getAvailablePoints();
            long afterPoints = beforePoints + change;
            if (afterPoints < 0) {
                throw new BusinessException(400, "积分不足");
            }

            long incomeDelta = change > 0 ? change : 0L;
            long usedDelta = change < 0 ? -change : 0L;
            int rows = userPointsMapper.updatePointsWithVersion(
                    userIdValue,
                    change,
                    incomeDelta,
                    usedDelta,
                    current.getVersion()
            );
            if (rows == 1) {
                insertPointsFlow(userIdValue, change, beforePoints, afterPoints, sourceType, sourceId, reason);
                log.info("积分变更成功: userId={}, pointsChange={}, before={}, after={}",
                        userIdValue, change, beforePoints, afterPoints);
                return userPointsMapper.findByUserId(userIdValue);
            }

            log.warn("积分乐观锁更新失败，准备重试: userId={}, retry={}", userIdValue, i + 1);
        }

        throw new BusinessException(500, "积分更新失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPoints addPoints(String userId,
                                Integer points,
                                String sourceType,
                                String sourceId,
                                String reason) {
        if (points == null || points <= 0) {
            throw new BusinessException(400, "增加积分必须大于0");
        }
        return changePoints(userId, points, sourceType, sourceId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPoints deductPoints(String userId,
                                   Integer points,
                                   String sourceType,
                                   String sourceId,
                                   String reason) {
        if (points == null || points <= 0) {
            throw new BusinessException(400, "扣减积分必须大于0");
        }
        return changePoints(userId, -points, sourceType, sourceId, reason);
    }

    private void insertPointsFlow(String userId,
                                  long pointsChange,
                                  long beforePoints,
                                  long afterPoints,
                                  String sourceType,
                                  String sourceId,
                                  String reason) {
        PointsFlow flow = new PointsFlow();
        flow.setFlowNo(PointsFlowNoGenerator.generate());
        flow.setUserId(userId);
        flow.setChangePoints(pointsChange);
        flow.setBeforePoints(beforePoints);
        flow.setAfterPoints(afterPoints);
        flow.setFlowType(pointsChange > 0 ? PointsFlowTypeEnum.INCOME.name() : PointsFlowTypeEnum.EXPENSE.name());
        flow.setSourceType(sourceType);
        flow.setSourceBizId(sourceId);
        flow.setReason(reason);
        pointsFlowMapper.insertPointsFlow(flow);
    }

    private void validateChangeRequest(String userId, Integer pointsChange, String sourceType, String reason) {
        normalizeUserId(userId);
        if (pointsChange == null || pointsChange == 0) {
            throw new BusinessException(400, "积分变动值不能为0");
        }
        if (!StringUtils.hasText(sourceType)) {
            throw new BusinessException(400, "积分来源类型不能为空");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(400, "积分变动原因不能为空");
        }
    }

    private String normalizeUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        return userId.trim();
    }
}
