/**
 * InventoryService 单元测试 —— 测试库存查询、阈值更新、盘点。
 *
 * @author 殷智元
 */
package com.supermarket.service;

import com.supermarket.dto.InventoryDTO;
import com.supermarket.dto.PageDTO;
import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.entity.Supplier;
import com.supermarket.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService — 库存服务测试")
class InventoryServiceTest {

    @Mock private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product cola, chips;

    @BeforeEach
    void setUp() {
        Category drinks = new Category();
        drinks.setId(1L);
        drinks.setName("饮品");

        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("测试供应商");

        // 可乐：库存正常
        cola = new Product();
        cola.setId(1L);
        cola.setBarcode("6901234567890");
        cola.setName("可口可乐 330ml");
        cola.setUnit("瓶");
        cola.setSalePrice(new BigDecimal("3.00"));
        cola.setPurchasePrice(new BigDecimal("2.00"));
        cola.setStock(100);
        cola.setMinStock(10);
        cola.setMaxStock(500);
        cola.setCategory(drinks);
        cola.setSupplier(supplier);

        // 薯片：库存偏低
        chips = new Product();
        chips.setId(2L);
        chips.setBarcode("6909876543210");
        chips.setName("乐事薯片");
        chips.setUnit("袋");
        chips.setSalePrice(new BigDecimal("7.00"));
        chips.setPurchasePrice(new BigDecimal("5.00"));
        chips.setStock(3);
        chips.setMinStock(10);
        chips.setMaxStock(100);
        chips.setCategory(drinks);
        chips.setSupplier(supplier);
    }

    @Test
    @DisplayName("查询库存：应返回分页的库存信息，含库存金额和状态")
    void findAll() {
        // given
        List<Product> products = Arrays.asList(cola, chips);
        Page<Product> page = new PageImpl<>(products);
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // when
        PageDTO<InventoryDTO> result = inventoryService.findAll(0, 20);

        // then: 2 条记录
        assertEquals(2, result.getTotalElements());
        InventoryDTO colaDTO = result.getContent().get(0);

        // 可口可乐
        assertEquals("可口可乐 330ml", colaDTO.getProductName());
        assertEquals(100, colaDTO.getStock());
        assertEquals("NORMAL", colaDTO.getStatus());
        // 库存金额 = 100 × 2.00 = 200.00
        assertEquals(new BigDecimal("200.00"), colaDTO.getStockValue());

        // 薯片：库存 3 ≤ 下限 10 → LOW
        InventoryDTO chipsDTO = result.getContent().get(1);
        assertEquals(3, chipsDTO.getStock());
        assertEquals("LOW", chipsDTO.getStatus());
    }

    @Test
    @DisplayName("更新阈值：商品存在时应成功更新")
    void updateThreshold() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(cola));

        inventoryService.updateThreshold(1L, 20, 400);
        verify(productRepository).save(cola);
        assertEquals(20, cola.getMinStock());
        assertEquals(400, cola.getMaxStock());
    }

    @Test
    @DisplayName("更新阈值：商品不存在时抛异常")
    void updateThreshold_notFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> inventoryService.updateThreshold(999L, 10, 100));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("库存盘点：应返回所有库存低于下限的商品")
    void checkInventory() {
        // given: 只有薯片库存偏低
        when(productRepository.findLowStockWarning()).thenReturn(List.of(chips));

        // when
        List<InventoryDTO> result = inventoryService.checkInventory();

        // then: 只返回 1 条（薯片），可乐不在列表中
        assertEquals(1, result.size());
        assertEquals("乐事薯片", result.get(0).getProductName());
        assertEquals("LOW", result.get(0).getStatus());
    }

    @Test
    @DisplayName("库存状态：stock ≤ 0 时应为 OUT")
    void statusOut() {
        cola.setStock(0);
        List<Product> products = List.of(cola);
        Page<Product> page = new PageImpl<>(products);
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageDTO<InventoryDTO> result = inventoryService.findAll(0, 20);
        assertEquals("OUT", result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("库存状态：stock ≥ maxStock 时应为 HIGH")
    void statusHigh() {
        cola.setStock(999);
        List<Product> products = List.of(cola);
        Page<Product> page = new PageImpl<>(products);
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageDTO<InventoryDTO> result = inventoryService.findAll(0, 20);
        assertEquals("HIGH", result.getContent().get(0).getStatus());
    }
}
