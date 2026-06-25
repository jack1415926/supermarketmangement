/**
 * Spring Security 配置类 —— 安全模块的核心配置。
 *
 * 负责：
 *   1. 配置 URL 访问权限（哪些 URL 需要认证，哪些公开）
 *   2. 注册 JWT 过滤器（在密码认证之前先检查 JWT）
 *   3. 配置密码编码器（BCrypt）
 *   4. 配置 CORS 和 CSRF 策略
 *   5. 设置会话管理为无状态（STATELESS）
 *
 * Spring Security 的过滤器链：
 *   请求 → CORS Filter → CSRF Filter → Auth Filter → ... → Controller
 *   我们的 JwtAuthFilter 插入到 UsernamePasswordAuthenticationFilter 之前。
 *
 * 类似 C++ 中的权限中间件配置。
 *
 * @author 殷智元
 */
package com.supermarket.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration          // 标记为 Spring 配置类
@EnableWebSecurity      // 启用 Spring Security（替代了旧版的 WebSecurityConfigurerAdapter）
@EnableMethodSecurity   // 启用方法级安全（@PreAuthorize 注解生效）
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * 安全过滤器链 —— Spring Security 的核心配置。
     *
     * SecurityFilterChain 定义了一组 Filter，Spring Boot 自动应用它们。
     *
     * @param http HttpSecurity 对象，用于配置安全规则
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ==================== CORS 配置 ====================
            // 使用已有的 CorsFilter Bean（WebConfig.java 中定义）而不是在这里重新配置
            .cors(cors -> {})
            // ==================== CSRF 配置 ====================
            // CSRF（跨站请求伪造）攻击在 Cookie-Session 模式下有风险。
            // 但 RESTful API 用 JWT Token（在 Header 中），不依赖 Cookie，
            // 所以 CSRF 攻击不适用，可以安全地禁用它。
            .csrf(csrf -> csrf.disable())
            // ==================== 会话管理 ====================
            // STATELESS：不创建 HTTP Session。每次请求都独立验证 JWT Token，服务器不保存任何会话状态。
            // 这是 RESTful API 的最佳实践。
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // ==================== URL 权限配置 ====================
            // 定义哪些 URL 无需认证即可访问。其他所有 URL 都需要认证（带 JWT Token）。
            .authorizeHttpRequests(auth -> auth
                // 公开接口：登录、健康检查、Swagger UI 文档
                .requestMatchers(
                    "/api/auth/login",
                    "/api/health",
                    "/swagger-ui/**",       // Swagger UI 页面
                    "/v3/api-docs/**"       // OpenAPI JSON 文档
                ).permitAll()
                // 其他所有 /api/** 接口都需要认证
                .requestMatchers("/api/**").authenticated()
                // 其他路径（静态资源等）允许匿名访问
                .anyRequest().permitAll()
            )
            // ==================== 添加 JWT 过滤器 ====================
            // 在 Spring Security 的用户名密码过滤器之前插入我们的 JWT 过滤器。
            // 这样请求会先检查 JWT Token，如果没有 Token 再走其他认证流程。
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器 —— BCrypt。
     *
     * BCryptPasswordEncoder 特点：
     *   1. 单向哈希（不可逆）
     *   2. 自动加盐（每次加密结果不同，即使密码相同）
     *   3. 慢速算法（故意用 CPU 密集型运算，增加暴力破解成本）
     *
     * 用法：
     *   String encoded = passwordEncoder.encode("123456");  // 加密
     *   boolean match = passwordEncoder.matches("123456", encoded); // 比对
     *
     * 类似 C++ 中的 crypt() 或 bcrypt 库。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器 —— 用于手动执行认证（登录时使用）。
     *
     * AuthenticationManager 是 Spring Security 的认证入口。
     * 调用 authenticate() 方法会触发 UserDetailsService.loadUserByUsername()
     * 和 PasswordEncoder.matches() 的验证流程。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
