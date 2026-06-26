package org.example.student.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 登录请求参数。
 */
public class LoginRequest {
    public interface PasswordLogin {}

    public interface SmsLogin {}

    /** 登录账号。 */
    @NotBlank(message = "账号不能为空", groups = PasswordLogin.class)
    private String username;

    /** 登录密码。 */
    @NotBlank(message = "密码不能为空", groups = PasswordLogin.class)
    private String password;

    /** 手机号。 */
    @NotBlank(message = "手机号不能为空", groups = SmsLogin.class)
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误", groups = SmsLogin.class)
    private String phone;

    /** 短信验证码。 */
    @NotBlank(message = "验证码不能为空", groups = SmsLogin.class)
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字", groups = SmsLogin.class)
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
