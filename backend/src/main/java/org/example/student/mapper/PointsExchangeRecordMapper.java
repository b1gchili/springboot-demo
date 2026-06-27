package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.PointsExchangeRecord;

@Mapper
public interface PointsExchangeRecordMapper extends BaseMapper<PointsExchangeRecord> {
    PointsExchangeRecord findByUserIdAndRequestNo(@Param("userId") String userId,
                                                  @Param("requestNo") String requestNo);

    int insertExchangeRecord(PointsExchangeRecord record);
}
