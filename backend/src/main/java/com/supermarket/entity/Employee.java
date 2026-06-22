/**
 * 员工实体 —— 对应数据库 employees 表。
 *
 * 员工是超市的工作人员（收银员、管理员等物理人员）。
 * 注意 Employee 和 User 的关系：
 *   User = 系统登录账号（username + password + role）
 *   Employee = 人事信息（姓名、电话、职位、薪资等）
 * 一个 Employee 可以关联一个 User 登录账号，也可以暂时没有。
 *
 * @ManyToOne(fetch = FetchType.LAZY) 表示多对一的关系，多个员工可以关联同一个用户（虽然通常是一对一的）。
 * LAZY 加载：只有在访问 user 字段时才查询数据库，避免不必要的 JOIN。
 *
 * 类似 C++ 中有一个指针指向关联对象，但只在解引用时才加载数据。
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
     * 关联的系统登录账号。
     *
     * @ManyToOne：多个 Employee 可以关联到同一个 User（多对一关系）。
     * @JoinColumn(name = "user_id")：外键列的名字是 user_id，存储在 employees 表中。
     *
     * FetchType.LAZY（懒加载）：
     *   默认情况下只加载 Employee 本身，不加载关联的 User。
     *   只有在代码中调用 employee.getUser() 时才发第二条 SQL 查询。
     *   好处：避免了不需要 User 信息时多余的数据库查询。
     *
     * 类似 C++ 中的 std::optional<std::reference_wrapper<User>>，按需访问。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
