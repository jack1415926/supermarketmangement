/**
 * 数据初始化器 —— 首次启动时自动创建默认管理员账号。
 *
 * CommandLineRunner：
 *   Spring Boot 启动完成后自动执行 run() 方法。
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
import com.supermarket.repository.EmployeeRepository;
import com.supermarket.entity.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Spring Boot 启动后自动执行。
     */
    @Override
    public void run(String... args) {
        try {
            long count = userRepository.count();
            log.info("当前用户数量: {}", count);

            // 确保 admin 有对应的员工记录（POS 结账时需要）
            if (employeeRepository.findByUserUsername("admin").isEmpty()) {
                Employee adminEmp = new Employee();
                adminEmp.setName("系统管理员");
                adminEmp.setPhone("13800000000");
                adminEmp.setPosition("系统管理员");
                adminEmp.setSalary(new BigDecimal("0"));
                adminEmp.setHireDate(LocalDate.now());
                adminEmp.setUser(userRepository.findByUsername("admin").orElse(null));
                employeeRepository.save(adminEmp);
                log.info("为 admin 创建员工记录");
            }

            if (count == 0) {
                log.info("数据库无用户记录，开始创建默认账号...");

                // 直接 new BCryptPasswordEncoder，不依赖注入
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setDisplayName("系统管理员");
                admin.setRole("ROLE_ADMIN");
                admin.setStatus(1);

                userRepository.save(admin);
                log.info("默认管理员账号创建成功！用户名: admin, 密码: admin123");

                // 同时创建一个收银员账号方便测试
                User cashier = new User();
                cashier.setUsername("cashier");
                cashier.setPassword(encoder.encode("cashier123"));
                cashier.setDisplayName("李收银");
                cashier.setRole("ROLE_CASHIER");
                cashier.setStatus(1);
                userRepository.save(cashier);
                log.info("收银员账号创建成功！用户名: cashier, 密码: cashier123");

                // 同时创建一个管理员账号
                User manager = new User();
                manager.setUsername("manager");
                manager.setPassword(encoder.encode("manager123"));
                manager.setDisplayName("张经理");
                manager.setRole("ROLE_MANAGER");
                manager.setStatus(1);
                userRepository.save(manager);
                log.info("管理员账号创建成功！用户名: manager, 密码: manager123");
            }
        } catch (Exception e) {
            log.error("数据初始化失败", e);
        }
    }
}
