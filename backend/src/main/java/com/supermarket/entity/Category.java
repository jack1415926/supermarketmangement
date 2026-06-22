/**
 * 商品分类实体 —— 对应数据库 categories 表。
 *
 * 支持多级分类，通过 parentId 自引用实现树形结构。
 * 例如：食品 → 休闲食品 → 薯片
 *       食品 → 粮油调味 → 酱油
 *       饮品 → 碳酸饮料 → 可乐
 *
 * 树的实现方式：
 *   parentId = null  → 一级分类（根节点，如"食品""饮品"）
 *   parentId = 5     → 父分类 ID 为 5 的子分类
 *   sortOrder        → 同级分类的排序号，数字越小越靠前
 *
 * 自引用关系（Self-Referencing）：
 *   @ManyToOne → 指向父分类（parent）
 *   @OneToMany → 指向子分类列表（children）
 *
 * 类似 C++ 中的树节点：struct Node { Node* parent; vector<Node*> children; };
 *
 * @author 徐磊
 */
package com.supermarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // Jackson 注解：序列化时忽略该字段
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类名称，如 "食品"、"饮品"、"日用品" */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 父分类 ID。
     * null 表示这是一级分类（根节点）。
     * 不为 null 时，值是父 Category 的 id。
     *
     * 不直接用 @ManyToOne 对象关系，而是存裸 ID。
     * 原因：简化树形结构的查询和构建，避免 Hibernate 自动生成多余的 JOIN。
     * 树形结构在 Service 层手动递归组装，比 JPA 自动加载更可控。
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序号，数字越小越靠前。
     * 用于控制前端分类菜单的显示顺序。
     * 例如：食品(sortOrder=1)、饮品(sortOrder=2)、日用品(sortOrder=3)
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 状态：1=启用 0=禁用。
     * 禁用的分类及其子分类在前端不显示。
     */
    @Column(nullable = false)
    private Integer status = 1;

    // ==================== 自引用关系（仅用于查询，不参与数据库映射） ====================

    /**
     * 父分类对象引用。
     *
     * @ManyToOne：多个子分类指向同一个父分类。
     * FetchType.LAZY：按需加载，只在访问 getParent() 时查询。
     *
     * @JoinColumn(name = "parent_id")：外键列。
     * insertable=false, updatable=false：告诉 JPA 不要通过这个字段插入/更新。
     * 因为 parentId 字段已经直接存了父 ID，这个对象引用只是用于读取。
     * 如果让两个字段同时管理外键，JPA 会报错。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonIgnore // 序列化为 JSON 时忽略此字段，避免循环引用导致无限递归
    @ToString.Exclude // Lombok 的 toString 不要包含这个字段，避免触发懒加载
    private Category parent;

    /**
     * 子分类列表。
     *
     * @OneToMany：一个分类可以有多个子分类。
     * mappedBy = "parent"：表示这个关系由 Category.parent 字段来维护，
     *   JPA 不会为这个关系创建中间表。
     *
     * 类似 C++ 中的 vector<Node*> children。
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private List<Category> children = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
