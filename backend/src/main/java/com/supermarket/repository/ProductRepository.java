/**
 * 商品数据访问层 —— 对应 products 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Product;
import org.springframework.data.domain.Page;            // Spring Data 的分页对象
import org.springframework.data.domain.Pageable;       // 分页参数（页码、每页大小、排序）
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;  // 自定义 JPQL 查询
import org.springframework.data.repository.query.Param; // 命名参数绑定
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 根据条形码精确查找（POS 扫码时使用）。
     */
    Optional<Product> findByBarcode(String barcode);

    /**
     * 根据商品名称模糊搜索（分页）。
     *
     * Containing → LIKE %keyword%
     * Page<Product> → 分页结果，包含总记录数、总页数等信息
     * Pageable → 传入页码、每页大小、排序方式
     */
    Page<Product> findByNameContaining(String keyword, Pageable pageable);

    /**
     * 按分类 ID 查询商品（分页）。
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 模糊搜索 + 分类筛选（分页）。
     * 同时匹配名称和分类。
     */
    Page<Product> findByNameContainingAndCategoryId(String keyword, Long categoryId, Pageable pageable);

    /**
     * 查询所有库存低于下限的商品（缺货预警）。
     * 用于"进货计划"功能。
     *
     * Spring Data JPA 的方法命名无法直接比较两个字段（stock <= minStock），
     * 因此使用 @Query JPQL 手动编写查询。
     */
    @Query("SELECT p FROM Product p WHERE p.stock <= p.minStock")
    List<Product> findLowStock();

    /**
     * 查询所有库存高于上限的商品（积压预警）。
     * 同上，两个字段的比较必须用 @Query。
     */
    @Query("SELECT p FROM Product p WHERE p.stock >= p.maxStock")
    List<Product> findOverStock();

    /**
     * 根据供应商 ID 查询商品。
     */
    List<Product> findBySupplierId(Long supplierId);

    /** 检查条形码是否已存在 */
    boolean existsByBarcode(String barcode);
}
