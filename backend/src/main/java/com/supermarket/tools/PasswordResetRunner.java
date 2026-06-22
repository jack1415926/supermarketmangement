package com.supermarket.tools;

import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 密码重置工具 —— 启动时把所有用户密码统一重置为 "123456"
 *
 * 使用场景：
 *   1. 第一次部署，data.sql 里的 BCrypt hash 跟项目用的 PasswordEncoder 不匹配
 *   2. 测试期间密码混乱，想全部重置
 *   3. 生产环境绝不能用
 *
 * 用法：
 *   1. 把本类放到 backend/src/main/java/com/supermarket/tools/PasswordResetRunner.java
 *   2. 启动项目
 *   3. 控制台看到 ">>> 3 个用户密码已重置为 123456" 即成功
 *   4. 删除本类（或注释掉 @Component），重启项目
 */
@Component
public class PasswordResetRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String targetPassword = "123456";
        String encoded = passwordEncoder.encode(targetPassword);

        List<User> users = userRepository.findAll();
        int count = 0;
        for (User u : users) {
            u.setPassword(encoded);
            userRepository.save(u);
            count++;
        }

        System.out.println(">>> [PasswordResetRunner] " + count + " 个用户密码已重置为 '" + targetPassword + "'");
    }
}
