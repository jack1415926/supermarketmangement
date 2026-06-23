/**
 * 库存服务 —— 库存查询、预警阈值设置、盘点。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.InventoryDTO;
import com.supermarket.entity.Product;
import com.supermarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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

    /** 查询所有商品的库存信息 */
    public List<InventoryDTO> findAll() {
        return productRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /** 更新商品的库存预警阈值 */
    @Transactional
    public void updateThreshold(Long productId, Integer minStock, Integer maxStock) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("商品不存在: id=" + productId));
        product.setMinStock(minStock);
        product.setMaxStock(maxStock);
        productRepository.save(product);
    }

    /** 库存盘点：检查所有缺货（库存 <= 下限）的商品 */
    public List<InventoryDTO> checkInventory() {
        List<Product> lowStock = productRepository.findByStockLessThanEqualMinStock();
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
        // 库存金额 = 数量 × 进价
        dto.setStockValue(
            p.getPurchasePrice().multiply(BigDecimal.valueOf(p.getStock()))
        );
        // 库存状态
        if (p.getStock() <= 0) dto.setStatus("OUT");
        else if (p.getMinStock() != null && p.getStock() <= p.getMinStock()) dto.setStatus("LOW");
        else if (p.getMaxStock() != null && p.getStock() >= p.getMaxStock()) dto.setStatus("HIGH");
        else dto.setStatus("NORMAL");
        return dto;
    }
}
