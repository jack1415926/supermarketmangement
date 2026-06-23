/**
 * 销售单数据访问层 —— 对应 sales 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /** 根据流水号查找 */
    Optional<Sale> findByFlowNo(String flowNo);

    /** 按收银员查询（分页） */
    Page<Sale> findByCashierId(Long cashierId, Pageable pageable);

    /** 按会员查询（分页） */
    Page<Sale> findByMemberId(Long memberId, Pageable pageable);

    /** 按日期范围查询 */
    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :start AND :end ORDER BY s.createdAt DESC")
    List<Sale> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 按日期范围查询（分页） */
    @Query("SELECT s FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    Page<Sale> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    /** 当日销售汇总：总金额 */
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    BigDecimal getTotalAmountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 当日交易笔数 */
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    Long getTransactionCountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 某收银员在指定时间范围内的交易总金额（用于换班统计）。
     * 按 cashier_id 筛选，只统计该收银员的交易。
     */
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
           "WHERE s.cashier.id = :cashierId AND s.createdAt BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    BigDecimal getTotalAmountByCashierAndDateRange(
        @Param("cashierId") Long cashierId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * 某收银员在指定时间范围内的交易笔数（用于换班统计）。
     */
    @Query("SELECT COUNT(s) FROM Sale s " +
           "WHERE s.cashier.id = :cashierId AND s.createdAt BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    Long getTransactionCountByCashierAndDateRange(
        @Param("cashierId") Long cashierId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * 收银员交班汇总：按收银员统计当日交易笔数和金额。
     * JPQL 聚合查询：GROUP BY cashier.id，计算 COUNT 和 SUM。
     */
    @Query("SELECT s.cashier.id, COUNT(s), COALESCE(SUM(s.totalAmount), 0) " +
           "FROM Sale s WHERE s.createdAt BETWEEN :start AND :end " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY s.cashier.id")
    List<Object[]> getCashierDailySummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 每日销售报表：按日期分组统计。
     * 使用 MySQL 的 DATE() 函数提取日期部分。
     */
    @Query(value = "SELECT DATE(s.created_at) as sale_date, COUNT(s.id), COALESCE(SUM(s.total_amount), 0) " +
           "FROM sales s WHERE s.created_at BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
           "GROUP BY DATE(s.created_at) ORDER BY sale_date",
           nativeQuery = true)
    List<Object[]> getDailySalesReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 月度销售报表：按月份分组统计。
     */
    @Query(value = "SELECT YEAR(s.created_at), MONTH(s.created_at), COUNT(s.id), COALESCE(SUM(s.total_amount), 0) " +
           "FROM sales s WHERE s.created_at BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
           "GROUP BY YEAR(s.created_at), MONTH(s.created_at) ORDER BY YEAR(s.created_at), MONTH(s.created_at)",
           nativeQuery = true)
    List<Object[]> getMonthlySalesReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
