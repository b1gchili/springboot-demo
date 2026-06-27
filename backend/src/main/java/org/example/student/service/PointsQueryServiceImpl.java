package org.example.student.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.student.exception.BusinessException;
import org.example.student.mapper.PointsFlowMapper;
import org.example.student.mapper.UserPointsMapper;
import org.example.student.model.PageResult;
import org.example.student.model.PointsFlowVO;
import org.example.student.model.PointsRankingPageVO;
import org.example.student.model.PointsRankingVO;
import org.example.student.model.UserPoints;
import org.example.student.model.UserPointsVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public UserPointsVO getMyPoints(String userId) {
        String userIdValue = normalizeUserId(userId);
        UserPoints userPoints = pointsAccountService.getOrCreateUserPoints(userIdValue);
        UserPointsVO vo = new UserPointsVO();
        vo.setUserId(userPoints.getUserId());
        vo.setAvailablePoints(defaultLong(userPoints.getAvailablePoints()));
        vo.setTotalEarnedPoints(defaultLong(userPoints.getTotalEarnedPoints()));
        vo.setTotalUsedPoints(defaultLong(userPoints.getTotalUsedPoints()));
        return vo;
    }

    @Override
    public PageResult<PointsFlowVO> listMyFlows(String userId, Integer pageNum, Integer pageSize) {
        String userIdValue = normalizeUserId(userId);
        int currentPage = normalizePageNum(pageNum);
        int currentSize = normalizePageSize(pageSize, 10);
        PageHelper.startPage(currentPage, currentSize);
        List<PointsFlowVO> list = pointsFlowMapper.listByUserId(userIdValue);
        PageInfo<PointsFlowVO> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), currentPage, currentSize);
    }

    @Override
    public PointsRankingPageVO listRanking(String userId, Integer pageNum, Integer pageSize) {
        String userIdValue = normalizeUserId(userId);
        int currentPage = normalizePageNum(pageNum);
        int currentSize = normalizePageSize(pageSize, 20);
        PageHelper.startPage(currentPage, currentSize);
        List<PointsRankingVO> list = userPointsMapper.listRanking();
        PageInfo<PointsRankingVO> pageInfo = new PageInfo<>(list);

        PointsRankingVO myRanking = userPointsMapper.findMyRanking(userIdValue);
        PointsRankingPageVO vo = new PointsRankingPageVO();
        vo.setPage(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), currentPage, currentSize));
        vo.setMyRank(myRanking == null ? null : myRanking.getRanking());
        return vo;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private String normalizeUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        return userId.trim();
    }

    private int normalizePageSize(Integer pageSize, int defaultSize) {
        return pageSize == null || pageSize < 1 ? defaultSize : pageSize;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
