/**
 * 系统用户实体 —— 对应数据库 users 表。
 *
 * 这是整个权限系统的核心实体，每个可以登录系统的人都有一条 User 记录。
 * 用户通过 username + password 登录，role 决定他能看到哪些菜单、调用哪些 API。
 *
 * JPA 实体 = Java 对象与数据库表的映射。
 * 类似 C++ 中 ORM 框架的 "把 class 映射到数据库表"。
 *
 * 表名：users（不用 "user" 是因为 MySQL 中 user 是保留字）
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // Jackson 注解：序列化为 JSON 时隐藏此字段
import jakarta.persistence.*;                      // JPA 注解（@Entity, @Id, @Column 等）
import lombok.*;                                    // Lombok 注解（自动生成 getter/setter/构造器）
import org.hibernate.annotations.CreationTimestamp; // 自动填充创建时间
import org.hibernate.annotations.UpdateTimestamp;   // 自动填充更新时间

import java.time.LocalDateTime; // Java 8+ 的日期时间类（线程安全，推荐替代 Date）

/**
 * @Entity 告诉 JPA：这个类对应数据库中的一张表。
 * 类似 C++ 中定义一个 struct 然后用宏标记它"可以被持久化"。
 *
 * @Table(name = "users") 指定表名。
 * 不写的话默认用类名小写（即 "user"），但 user 是 MySQL 保留字，所以必须指定。
 *
 * @Data 是 Lombok 注解，自动生成：
 *   - getter（如 getId(), getUsername()）
 *   - setter（如 setId(Long id), setUsername(String username)）
 *   - toString(), equals(), hashCode()
 * 类似 C++ 中 IDE 自动生成的 getter/setter，但 Lombok 在编译期生成，不占源码行数。
 *
 * @NoArgsConstructor 生成无参构造函数（JPA 要求）
 * @AllArgsConstructor 生成全参构造函数（方便创建对象）
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor  // JPA 要求每个实体必须有无参构造器
@AllArgsConstructor // 全参构造器，方便测试和初始化时快速创建对象
public class User {

    /**
     * @Id 标记主键。
     * 类似数据库中的 PRIMARY KEY。
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY) 表示主键由数据库自动生成（自增）。
     * MySQL 的 AUTO_INCREMENT 对应这个策略。
     * 好处：插入时不需手动指定 ID，数据库会自动分配。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 登录用户名，必须唯一。
     *
     * @Column(nullable = false)  → 数据库列不允许 NULL
     * @Column(unique = true)     → 数据库唯一约束（UNIQUE INDEX）
     * @Column(length = 50)       → 列最大长度
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 登录密码，存储 BCrypt 加密后的密文（不是明文！）。
     *
     * @JsonIgnore：序列化为 JSON 时自动跳过此字段。
     *   这样即使 Service 层不小心返回了完整的 User 实体（如 GET /api/auth/me），
     *   密码哈希也不会出现在前端响应中。这是防止密码泄露的第二道防线。
     *
     * 重要：永远不要在数据库存明文密码。BCrypt 是单向哈希，不可逆。
     */
    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * 显示名称（如真实姓名），用于界面上的欢迎语。
     */
    @Column(length = 50)
    private String displayName;

    /**
     * 用户角色，决定权限。
     *
     * 可选值（对应 Spring Security 的 role）：
     *   ROLE_CASHIER  — 收银员（只能用 POS 收银台）
     *   ROLE_MANAGER  — 管理员/店长（管理后台全部功能）
     *   ROLE_ADMIN    — 系统管理员（系统配置、权限管理）
     *
     * @Enumerated(EnumType.STRING) 告诉 JPA：存枚举的字符串名，不是数字。
     * 如果不用 STRING 模式，存的是 0/1/2 这种序号，增删枚举值会导致数据库错乱。
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 账号状态。
     * 1 = 启用（正常使用）
     * 0 = 禁用（无法登录）
     *
     * 配合 Spring Security 的 isEnabled() 方法，禁用后该用户的所有 Token 立即失效。
     */
    @Column(nullable = false)
    private Integer status = 1; // 默认启用

    /**
     * @CreationTimestamp 是 Hibernate 扩展注解。
     * 在实体首次插入数据库时，自动设置为当前时间。
     * 不需要手动赋值，Hibernate 在 INSERT 时自动处理。
     *
     * 类似 C++ 中的 "自动填充创建时间戳"。
     */
    @CreationTimestamp
    @Column(updatable = false) // 一旦创建就不允许更新
    private LocalDateTime createdAt;

    /**
     * @UpdateTimestamp 是 Hibernate 扩展注解。
     * 每次更新实体时，自动设置为当前时间。
     * 不需要手动赋值，Hibernate 在 UPDATE 时自动处理。
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
