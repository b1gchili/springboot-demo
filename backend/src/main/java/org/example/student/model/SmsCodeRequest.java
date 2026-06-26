package org.example.student.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 获取短信验证码请求参数。
 */
public class SmsCodeRequest {
    /** 接收验证码的手机号。 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
