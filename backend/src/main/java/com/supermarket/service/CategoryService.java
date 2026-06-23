/**
 * 分类服务 —— 商品分类的增删改查 + 树形结构构建。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;
import com.supermarket.repository.CategoryRepository;
import com.supermarket.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    /** 查询分类树（只读操作） */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

    /** 删除分类（需检查是否有子分类和商品） */
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        // 检查是否有子分类
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("该分类下有子分类，请先删除子分类");
        }
        // 检查是否有商品使用此分类（防止外键约束错误）
        // 使用分页查询检查是否有任何商品引用了此分类
        if (productRepository.findByCategoryId(id, org.springframework.data.domain.PageRequest.of(0, 1)).hasContent()) {
            throw new IllegalArgumentException("该分类下有商品，无法删除");
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
