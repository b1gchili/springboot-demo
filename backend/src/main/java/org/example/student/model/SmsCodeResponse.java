package org.example.student.model;

/**
 * 获取短信验证码响应。
 *
 * 真实登录流程不应该把验证码明文返回给前端。
 */
public class SmsCodeResponse {
    /** 手机号。 */
    private String phone;
    /** 验证码有效秒数。 */
    private Long expiresIn;

    public SmsCodeResponse() {
    }

    public SmsCodeResponse(String phone, Long expiresIn) {
        this.phone = phone;
        this.expiresIn = expiresIn;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
