package org.example.student.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * 这里把 AuthInterceptor 注册到 Spring MVC，请求进入 Controller 前会先经过它。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 保护所有后端 API。
                .addPathPatterns("/api/**")
                // 登录、发验证码接口必须放行，否则无法获取 token。
                .excludePathPatterns("/api/auth/**");
    }
}
