package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.PointsFlow;
import org.example.student.model.PointsFlowVO;

import java.util.List;

@Mapper
public interface PointsFlowMapper extends BaseMapper<PointsFlow> {
    int insertPointsFlow(PointsFlow pointsFlow);

    List<PointsFlowVO> listByUserId(@Param("userId") String userId);
}
