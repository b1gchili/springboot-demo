package org.example.student.model;

/**
 * 获取短信验证码请求参数。
 */
public class SmsCodeRequest {
    /** 接收验证码的手机号。 */
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
