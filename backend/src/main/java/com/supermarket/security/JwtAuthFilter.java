/**
 * JWT 认证过滤器 —— 拦截每个 HTTP 请求，检查并验证 JWT Token。
 *
 * Spring Security 的过滤器链：请求 → Filter1 → Filter2 → ... → Controller
 * 这个过滤器加在 UsernamePasswordAuthenticationFilter 之前。
 *
 * 工作流程：
 *   1. 从请求的 Authorization header 中提取 Token
 *   2. 验证 Token 是否有效
 *   3. 如果有效，从 Token 中提取用户信息，设置到 SecurityContext 中
 *   4. 后续的 Controller 和 @PreAuthorize 就能获取当前用户了
 *
 * OncePerRequestFilter：
 *   保证每个 HTTP 请求只执行一次。
 *   即使请求被转发（forward）或包含（include），也不会重复执行。
 *
 * 类似 C++ 中的中间件/拦截器模式：每个请求进来先检查 token。
 *
 * @author 徐磊
 */
package com.supermarket.security;

import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import jakarta.servlet.FilterChain;           // 过滤器链
import jakarta.servlet.ServletException;     // Servlet 异常
import jakarta.servlet.http.HttpServletRequest;  // HTTP 请求
import jakarta.servlet.http.HttpServletResponse; // HTTP 响应
import lombok.RequiredArgsConstructor;       // 生成以 final 字段为参数的构造函数
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Spring Security 认证令牌
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 权限对象
import org.springframework.security.core.context.SecurityContextHolder; // 安全上下文持有者
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // 字符串工具（hasText 判空等）
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor // Lombok 注解：为所有 final 字段生成构造函数（Spring 自动注入）
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;           // JWT 工具
    private final UserRepository userRepository; // 用户查询

    /**
     * 过滤器的核心方法。
     * 每个 HTTP 请求都会经过这里。
     *
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param filterChain 过滤器链（处理完后调用 chain.doFilter 放行）
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: 从请求头中提取 Token
        String token = extractTokenFromRequest(request);

        // Step 2: 如果有 Token 且有效，设置认证信息
        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            setAuthentication(token);
        }

        // Step 3: 放行，继续处理请求
        // 即使没有 Token，也让请求继续（由 Spring Security 的 URL 权限配置决定是否拒绝）
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT Token。
     *
     * 格式：Authorization: Bearer <token>
     *
     * @param request HTTP 请求
     * @return Token 字符串，如果没有则返回 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 获取 Authorization 请求头的值
        String bearerToken = request.getHeader("Authorization");

        // 检查：不能为空，且必须以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 去掉 "Bearer " 前缀（7 个字符），只保留 Token
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * 设置 Spring Security 认证上下文。
     *
     * 把从 JWT 中提取的用户信息设置到 SecurityContextHolder 中。
     * 设置后，后续的：
     *   - Controller 可以通过 @AuthenticationPrincipal 获取当前用户
     *   - @PreAuthorize 可以根据角色判断权限
     *   - SecurityContextHolder.getContext().getAuthentication() 获取认证信息
     *
     * @param token 有效的 JWT Token
     */
    private void setAuthentication(String token) {
        // 从 Token 中提取用户名和角色
        String username = jwtUtils.getUsernameFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);

        // 从数据库查询用户（确保用户仍然存在且未被禁用）
        // 使用 orElse(null) 是因为用户可能已被删除
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getStatus() == 0) {
            return; // 用户不存在或已禁用，不设置认证
        }

        // 创建权限列表
        // SimpleGrantedAuthority 是 Spring Security 的权限表示类
        // 角色必须以 "ROLE_" 开头（Spring Security 的约定）
        // 例如：ROLE_CASHIER, ROLE_MANAGER, ROLE_ADMIN
        var authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        // 创建认证令牌
        // UsernamePasswordAuthenticationToken 是 Spring Security 的标准认证对象
        // 参数：(主体=用户对象, 凭证=null(Token已校验), 权限列表)
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user, null, authorities);

        // 将认证信息设置到安全上下文
        // SecurityContextHolder 使用 ThreadLocal 存储，每个请求线程独立
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
