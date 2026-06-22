/**
 * 认证控制器 —— 处理登录、登出、获取当前用户。
 *
 * REST 端点：
 *   POST /api/auth/login  — 登录（公开接口）
 *   POST /api/auth/logout — 登出（需认证）
 *   GET  /api/auth/me     — 获取当前用户信息（需认证）
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.auth.LoginRequest;
import com.supermarket.dto.auth.LoginResponse;
import com.supermarket.entity.User;
import com.supermarket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 注入当前登录用户的 UserDetails
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // 所有端点的基础路径
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录。
     *
     * @Valid：触发 LoginRequest 中的 @NotBlank 校验。
     *   如果校验失败，抛出 MethodArgumentNotValidException，
     *   由 GlobalExceptionHandler 统一处理。
     *
     * @param request 登录请求体 { "username": "admin", "password": "admin123" }
     * @return { "code": 200, "message": "success", "data": { "token": "...", "username": "admin", ... } }
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 用户登出。
     *
     * JWT 是无状态的，无法在服务端"销毁"Token。
     * 实际的登出应该在前端清除本地存储的 Token。
     * 这里只是一个占位端点，返回成功提示。
     *
     * 如果需要严格登出，可以：
     *   1. 使用 Token 黑名单（Redis 缓存已注销的 Token）
     *   2. 设置较短的 Token 有效期（如 1 小时）
     *
     * @return 登出成功提示
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(); // 无数据返回，仅提示成功
    }

    /**
     * 获取当前登录用户信息。
     *
     * @AuthenticationPrincipal 是 Spring Security 的注解，
     *   自动从 SecurityContext 中注入当前登录用户。
     *   如果请求没有携带有效 Token，这个端点根本不会被执行
     *   （Spring Security 在过滤器层就返回了 401）。
     *
     * @param userDetails 当前登录用户（由 JwtAuthFilter 设置）
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 从数据库获取完整的用户信息
        User user = authService.getCurrentUser(userDetails.getUsername());

        // 构建返回数据（不包含密码）
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("displayName", user.getDisplayName());
        data.put("role", user.getRole());

        return Result.success(data);
    }
}
