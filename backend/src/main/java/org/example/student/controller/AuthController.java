package org.example.student.controller;

import org.example.student.model.LoginRequest;
import org.example.student.model.LoginResponse;
import org.example.student.model.RefreshTokenRequest;
import org.example.student.model.Result;
import org.example.student.model.SmsCodeRequest;
import org.example.student.model.SmsCodeResponse;
import org.example.student.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 登录认证接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    /** 账号密码登录：校验账号密码，成功后返回 JWT。 */
    @PostMapping("/login/password")
    public Result<LoginResponse> loginByPassword(@RequestBody @Validated(LoginRequest.PasswordLogin.class) LoginRequest request,
                                                 HttpServletRequest httpRequest) {
        return Result.success(authService.loginByPassword(request, getClientIp(httpRequest)));
    }

    /** 获取手机验证码：验证码有效时间为 60 秒。 */
    @PostMapping("/sms/code")
    public Result<SmsCodeResponse> sendSmsCode(@RequestBody @Valid SmsCodeRequest request) {
        return Result.success(authService.sendSmsCode(request.getPhone()));
    }

    /** 手机验证码登录：校验手机号和验证码，成功后返回 JWT。 */
    @PostMapping("/login/sms")
    public Result<LoginResponse> loginBySms(@RequestBody @Validated(LoginRequest.SmsLogin.class) LoginRequest request,
                                            HttpServletRequest httpRequest) {
        return Result.success(authService.loginBySms(request, getClientIp(httpRequest)));
    }

    /** 无感知刷新 token：refresh token 有效时返回新的 access token。 */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return Result.success(authService.refreshToken(request.getRefreshToken()));
    }

    /**
     * 获取客户端 IP。
     *
     * 如果前面有 Nginx 或 devServer 代理，优先读取代理转发头。
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
