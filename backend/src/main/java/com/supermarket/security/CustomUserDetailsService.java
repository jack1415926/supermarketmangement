/**
 * 自定义用户详情服务 —— 从数据库加载用户信息给 Spring Security。
 *
 * Spring Security 不直接操作 User 实体，而是通过 UserDetailsService 接口来获取用户。
 * 这个类实现了该接口，从我们的 users 表中查询用户，转换为 Spring Security 能理解的格式。
 *
 * 工作流程（登录时）：
 *   1. AuthController 收到登录请求
 *   2. AuthService 调用 AuthenticationManager.authenticate()
 *   3. AuthenticationManager 调用本类的 loadUserByUsername()
 *   4. 从数据库查到用户，包装为 UserDetails 返回
 *   5. AuthenticationManager 自动比对密码（用 BCryptPasswordEncoder）
 *   6. 密码正确 → 认证成功 → 生成 JWT
 *
 * 类似 C++ 中的回调函数：Spring Security 调用我们的 loadUserByUsername 来获取用户数据。
 *
 * @author 徐磊
 */
package com.supermarket.security;

import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @Service：标记为服务层组件（类似 @Component，但语义更明确）
 *   Spring 会自动扫描并创建这个类的 Bean。
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户信息。
     * Spring Security 在认证时自动调用此方法。
     *
     * @param username 用户名（来自登录请求）
     * @return UserDetails 对象（Spring Security 的标准用户格式）
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 转换为 Spring Security 的 UserDetails
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),                          // 用户名
            user.getPassword(),                          // 密码（已 BCrypt 加密）
            Integer.valueOf(1).equals(user.getStatus()),   // enabled（避免 Integer 自动拆箱 NPE）
            true,                                        // accountNonExpired（账号未过期）
            true,                                        // credentialsNonExpired（凭证未过期）
            true,                                        // accountNonLocked（账号未锁定）
            Collections.singletonList(                   // 权限列表
                new SimpleGrantedAuthority(user.getRole()) // 用户角色
            )
        );
    }
}
