package org.example.student.model;

/**
 * 新增/修改用户请求参数。
 */
public class UserRequest {
    /** 账号。 */
    private String username;
    /** 姓名。 */
    private String displayName;
    /** 手机号。 */
    private String phone;
    /** 密码。新增时必填，修改时可选填。 */
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
