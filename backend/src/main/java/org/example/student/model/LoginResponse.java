package org.example.student.model;

/**
 * 登录成功响应。
 */
public class LoginResponse {
    /** JWT 字符串，前端需要保存并放入后续请求头。 */
    private String token;
    /** token 类型，固定为 Bearer。 */
    private String tokenType;
    /** token 剩余有效秒数。 */
    private Long expiresIn;
    /** refresh token 字符串，用于 access token 过期后无感知刷新。 */
    private String refreshToken;
    /** refresh token 剩余有效秒数。 */
    private Long refreshExpiresIn;
    /** 当前登录用户的基础信息。 */
    private UserInfo user;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tokenType, Long expiresIn, UserInfo user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public LoginResponse(String token, String tokenType, Long expiresIn, String refreshToken, Long refreshExpiresIn, UserInfo user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = refreshExpiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(Long refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}
