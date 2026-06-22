/**
 * 员工实体 —— 对应数据库 employees 表。
 *
 * 员工是超市的工作人员（收银员、管理员等物理人员）。
 * 注意 Employee 和 User 的关系：
 *   User = 系统登录账号（username + password + role）
 *   Employee = 人事信息（姓名、电话、职位、薪资等）
 *
 * Employee 和 User 是一对一关系：
 *   一个员工对应一个系统登录账号，一个账号也只属于一个员工。
 *   并非所有员工都需要登录账号（如临时工），所以此关联可为 null。
 *
 * 类似 C++ 中的：struct Employee { User* loginAccount; }; // 每个员工最多一个账号
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal; // 精确小数，用于金额。不用 double（会有精度问题）
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 员工姓名 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 邮箱 */
    @Column(length = 100)
    private String email;

    /**
     * 职位。
     * 例如：收银员、店长、仓库管理员、采购员
     */
    @Column(length = 50)
    private String position;

    /**
     * 月薪（精确小数）。
     *
     * 重要：金额必须用 BigDecimal，不能用 float/double。
     * 原因：float/double 是二进制浮点数，无法精确表示十进制小数。
     * 例如 0.1 在二进制中是无限循环小数，多次运算后累积误差会导致金额错误。
     *
     * BigDecimal 原理：内部用整数 + 小数点位置表示，精确无误差。
     */
    @Column(precision = 10, scale = 2) // 总共 10 位，小数点后 2 位
    private BigDecimal salary;

    /** 入职日期 */
    private LocalDate hireDate;

    /** 状态：1=在职 0=离职 */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 关联的系统登录账号（一对一关系）。
     *
     * @OneToOne：一个 Employee 对应一个 User，一个 User 也对应一个 Employee。
     * @JoinColumn(name = "user_id")：外键列，存储在 employees 表中。
     *   unique = true 确保一个 User 不会被多个 Employee 关联。
     *
     * FetchType.LAZY（懒加载）：
     *   默认只加载 Employee 本身，不加载关联的 User。
     *   只有在代码中调用 employee.getUser() 时才查数据库。
     *
     * 类似 C++ 中的：std::optional<User*> loginAccount;
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
