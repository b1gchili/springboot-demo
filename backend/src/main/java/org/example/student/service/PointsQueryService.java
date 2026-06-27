package org.example.student.service;

import org.example.student.model.PageResult;
import org.example.student.model.PointsFlowVO;
import org.example.student.model.PointsRankingPageVO;
import org.example.student.model.UserPointsVO;

/**
 * 积分查询服务。
 */
public interface PointsQueryService {
    UserPointsVO getMyPoints(Long userId);

    PageResult<PointsFlowVO> listMyFlows(Long userId, Integer pageNum, Integer pageSize);

    PointsRankingPageVO listRanking(Long userId, Integer pageNum, Integer pageSize);
}
