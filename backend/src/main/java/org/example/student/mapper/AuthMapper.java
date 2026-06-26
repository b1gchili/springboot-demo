package org.example.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.SmsCodeRecord;
import org.example.student.model.UserRecord;

import java.util.Date;

@Mapper
public interface AuthMapper {
    UserRecord findEnabledUserByUsername(@Param("username") String username);

    UserRecord findEnabledUserByPhone(@Param("phone") String phone);

    int updateLastLoginTime(@Param("userId") String userId);

    int insertSmsCode(@Param("phone") String phone,
                      @Param("code") String code,
                      @Param("expiresAt") Date expiresAt);

    int expireAvailableSmsCodes(@Param("phone") String phone);

    SmsCodeRecord findLatestAvailableSmsCode(@Param("phone") String phone);

    int markSmsCodeUsed(@Param("id") Long id);
}
