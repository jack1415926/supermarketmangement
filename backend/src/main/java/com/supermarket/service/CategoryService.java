/**
 * 分类服务 —— 商品分类的增删改查 + 树形结构构建。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;
import com.supermarket.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 查询分类树。
     *
     * 算法：
     *   1. 查出所有启用的分类
     *   2. 按 parentId 分组
     *   3. 递归构建树：每个节点找到自己的 children
     *
     * 为什么不直接递归查数据库？
     *   - N 次查询效率低（N+1 问题）
     *   - 分类数量一般不多（<1000），全部加载到内存再构建树效率高
     */
    public List<CategoryDTO> findAllAsTree() {
        List<Category> all = categoryRepository.findByStatusOrderBySortOrderAsc(1);
        // 转为 DTO 列表
        List<CategoryDTO> dtoList = all.stream().map(this::toDTO).collect(Collectors.toList());

        // 按 parentId 分组
        Map<Long, List<CategoryDTO>> parentMap = dtoList.stream()
            .filter(dto -> dto.getParentId() != null)
            .collect(Collectors.groupingBy(CategoryDTO::getParentId));

        // 组装树：根节点（parentId==null）直接返回，子节点挂到父节点下
        List<CategoryDTO> roots = new ArrayList<>();
        for (CategoryDTO dto : dtoList) {
            if (dto.getParentId() == null) {
                roots.add(dto);
            }
            // 设置子节点
            dto.setChildren(parentMap.getOrDefault(dto.getId(), new ArrayList<>()));
        }
        return roots;
    }

    /** 按 ID 查询单个分类 */
    public Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("分类不存在: id=" + id));
    }

    /** 新增分类 */
    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("分类名称已存在: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    /** 更新分类 */
    @Transactional
    public Category update(Long id, Category updated) {
        Category existing = findById(id);
        if (!existing.getName().equals(updated.getName())
            && categoryRepository.existsByName(updated.getName())) {
            throw new IllegalArgumentException("分类名称已存在: " + updated.getName());
        }
        existing.setName(updated.getName());
        existing.setParentId(updated.getParentId());
        existing.setSortOrder(updated.getSortOrder());
        existing.setStatus(updated.getStatus());
        return categoryRepository.save(existing);
    }

    /** 删除分类（需检查是否有子分类） */
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("该分类下有子分类，请先删除子分类");
        }
        categoryRepository.delete(category);
    }

    /** Entity 转 DTO */
    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setParentId(c.getParentId());
        dto.setSortOrder(c.getSortOrder());
        dto.setStatus(c.getStatus());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
