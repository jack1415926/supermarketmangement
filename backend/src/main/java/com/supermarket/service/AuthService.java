/**
 * 认证服务 —— 处理登录、获取当前用户等业务逻辑。
 *
 * Service 层介于 Controller 和 Repository 之间：
 *   Controller → Service → Repository → Database
 *
 * 职责：
 *   1. 调用 AuthenticationManager 进行认证
 *   2. 认证成功后生成 JWT Token
 *   3. 返回用户信息和 Token
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.auth.LoginRequest;
import com.supermarket.dto.auth.LoginResponse;
import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import com.supermarket.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    /**
     * 用户登录。
     *
     * 流程：
     *   1. 用用户名和密码创建认证令牌
     *   2. 交给 AuthenticationManager 认证
     *      - 内部会调用 CustomUserDetailsService.loadUserByUsername() 加载用户
     *      - 然后自动用 BCryptPasswordEncoder 比对密码
     *   3. 认证成功 → 生成 JWT
     *   4. 返回用户信息和 Token
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 登录响应（Token + 用户信息）
     * @throws BadCredentialsException 用户名或密码错误
     */
    public LoginResponse login(LoginRequest request) {
        // Step 1: 创建认证令牌
        // UsernamePasswordAuthenticationToken 是 Spring Security 的"未认证"令牌
        Authentication authToken = new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        );

        // Step 2: 执行认证
        // 如果失败会抛出 BadCredentialsException，被 GlobalExceptionHandler 捕获
        Authentication authenticated = authenticationManager.authenticate(authToken);

        // Step 3: 认证成功，查询用户信息
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("认证成功但找不到用户，系统异常"));

        // 检查账号是否被禁用
        if (user.getStatus() == 0) {
            throw new BadCredentialsException("账号已被禁用，请联系管理员");
        }

        // Step 4: 生成 JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());

        // Step 5: 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setRole(user.getRole());

        return response;
    }

    /**
     * 获取当前登录用户信息（用于 GET /api/auth/me）。
     *
     * @param username 用户名（从 JWT 中提取）
     * @return User 对象（不含密码）
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
