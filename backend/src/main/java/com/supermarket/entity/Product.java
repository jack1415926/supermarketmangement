/**
 * 商品实体 —— 对应数据库 products 表。
 *
 * 这是商品信息的核心表，记录超市销售的所有商品。
 * 每个商品属于一个分类（Category），由某个供应商（Supplier）供货。
 *
 * 重要字段说明：
 *   barcode       — 条形码，POS 收银时通过扫码枪读取
 *   salePrice     — 零售价（卖给顾客的价格）
 *   purchasePrice — 进货价（从供应商采购的成本价）
 *   stock         — 当前库存数量
 *   minStock      — 库存下限阈值，库存低于此值时触发预警
 *   maxStock      — 库存上限阈值，库存高于此值时触发积压预警
 *
 * 金额全部使用 BigDecimal（精确小数），绝不用 float/double。
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 条形码，POS 收银时扫码枪读取的就是这个。
     * 必须唯一，一个条形码对应一个商品。
     * 例如：6901234567890（13 位 EAN-13 码）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String barcode;

    /** 商品名称，如 "可口可乐 330ml" */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 规格/单位，如 "瓶"、"袋"、"箱"、"kg"。
     * 用于前端显示和盘点时的计量。
     */
    @Column(length = 20)
    private String unit;

    /**
     * 零售价（卖给顾客的单价）。
     *
     * precision = 10, scale = 2  → 最多 10 位有效数字，其中 2 位小数。
     * 例如：99999999.99 元。
     */
    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    /**
     * 进货价（从供应商采购的单价）。
     * 用于计算利润 = salePrice - purchasePrice。
     */
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    /** 当前库存数量 */
    @Column(nullable = false)
    private Integer stock = 0;

    /**
     * 库存下限（最小库存阈值）。
     * 当 stock <= minStock 时，系统会发出缺货预警。
     * 用于"进货计划"功能——帮助管理员判断哪些商品需要补货。
     */
    @Column(name = "min_stock")
    private Integer minStock = 0;

    /**
     * 库存上限（最大库存阈值）。
     * 当 stock >= maxStock 时，系统会发出积压预警。
     * 避免过度采购导致库存积压和资金占用。
     */
    @Column(name = "max_stock")
    private Integer maxStock = 999;

    /**
     * 生产日期。
     * 用于食品类商品的保质期管理。
     */
    @Column(name = "production_date")
    private LocalDateTime productionDate;

    /**
     * 保质期（天）。
     * 例如 180 表示保质期 180 天（约半年）。
     * 结合 production_date 可以计算过期日期。
     */
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    /** 状态：1=上架销售 0=已下架 */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 乐观锁版本号 —— 解决并发超卖问题。
     *
     * @Version 是 JPA 的乐观锁机制：
     *   每次更新 Product 时，JPA 自动将 version 加 1，
     *   并在 UPDATE 语句中加上 WHERE version = ? 条件。
     *   如果两个并发请求同时更新同一商品：
     *     T1 读到 version=5 → UPDATE SET stock=2, version=6 WHERE id=1 AND version=5 → 成功
     *     T2 读到 version=5 → UPDATE SET stock=2, version=6 WHERE id=1 AND version=5 → 失败（version 已是 6）
     *   T2 会收到 OptimisticLockException，Spring 自动回滚事务。
     *   调用方可以重试整个 checkout 操作。
     *
     * 类似 C++ 中的 CAS（Compare-And-Swap）原子操作。
     */
    @Version
    @Column(name = "version")
    private Long version;

    // ==================== 关联关系 ====================

    /**
     * 所属分类。
     *
     * @ManyToOne：多个商品属于同一个分类。
     * FetchType.LAZY：默认不加载分类详情，只在需要时查询。
     * @JoinColumn(name = "category_id")：外键列名。
     *   在 products 表中有一个 category_id 列，指向 categories 表的 id。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude // 避免 toString 触发懒加载
    private Category category;

    /**
     * 供应商。
     *
     * @ManyToOne：多个商品由同一个供应商供货。
     * FetchType.LAZY：默认不加载供应商详情。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @ToString.Exclude
    private Supplier supplier;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
