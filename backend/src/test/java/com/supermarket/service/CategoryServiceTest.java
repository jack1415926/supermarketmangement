/**
 * CategoryService 单元测试 —— 测试分类 CRUD + 树形构建。
 *
 * @author 殷智元
 */
package com.supermarket.service;

import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.repository.CategoryRepository;
import com.supermarket.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService — 分类服务测试")
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category food, snack, drink;

    @BeforeEach
    void setUp() {
        // 创建测试分类：食品(根) → 零食(子)，饮品(根)
        food = createCategory(1L, "食品", null, 1);
        snack = createCategory(2L, "休闲食品", 1L, 1);
        drink = createCategory(3L, "饮品", null, 2);
    }

    @Test
    @DisplayName("查询分类树：应返回正确的树形结构")
    void findAllAsTree() {
        // given: 模拟返回所有分类
        when(categoryRepository.findByStatusOrderBySortOrderAsc(1))
            .thenReturn(List.of(food, snack, drink));

        // when
        List<CategoryDTO> tree = categoryService.findAllAsTree();

        // then: 根节点有 2 个（食品、饮品）
        assertEquals(2, tree.size());
        assertEquals("食品", tree.get(0).getName());
        assertEquals("饮品", tree.get(1).getName());
        // 食品下有一个子节点
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("休闲食品", tree.get(0).getChildren().get(0).getName());
        // 饮品没有子节点
        assertTrue(tree.get(1).getChildren().isEmpty());
    }

    @Test
    @DisplayName("查询单个分类：ID 存在时返回分类，不存在时抛异常")
    void findById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertEquals("食品", categoryService.findById(1L).getName());
        assertThrows(NoSuchElementException.class, () -> categoryService.findById(999L));
    }

    @Test
    @DisplayName("新增分类：名称不重复时应成功保存")
    void create_success() {
        when(categoryRepository.existsByName("食品")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(food);

        Category result = categoryService.create(food);
        assertNotNull(result);
        verify(categoryRepository).save(food);
    }

    @Test
    @DisplayName("新增分类：名称重复时抛异常")
    void create_duplicateName() {
        when(categoryRepository.existsByName("食品")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.create(food));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("删除分类：有子分类时应抛异常")
    void delete_withChildren() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(categoryRepository.findByParentIdOrderBySortOrderAsc(1L)).thenReturn(List.of(snack));

        assertThrows(IllegalArgumentException.class, () -> categoryService.delete(1L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("删除分类：有商品引用时应抛异常")
    void delete_withProducts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(categoryRepository.findByParentIdOrderBySortOrderAsc(1L)).thenReturn(List.of());
        // 模拟有商品引用了此分类（返回非空页）
        Page<Product> nonEmptyPage = mock(Page.class);
        when(nonEmptyPage.hasContent()).thenReturn(true);
        when(productRepository.findByCategoryId(eq(1L), any(PageRequest.class)))
            .thenReturn(nonEmptyPage);

        assertThrows(IllegalArgumentException.class, () -> categoryService.delete(1L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("删除分类：无子分类无商品时应成功")
    void delete_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(food));
        when(categoryRepository.findByParentIdOrderBySortOrderAsc(1L)).thenReturn(List.of());
        // Mock 返回空页（无商品引用）
        Page emptyPage = mock(Page.class);
        when(emptyPage.hasContent()).thenReturn(false);
        when(productRepository.findByCategoryId(eq(1L), any(PageRequest.class))).thenReturn(emptyPage);

        categoryService.delete(1L);
        verify(categoryRepository).delete(food);
    }

    /** 辅助：创建分类对象 */
    private Category createCategory(Long id, String name, Long parentId, int sortOrder) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setParentId(parentId);
        c.setSortOrder(sortOrder);
        c.setStatus(1);
        c.setChildren(new ArrayList<>());
        return c;
    }
}
