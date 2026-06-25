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
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    /**
     * 过滤器的核心方法。
     * 每个 HTTP 请求都会经过这里。
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
        try {
            if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
                setAuthentication(token);
            }
        } catch (Exception e) {
            // 认证失败不阻塞请求，让后续的 Spring Security URL 权限配置决定是否拒绝
        }

        // Step 3: 放行，继续处理请求
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT Token。
     *
     * 格式：Authorization: Bearer <token>
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
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
     */
    private void setAuthentication(String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || Integer.valueOf(0).equals(user.getStatus())) {
            return;
        }

        var authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        // 将 User 实体转换为 Spring Security 标准的 UserDetails 对象
        // 这样 Controller 中 @AuthenticationPrincipal UserDetails 才能正确注入
        var userDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Integer.valueOf(1).equals(user.getStatus()),
            true, true, true,
            authorities
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
