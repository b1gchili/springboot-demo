package org.example.student.config;

import org.example.student.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * JWT 登录拦截器。
 *
 * 前端请求业务接口时，需要在请求头中携带：
 * Authorization: Bearer <token>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行浏览器预检请求，避免跨域/代理场景下 OPTIONS 被误拦截。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从 Authorization 请求头中解析 Bearer token。
        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        // token 有效则放行，并把用户名放到 request 中，后续业务可按需读取。
        if (JwtUtil.validateAccessToken(token)) {
            request.setAttribute("username", JwtUtil.getUsername(token));
            return true;
        }

        // token 缺失、签名错误或已过期时，直接返回 401。
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
        return false;
    }
}
