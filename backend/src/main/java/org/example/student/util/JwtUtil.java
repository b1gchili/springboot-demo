package org.example.student.util;

import org.example.student.exception.BusinessException;
import org.example.student.model.UserInfo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JWT 工具类。
 *
 * 当前项目没有引入第三方 JWT 依赖，这里用 JDK 自带的 HmacSHA256 完成
 * token 的签发和校验。生成格式仍然是标准 JWT：header.payload.signature。
 */
public class JwtUtil {
    /** 演示项目写死密钥；真实项目应放到配置文件或环境变量中。 */
    private static final String SECRET = "student-demo-jwt-secret-change-me";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    /** JWT 有效期：2 小时。 */
    private static final long EXPIRES_IN_SECONDS = 2 * 60 * 60L;

    private JwtUtil() {
    }

    public static long getExpiresInSeconds() {
        return EXPIRES_IN_SECONDS;
    }

    /**
     * 根据用户信息生成 JWT。
     *
     * payload 中保存用户标识、账号、手机号、展示名、签发时间和过期时间。
     */
    public static String createToken(UserInfo user) {
        long now = System.currentTimeMillis() / 1000;
        long exp = now + EXPIRES_IN_SECONDS;
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{"
                + "\"sub\":\"" + escape(user.getUserId()) + "\","
                + "\"username\":\"" + escape(user.getUsername()) + "\","
                + "\"phone\":\"" + escape(user.getPhone()) + "\","
                + "\"name\":\"" + escape(user.getDisplayName()) + "\","
                + "\"iat\":" + now + ","
                + "\"exp\":" + exp
                + "}";
        String unsignedToken = base64Url(header) + "." + base64Url(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    /**
     * 校验 JWT 是否有效。
     *
     * 校验点包括：格式是否正确、签名是否匹配、是否超过 exp 过期时间。
     */
    public static boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            return false;
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        long exp = readLongClaim(payload, "exp");
        return exp > System.currentTimeMillis() / 1000;
    }

    /**
     * 从有效 JWT 中读取用户名，供拦截器或业务代码识别当前登录用户。
     */
    public static String getUsername(String token) {
        if (!validateToken(token)) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return readStringClaim(payload, "username");
    }

    /** 对 header.payload 做 HMAC-SHA256 签名。 */
    private static String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException("JWT签名失败", e);
        }
    }

    private static String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static long readLongClaim(String payload, String claim) {
        String marker = "\"" + claim + "\":";
        int start = payload.indexOf(marker);
        if (start < 0) {
            return 0;
        }
        start += marker.length();
        int end = start;
        while (end < payload.length() && Character.isDigit(payload.charAt(end))) {
            end++;
        }
        return Long.parseLong(payload.substring(start, end));
    }

    private static String readStringClaim(String payload, String claim) {
        String marker = "\"" + claim + "\":\"";
        int start = payload.indexOf(marker);
        if (start < 0) {
            return "";
        }
        start += marker.length();
        int end = payload.indexOf("\"", start);
        return end < 0 ? "" : payload.substring(start, end);
    }

    /** 转义 payload 字符串字段，避免引号和反斜杠破坏 JSON 结构。 */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 固定时间比较，避免普通字符串比较因提前返回带来的时序差异。
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        int result = a.length() ^ b.length();
        for (int i = 0; i < Math.min(a.length(), b.length()); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
