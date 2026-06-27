package org.example.student.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;

import java.util.Date;

/**
 * 用户列表导出 Excel 行数据。
 */
public class UserExportRow {
    @ExcelProperty("账号")
    private String username;

    @ExcelProperty("姓名")
    private String displayName;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("头像地址")
    private String avatarUrl;

    @ExcelProperty("登录次数")
    private Long loginCount;

    @ExcelProperty("最后登录时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    public static UserExportRow from(UserListItem item) {
        UserExportRow row = new UserExportRow();
        row.setUsername(item.getUsername());
        row.setDisplayName(item.getDisplayName());
        row.setPhone(item.getPhone());
        row.setAvatarUrl(item.getAvatarUrl());
        row.setLoginCount(item.getLoginCount());
        row.setLastLoginTime(item.getLastLoginTime());
        return row;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
