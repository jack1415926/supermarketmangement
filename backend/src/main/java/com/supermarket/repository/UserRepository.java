/**
 * 用户数据访问层 —— 对应 users 表。
 *
 * Spring Data JPA 的 Repository 接口：
 *   继承 JpaRepository<User, Long> 后自动获得 CRUD 方法：
 *     save(User)      — 新增/更新用户
 *     findById(Long)  — 按主键查询，返回 Optional<User>
 *     findAll()       — 查询所有用户
 *     deleteById(Long)— 按主键删除
 *     count()         — 统计总数
 *   无需写实现类！Spring 会在运行时自动生成代理对象。
 *
 * 自定义方法通过方法名命名约定自动生成 SQL：
 *   findBy + 字段名 + 条件
 *   例如 findByUsername → SELECT * FROM users WHERE username = ?
 *
 * 类似 C++ 中的模板 Repository 基类，提供泛型 CRUD 操作。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA 的泛型 Repository 接口
import org.springframework.stereotype.Repository;              // 标记为数据访问层组件

import java.util.Optional; // Java 8 的容器，用于安全处理可能为 null 的值

/**
 * JpaRepository<User, Long>：
 *   User  — 实体类型
 *   Long  — 主键类型
 */
@Repository // 标记为 Spring Bean，但继承 JpaRepository 的接口会被自动代理，此注解可选
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户（用于登录验证）。
     *
     * 方法名命名规则：findBy + Username（字段名首字母大写）
     * Spring Data JPA 会自动生成 SQL：
     *   SELECT * FROM users WHERE username = ?
     *
     * 返回值 Optional<User>：
     *   Optional 是 Java 8 引入的容器，封装可能为 null 的值。
     *   好处：强制调用者处理 null 情况，避免 NullPointerException。
     *   用法：userRepo.findByUsername("admin").orElseThrow(() -> new NotFoundException());
     *
     *   类似 C++17 的 std::optional<User>。
     *
     * @param username 用户名
     * @return 包装在 Optional 中的 User，如果不存在则 Optional.empty()
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否已存在（用于注册时防止重复）。
     *
     * 方法名命名规则：existsBy + Username
     * 生成 SQL：SELECT COUNT(*) > 0 FROM users WHERE username = ?
     *
     * @param username 用户名
     * @return true 表示用户名已被占用
     */
    boolean existsByUsername(String username);

    /**
     * 根据角色查找用户列表（用于管理后台的用户管理）。
     *
     * 生成 SQL：SELECT * FROM users WHERE role = ?
     *
     * @param role 角色标识（如 ROLE_CASHIER）
     * @return 该角色的所有用户
     */
    java.util.List<User> findByRole(String role);
}
