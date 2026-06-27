package org.example.student.service;

import org.example.student.model.PageResult;
import org.example.student.model.PointsFlowVO;
import org.example.student.model.PointsRankingPageVO;
import org.example.student.model.UserPointsVO;

/**
 * 积分查询服务。
 */
public interface PointsQueryService {
    UserPointsVO getMyPoints(String userId);

    default UserPointsVO getMyPoints(Long userId) {
        return getMyPoints(userId == null ? null : String.valueOf(userId));
    }

    PageResult<PointsFlowVO> listMyFlows(String userId, Integer pageNum, Integer pageSize);

    default PageResult<PointsFlowVO> listMyFlows(Long userId, Integer pageNum, Integer pageSize) {
        return listMyFlows(userId == null ? null : String.valueOf(userId), pageNum, pageSize);
    }

    PointsRankingPageVO listRanking(String userId, Integer pageNum, Integer pageSize);

    default PointsRankingPageVO listRanking(Long userId, Integer pageNum, Integer pageSize) {
        return listRanking(userId == null ? null : String.valueOf(userId), pageNum, pageSize);
    }
}
