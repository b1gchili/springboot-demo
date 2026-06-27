package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.PointsSignRecord;

import java.time.LocalDate;

@Mapper
public interface PointsSignRecordMapper extends BaseMapper<PointsSignRecord> {
    int insertSignRecord(PointsSignRecord record);

    PointsSignRecord findByUserIdAndSignDate(@Param("userId") String userId,
                                             @Param("signDate") LocalDate signDate);
}
