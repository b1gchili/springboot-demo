package org.example.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.student.model.UserListItem;
import org.example.student.model.UserRecord;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserRecord> {
    List<UserListItem> listUsers();
}
