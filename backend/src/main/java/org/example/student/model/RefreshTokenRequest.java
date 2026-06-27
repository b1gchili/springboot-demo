package org.example.student.model;

import javax.validation.constraints.NotBlank;

/**
 * 刷新 access token 请求参数。
 */
public class RefreshTokenRequest {
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
