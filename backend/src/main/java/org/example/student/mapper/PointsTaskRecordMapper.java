package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.PointsTaskRecord;

@Mapper
public interface PointsTaskRecordMapper extends BaseMapper<PointsTaskRecord> {
    int insertTaskRecord(PointsTaskRecord record);

    PointsTaskRecord findByUserIdAndTaskCodeAndBizId(@Param("userId") String userId,
                                                     @Param("taskCode") String taskCode,
                                                     @Param("bizId") String bizId);
}
