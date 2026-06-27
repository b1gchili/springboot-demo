package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.PointsTaskConfig;

@Mapper
public interface PointsTaskConfigMapper extends BaseMapper<PointsTaskConfig> {
    PointsTaskConfig findByTaskCode(@Param("taskCode") String taskCode);
}
