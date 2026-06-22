/**
 * 进货单数据访问层 —— 对应 purchases 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /** 根据进货单号查找 */
    Optional<Purchase> findByPurchaseNo(String purchaseNo);

    /** 按状态查询进货单（分页） */
    Page<Purchase> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /** 按供应商查询进货单 */
    Page<Purchase> findBySupplierId(Long supplierId, Pageable pageable);

    /** 按日期范围查询进货单 */
    @Query("SELECT p FROM Purchase p WHERE p.createdAt BETWEEN :start AND :end ORDER BY p.createdAt DESC")
    List<Purchase> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 按日期范围+状态查询 */
    @Query("SELECT p FROM Purchase p WHERE p.createdAt BETWEEN :start AND :end AND p.status = :status")
    List<Purchase> findByDateRangeAndStatus(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("status") String status
    );
}
