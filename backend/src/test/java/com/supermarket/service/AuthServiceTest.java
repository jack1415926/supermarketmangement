/**
 * AuthService 单元测试 —— 测试登录、认证逻辑。
 *
 * 使用 Mockito 模拟所有依赖，不连接数据库。
 * Mockito 是 Java 最常用的 mock 框架，类似 C++ 的 GoogleMock。
 *
 * @ExtendWith(MockitoExtension.class)：用 Mockito 扩展运行测试，不启动 Spring 容器。
 * @Mock：创建模拟对象（假的 Repository、假的 AuthenticationManager）
 * @InjectMocks：把模拟对象注入到被测试的 Service 中
 *
 * 测试通过 `./mvnw test` 执行，不需要 MySQL。
 *
 * @author 殷智元
 */
package com.supermarket.service;

import com.supermarket.dto.auth.LoginRequest;
import com.supermarket.dto.auth.LoginResponse;
import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import com.supermarket.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — 认证服务测试")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("$2a$10$encodedPasswordHash");
        user.setDisplayName("管理员");
        user.setRole("ROLE_ADMIN");
        user.setStatus(1);
    }

    @Test
    @DisplayName("登录成功：正确的用户名和密码应返回 JWT Token")
    void login_success() {
        // given: 模拟认证成功
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken("admin", "ROLE_ADMIN")).thenReturn("fake-jwt-token");

        // when: 执行登录
        LoginResponse response = authService.login(loginRequest);

        // then: 验证返回值
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("admin", response.getUsername());
        assertEquals("管理员", response.getDisplayName());
        assertEquals("ROLE_ADMIN", response.getRole());
        assertEquals(1L, response.getUserId());

        // 验证调用了认证管理器
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("登录失败：密码错误应抛出 BadCredentialsException")
    void login_wrongPassword() {
        // given: 模拟认证失败
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("密码错误"));

        // when + then: 应抛出异常
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("登录失败：账号被禁用应抛出 DisabledException")
    void login_disabledAccount() {
        // given: 认证通过但账号被禁用
        user.setStatus(0);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // when + then
        assertThrows(DisabledException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("获取当前用户：应返回完整的用户信息")
    void getCurrentUser() {
        // given
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // when
        User result = authService.getCurrentUser("admin");

        // then
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("ROLE_ADMIN", result.getRole());
    }
}
