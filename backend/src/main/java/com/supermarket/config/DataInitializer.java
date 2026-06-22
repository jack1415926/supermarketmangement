/**
 * 数据初始化器 —— 首次启动时自动创建默认管理员账号。
 *
 * CommandLineRunner：
 *   Spring Boot 启动完成后自动执行 run() 方法。
 *   适合用于：插入初始数据、检查环境配置、预热缓存。
 *
 * 这里用于：
 *   首次启动时检查是否已有用户，如果没有则创建默认 admin 账号。
 *   这样后端启动后就能立即登录测试。
 *
 * 类似 C++ 中的 main() 之后调用一个 init() 函数做初始化。
 *
 * 默认账号：
 *   用户名：admin
 *   密码：admin123
 *   角色：ROLE_ADMIN（系统管理员，拥有所有权限）
 *
 * @author 徐磊
 */
package com.supermarket.config;

import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Lombok 的日志注解
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @Slf4j：Lombok 的日志注解，自动生成 log 对象。
 *   用法：log.info("消息")、log.error("错误", exception)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Boot 启动后自动执行。
     *
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) {
        // 检查是否已有用户记录
        if (userRepository.count() == 0) {
            log.info("数据库无用户记录，开始创建默认管理员账号...");

            // 创建默认管理员
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // BCrypt 加密
            admin.setDisplayName("系统管理员");
            admin.setRole("ROLE_ADMIN"); // 系统管理员，最高权限
            admin.setStatus(1);          // 启用状态

            userRepository.save(admin);

            log.info("默认管理员账号创建成功！");
            log.info("  用户名: admin");
            log.info("  密码: admin123");
            log.info("  请登录后立即修改密码！");
        } else {
            log.info("数据库已有 {} 个用户，跳过初始化。", userRepository.count());
        }
    }
}
