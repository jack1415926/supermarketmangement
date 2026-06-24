/**
 * 库存服务 —— 库存查询（分页）、预警阈值设置、盘点。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.InventoryDTO;
import com.supermarket.dto.PageDTO;
import com.supermarket.entity.Product;
import com.supermarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    /**
     * 分页查询库存信息。
     * 避免一次性加载所有商品导致内存溢出。
     */
    @Transactional(readOnly = true)
    public PageDTO<InventoryDTO> findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Product> productPage = productRepository.findAll(pageable);
        return PageDTO.from(productPage.map(this::toDTO));
    }

    /**
     * 更新商品库存预警阈值。
     * null 参数表示保持原值不变（避免误覆盖）。
     */
    @Transactional
    public void updateThreshold(Long productId, Integer minStock, Integer maxStock) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("商品不存在: id=" + productId));
        // 仅当明确传入值时才更新
        if (minStock != null) {
            // 校验：下限不能大于上限
            if (maxStock != null && minStock > maxStock) {
                throw new IllegalArgumentException("库存下限不能大于上限");
            }
            if (minStock > (maxStock != null ? maxStock : product.getMaxStock())) {
                throw new IllegalArgumentException("库存下限不能大于上限");
            }
            product.setMinStock(minStock);
        }
        if (maxStock != null) {
            if (minStock == null && product.getMinStock() > maxStock) {
                throw new IllegalArgumentException("库存下限不能大于上限");
            }
            product.setMaxStock(maxStock);
        }
        productRepository.save(product);
    }

    /** 库存盘点：返回所有缺货（库存 <= 下限）的商品 */
    @Transactional(readOnly = true)
    public List<InventoryDTO> checkInventory() {
        List<Product> lowStock = productRepository.findLowStockWarning();
        return lowStock.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Product → InventoryDTO */
    private InventoryDTO toDTO(Product p) {
        InventoryDTO dto = new InventoryDTO();
        dto.setProductId(p.getId());
        dto.setProductName(p.getName());
        dto.setBarcode(p.getBarcode());
        dto.setUnit(p.getUnit());
        dto.setCategoryName(p.getCategory() != null ? p.getCategory().getName() : "");
        dto.setStock(p.getStock());
        dto.setMinStock(p.getMinStock());
        dto.setMaxStock(p.getMaxStock());
        dto.setSalePrice(p.getSalePrice());
        dto.setPurchasePrice(p.getPurchasePrice());
        dto.setStockValue(
            p.getPurchasePrice().multiply(BigDecimal.valueOf(p.getStock()))
        );
        if (p.getStock() <= 0) dto.setStatus("OUT");
        else if (p.getMinStock() != null && p.getStock() <= p.getMinStock()) dto.setStatus("LOW");
        else if (p.getMaxStock() != null && p.getStock() >= p.getMaxStock()) dto.setStatus("HIGH");
        else dto.setStatus("NORMAL");
        return dto;
    }
}
