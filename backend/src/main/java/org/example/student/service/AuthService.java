package org.example.student.service;

import org.example.student.exception.BusinessException;
import org.example.student.mapper.AuthMapper;
import org.example.student.model.LoginRequest;
import org.example.student.model.LoginResponse;
import org.example.student.model.SmsCodeRecord;
import org.example.student.model.SmsCodeResponse;
import org.example.student.model.UserInfo;
import org.example.student.model.UserRecord;
import org.example.student.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Date;

/**
 * 登录认证服务。
 *
 * 用户和短信验证码都通过 MyBatis 访问 MySQL。
 * 当前项目不接入真实短信服务，验证码会输出到后端控制台日志。
 */
@Slf4j
@Service
public class AuthService {

    /** 验证码有效期：60 秒。 */
    private static final long SMS_CODE_TTL_MILLIS = 60 * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthMapper authMapper;
    private final LoginLogService loginLogService;

    public AuthService(AuthMapper authMapper, LoginLogService loginLogService) {
        this.authMapper = authMapper;
        this.loginLogService = loginLogService;
    }

    /**
     * 账号密码登录。
     */
    public LoginResponse loginByPassword(LoginRequest request, String loginIp) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(400, "账号和密码不能为空");
        }
        UserRecord user = authMapper.findEnabledUserByUsername(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        authMapper.updateLastLoginTime(user.getUserId());
        loginLogService.recordLogin(user, loginIp);
        return buildLoginResponse(user.toUserInfo());
    }

    /**
     * 生成手机验证码并写入 MySQL。
     *
     * 验证码不会返回给前端，会输出到后端控制台日志，便于本地演示。
     */
    public SmsCodeResponse sendSmsCode(String phone) {
        if (!isValidPhone(phone)) {
            throw new BusinessException(400, "手机号格式错误");
        }
        UserRecord user = authMapper.findEnabledUserByPhone(phone);
        if (user == null) {
            throw new BusinessException(404, "手机号未绑定用户");
        }

        SmsCodeRecord latestCode = authMapper.findLatestAvailableSmsCode(phone);
        if (latestCode != null && latestCode.getExpiresAt().after(new Date())) {
            long remainMillis = latestCode.getExpiresAt().getTime() - System.currentTimeMillis();
            long remainSeconds = Math.max(1, remainMillis / 1000);
            throw new BusinessException(429, "验证码已发送，请 " + remainSeconds + " 秒后再试");
        }

        authMapper.expireAvailableSmsCodes(phone);
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        Date expiresAt = new Date(System.currentTimeMillis() + SMS_CODE_TTL_MILLIS);
        authMapper.insertSmsCode(phone, code, expiresAt);

        log.info("手机验证码已生成: phone={}, code={}, expiresIn={}s", phone, code, 60);
        return new SmsCodeResponse(phone, 60L);
    }

    /**
     * 手机验证码登录。
     *
     * 验证码校验成功后标记为已使用，避免同一个验证码被重复使用。
     */
    public LoginResponse loginBySms(LoginRequest request, String loginIp) {
        if (!isValidPhone(request.getPhone())) {
            throw new BusinessException(400, "手机号格式错误");
        }
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(400, "验证码不能为空");
        }
        if (!request.getCode().matches("^\\d{6}$")) {
            throw new BusinessException(400, "验证码必须为6位数字");
        }

        SmsCodeRecord record = authMapper.findLatestAvailableSmsCode(request.getPhone());
        if (record == null) {
            throw new BusinessException(400, "请先获取验证码");
        }
        if (record.getExpiresAt().before(new Date())) {
            authMapper.markSmsCodeUsed(record.getId());
            throw new BusinessException(400, "验证码已过期");
        }
        if (!record.getCode().equals(request.getCode())) {
            throw new BusinessException(400, "验证码错误");
        }
        authMapper.markSmsCodeUsed(record.getId());

        UserRecord user = authMapper.findEnabledUserByPhone(request.getPhone());
        if (user == null) {
            throw new BusinessException(401, "手机号未绑定用户");
        }
        authMapper.updateLastLoginTime(user.getUserId());
        loginLogService.recordLogin(user, loginIp);
        return buildLoginResponse(user.toUserInfo());
    }

    /**
     * 使用 refresh token 刷新 access token。
     */
    public LoginResponse refreshToken(String refreshToken) {
        String userId = JwtUtil.getUserIdFromRefreshToken(refreshToken);
        UserRecord user = authMapper.findEnabledUserByUserId(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在或已被禁用，请重新登录");
        }
        return buildRefreshResponse(user.toUserInfo(), refreshToken);
    }

    /** 登录成功后统一生成 JWT 响应。 */
    private LoginResponse buildLoginResponse(UserInfo user) {
        return new LoginResponse(
                JwtUtil.createToken(user),
                "Bearer",
                JwtUtil.getExpiresInSeconds(),
                JwtUtil.createRefreshToken(user),
                JwtUtil.getRefreshExpiresInSeconds(),
                user
        );
    }

    /** 刷新时只替换 access token，refresh token 保持不变。 */
    private LoginResponse buildRefreshResponse(UserInfo user, String refreshToken) {
        return new LoginResponse(
                JwtUtil.createToken(user),
                "Bearer",
                JwtUtil.getExpiresInSeconds(),
                refreshToken,
                JwtUtil.getRefreshExpiresInSeconds(),
                user
        );
    }

    private boolean isValidPhone(String phone) {
        return StringUtils.hasText(phone) && phone.matches("^1\\d{10}$");
    }
}
