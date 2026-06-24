/**
 * 换班记录实体 —— 对应数据库 shifts 表。
 *
 * 用于记录收银员的交班/换班操作。
 *
 * 业务场景：
 *   收银员 A 上早班（8:00-16:00），收银员 B 上晚班（16:00-24:00）。
 *   换班时：
 *     1. A 统计当班交易笔数和交易总额
 *     2. 核对收银机中的现金
 *     3. 创建换班记录，结束 A 的班次，开始 B 的班次
 *
 * 换班记录用于：
 *   1. 对账：核对收银机现金和系统交易记录是否一致
 *   2. 绩效考核：统计每个收银员的交易笔数和金额
 *   3. 责任追踪：一旦出现短款或长款，可以追溯到具体收银员
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 收银员。
     *
     * @ManyToOne：多个换班记录可以属于同一个收银员（不同日期）。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    @ToString.Exclude
    private Employee cashier;

    /**
     * 班次开始时间。
     * 收银员登录 POS 时自动记录。
     */
    @Column(name = "start_time")
    private LocalDateTime startTime = LocalDateTime.now();

    /**
     * 班次结束时间。
     * 收银员换班/退出时自动记录。
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 当班交易笔数。
     * 这个收银员在这个班次内完成了多少笔交易。
     */
    @Column(name = "transaction_count")
    private Integer transactionCount = 0;

    /**
     * 当班交易总额。
     * 这个收银员在这个班次内的销售总额。
     */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 班次状态：
     *   ACTIVE  — 当前正在进行中
     *   CLOSED  — 已交班关闭
     */
    @Column(length = 20)
    private String status = "ACTIVE";
}
