/**
 * Web 配置 —— CORS 跨域配置。
 *
 * 重要安全说明：
 *   setAllowCredentials(true) 与 addAllowedOriginPattern("*") 不能同时使用。
 *   如果允许任意来源携带凭证（如 JWT Token），恶意网站可以冒充用户发起请求。
 *   因此这里只允许明确的前端地址，不允许通配符。
 *
 * @author 徐磊
 */
package com.supermarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // 只允许明确的前端地址，不使用通配符 "*"。
        // 原因：携带凭证（credentials）时使用通配符会导致严重的安全漏洞，
        // 任何外部网站都能读取和操作本系统的 API 数据。
        // 生产环境部署时应将 localhost 地址替换为实际域名。
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:5173",   // Vite 前端开发服务器
            "http://localhost:8080"    // 后端本地调试（Swagger 等）
        ));

        config.addAllowedHeader("*");   // 允许所有请求头
        config.addAllowedMethod("*");   // 允许所有 HTTP 方法（GET/POST/PUT/DELETE 等）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
