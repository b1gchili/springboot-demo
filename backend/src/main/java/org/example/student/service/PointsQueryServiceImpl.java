package org.example.student.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.student.mapper.PointsFlowMapper;
import org.example.student.mapper.UserPointsMapper;
import org.example.student.model.PageResult;
import org.example.student.model.PointsFlowVO;
import org.example.student.model.PointsRankingPageVO;
import org.example.student.model.PointsRankingVO;
import org.example.student.model.UserPoints;
import org.example.student.model.UserPointsVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分查询服务实现。
 */
@Service
public class PointsQueryServiceImpl implements PointsQueryService {
    private final PointsAccountService pointsAccountService;
    private final PointsFlowMapper pointsFlowMapper;
    private final UserPointsMapper userPointsMapper;

    public PointsQueryServiceImpl(PointsAccountService pointsAccountService,
                                  PointsFlowMapper pointsFlowMapper,
                                  UserPointsMapper userPointsMapper) {
        this.pointsAccountService = pointsAccountService;
        this.pointsFlowMapper = pointsFlowMapper;
        this.userPointsMapper = userPointsMapper;
    }

    @Override
    public UserPointsVO getMyPoints(Long userId) {
        UserPoints userPoints = pointsAccountService.getOrCreateUserPoints(userId);
        UserPointsVO vo = new UserPointsVO();
        vo.setUserId(userPoints.getUserId());
        vo.setAvailablePoints(defaultLong(userPoints.getAvailablePoints()));
        vo.setTotalEarnedPoints(defaultLong(userPoints.getTotalEarnedPoints()));
        vo.setTotalUsedPoints(defaultLong(userPoints.getTotalUsedPoints()));
        return vo;
    }

    @Override
    public PageResult<PointsFlowVO> listMyFlows(Long userId, Integer pageNum, Integer pageSize) {
        int currentPage = normalizePageNum(pageNum);
        int currentSize = normalizePageSize(pageSize, 10);
        PageHelper.startPage(currentPage, currentSize);
        List<PointsFlowVO> list = pointsFlowMapper.listByUserId(String.valueOf(userId));
        PageInfo<PointsFlowVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), currentPage, currentSize);
    }

    @Override
    public PointsRankingPageVO listRanking(Long userId, Integer pageNum, Integer pageSize) {
        int currentPage = normalizePageNum(pageNum);
        int currentSize = normalizePageSize(pageSize, 20);
        PageHelper.startPage(currentPage, currentSize);
        List<PointsRankingVO> list = userPointsMapper.listRanking();
        PageInfo<PointsRankingVO> pageInfo = new PageInfo<>(list);

        PointsRankingVO myRanking = userPointsMapper.findMyRanking(String.valueOf(userId));
        PointsRankingPageVO vo = new PointsRankingPageVO();
        vo.setPage(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), currentPage, currentSize));
        vo.setMyRank(myRanking == null ? null : myRanking.getRanking());
        return vo;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize, int defaultSize) {
        return pageSize == null || pageSize < 1 ? defaultSize : pageSize;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
