/**
 * 商品分类数据访问层 —— 对应 categories 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 查询所有一级分类（根节点），按排序号升序排列。
     * parentId IS NULL → 没有父分类的就是根节点。
     *
     * 生成 SQL：SELECT * FROM categories WHERE parent_id IS NULL ORDER BY sort_order ASC
     */
    List<Category> findByParentIdIsNullOrderBySortOrderAsc();

    /**
     * 查询某分类下的所有子分类。
     *
     * 生成 SQL：SELECT * FROM categories WHERE parent_id = ? ORDER BY sort_order ASC
     */
    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * 查询所有启用的分类，按排序号升序排列。
     * 用于前端展示分类菜单/树时过滤已禁用的分类。
     */
    List<Category> findByStatusOrderBySortOrderAsc(Integer status);

    /** 检查分类名称是否已存在 */
    boolean existsByName(String name);
}
