package org.example.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.UserListItem;
import org.example.student.model.UserRecord;

import java.util.List;

@Mapper
public interface UserMapper {
    List<UserListItem> listUsers();

    UserRecord findByUserId(@Param("userId") String userId);

    int countByUsername(@Param("username") String username, @Param("excludeUserId") String excludeUserId);

    int countByPhone(@Param("phone") String phone, @Param("excludeUserId") String excludeUserId);

    int insertUser(UserRecord user);

    int updateUser(UserRecord user);

    int disableUser(@Param("userId") String userId);
}
