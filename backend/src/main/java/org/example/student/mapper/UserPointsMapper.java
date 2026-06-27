package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.PointsRankingVO;
import org.example.student.model.UserPoints;

import java.util.List;

@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
    UserPoints findByUserId(@Param("userId") String userId);

    int insertUserPoints(UserPoints userPoints);

    int updatePointsWithVersion(@Param("userId") String userId,
                                @Param("pointsChange") Long pointsChange,
                                @Param("incomeDelta") Long incomeDelta,
                                @Param("usedDelta") Long usedDelta,
                                @Param("version") Integer version);

    List<PointsRankingVO> listRanking();

    PointsRankingVO findMyRanking(@Param("userId") String userId);
}
