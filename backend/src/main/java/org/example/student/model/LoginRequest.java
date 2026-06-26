package org.example.student.model;

/**
 * 登录请求参数。
 *
 * 账号密码登录使用 username/password，手机验证码登录使用 phone/code。
 */
public class LoginRequest {
    /** 登录账号。 */
    private String username;
    /** 登录密码。 */
    private String password;
    /** 手机号。 */
    private String phone;
    /** 短信验证码。 */
    private String code;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
