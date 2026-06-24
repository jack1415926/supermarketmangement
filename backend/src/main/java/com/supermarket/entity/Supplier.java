/**
 * 供应商实体 —— 对应数据库 suppliers 表。
 *
 * 供应商是超市的货物来源。每个商品都有一个关联的供应商。
 * 供应商信息用于：
 *   1. 进货时选择供应商
 *   2. 查询某供应商供货的所有商品
 *   3. 联系供应商进行补货
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 供应商名称（公司名称），必须唯一 */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** 联系人姓名 */
    @Column(length = 50)
    private String contact;

    /** 联系电话 */
    @Column(length = 20)
    private String phone;

    /** 公司地址 */
    @Column(length = 200)
    private String address;

    /** 状态：1=合作中 0=已停用 */
    @Column(nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
