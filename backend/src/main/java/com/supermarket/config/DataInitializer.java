/**
 * 数据初始化器 —— 首次启动时自动创建三个默认账号。
 *
 * @author 殷智元
 */
package com.supermarket.config;

import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("数据库无用户记录，开始创建默认账号...");

            // 管理员（所有权限）
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setDisplayName("系统管理员");
            admin.setRole("ROLE_ADMIN");
            admin.setStatus(1);
            userRepository.save(admin);

            // 店长（管理权限）
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("123456"));
            manager.setDisplayName("店长");
            manager.setRole("ROLE_MANAGER");
            manager.setStatus(1);
            userRepository.save(manager);

            // 收银员（收银权限）
            User cashier = new User();
            cashier.setUsername("cashier");
            cashier.setPassword(passwordEncoder.encode("123456"));
            cashier.setDisplayName("收银员");
            cashier.setRole("ROLE_CASHIER");
            cashier.setStatus(1);
            userRepository.save(cashier);

            log.info("默认账号创建完成！");
            log.info("  admin   / 123456   (超管)");
            log.info("  manager / 123456   (店长)");
            log.info("  cashier / 123456   (收银员)");
        } else {
            log.info("数据库已有 {} 个用户，跳过初始化。", userRepository.count());
        }
    }
}
