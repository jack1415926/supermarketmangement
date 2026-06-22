/**
 * 销售明细数据访问层 —— 对应 sale_items 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    /** 根据销售单 ID 查询所有明细 */
    List<SaleItem> findBySaleId(Long saleId);

    /** 根据商品 ID 查询销售记录（用于追溯某商品的销售历史） */
    List<SaleItem> findByProductId(Long productId);

    /**
     * 商品销售排行榜：按销量降序排列。
     *
     * 使用 JPQL 聚合查询：GROUP BY product.id, SUM(quantity), COUNT(sale.id)。
     * JPQL（Java Persistence Query Language）：
     *   类似 SQL，但操作的是实体对象而不是数据库表。
     *   表名 → 实体类名，列名 → 字段名。
     */
    @Query("SELECT si.product.id, SUM(si.quantity), COUNT(DISTINCT si.sale.id) " +
           "FROM SaleItem si WHERE si.sale.createdAt BETWEEN :start AND :end " +
           "GROUP BY si.product.id ORDER BY SUM(si.quantity) DESC")
    List<Object[]> getSalesRanking(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 商品销售排行榜（按销售额降序）。
     */
    @Query("SELECT si.product.id, SUM(si.quantity), COALESCE(SUM(si.subtotal), 0) " +
           "FROM SaleItem si WHERE si.sale.createdAt BETWEEN :start AND :end " +
           "GROUP BY si.product.id ORDER BY SUM(si.subtotal) DESC")
    List<Object[]> getSalesRankingByAmount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
