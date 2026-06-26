package org.example.student.model;

/**
 * 登录用户基础信息。
 */
public class UserInfo {
    /** 用户唯一标识。 */
    private String userId;
    /** 登录账号。 */
    private String username;
    /** 绑定手机号。 */
    private String phone;
    /** 页面展示名称。 */
    private String displayName;

    public UserInfo() {
    }

    public UserInfo(String userId, String username, String phone, String displayName) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
