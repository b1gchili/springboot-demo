package org.example.student.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

/**
 * 登录日志列表展示项。
 */
public class LoginLogItem {
    /** 账号。 */
    private String username;
    /** 姓名。 */
    private String displayName;
    /** 手机号。 */
    private String phone;
    /** 登录 IP。 */
    private String loginIp;
    /** 登录时间。 */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

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

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
}
