package org.example.student.model;

/**
 * sys_user 表记录。
 */
public class UserRecord {
    private String userId;
    private String username;
    private String password;
    private String phone;
    private String displayName;

    public UserInfo toUserInfo() {
        return new UserInfo(userId, username, phone, displayName);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
