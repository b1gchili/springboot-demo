package org.example.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.student.model.LoginLogItem;
import org.example.student.model.LoginLogRecord;

import java.util.List;

@Mapper
public interface LoginLogMapper {
    int insertLoginLog(LoginLogRecord record);

    List<LoginLogItem> listLoginLogs();
}
