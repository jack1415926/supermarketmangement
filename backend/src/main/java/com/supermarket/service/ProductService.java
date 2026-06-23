/**
 * 商品服务 —— 商品的增删改查 + 分页搜索。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.dto.PageDTO;
import com.supermarket.dto.ProductDTO;
import com.supermarket.entity.Product;
import com.supermarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 分页查询商品列表，支持关键字搜索。
     *
     * @param page 页码（从 0 开始）
     * @param size 每页大小
     * @param keyword 搜索关键字（可选，null 表示全部）
     * @return 分页结果
     */
    public PageDTO<ProductDTO> findAll(int page, int size, String keyword) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage;
        if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.findByNameContaining(keyword, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }
        return PageDTO.from(productPage.map(this::toDTO));
    }

    /** 按 ID 查询单个商品 */
    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("商品不存在: id=" + id));
    }

    /** 按条形码查询（POS 扫码） */
    public Product findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
            .orElseThrow(() -> new NoSuchElementException("商品不存在, 条形码=" + barcode));
    }

    /** 新增商品 */
    @Transactional
    public Product create(Product product) {
        if (productRepository.existsByBarcode(product.getBarcode())) {
            throw new IllegalArgumentException("条形码已存在: " + product.getBarcode());
        }
        return productRepository.save(product);
    }

    /** 更新商品 */
    @Transactional
    public Product update(Long id, Product updated) {
        Product existing = findById(id);
        if (!existing.getBarcode().equals(updated.getBarcode())
            && productRepository.existsByBarcode(updated.getBarcode())) {
            throw new IllegalArgumentException("条形码已存在: " + updated.getBarcode());
        }
        existing.setBarcode(updated.getBarcode());
        existing.setName(updated.getName());
        existing.setUnit(updated.getUnit());
        existing.setSalePrice(updated.getSalePrice());
        existing.setPurchasePrice(updated.getPurchasePrice());
        existing.setMinStock(updated.getMinStock());
        existing.setMaxStock(updated.getMaxStock());
        existing.setCategory(updated.getCategory());
        existing.setSupplier(updated.getSupplier());
        existing.setStatus(updated.getStatus());
        return productRepository.save(existing);
    }

    /** 下架商品（软删除） */
    @Transactional
    public void deactivate(Long id) {
        Product product = findById(id);
        product.setStatus(0);
        productRepository.save(product);
    }

    /** Entity 转 DTO */
    public ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setBarcode(p.getBarcode());
        dto.setName(p.getName());
        dto.setUnit(p.getUnit());
        dto.setSalePrice(p.getSalePrice());
        dto.setPurchasePrice(p.getPurchasePrice());
        dto.setStock(p.getStock());
        dto.setMinStock(p.getMinStock());
        dto.setMaxStock(p.getMaxStock());
        dto.setStatus(p.getStatus());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        if (p.getSupplier() != null) {
            dto.setSupplierId(p.getSupplier().getId());
            dto.setSupplierName(p.getSupplier().getName());
        }
        // 计算库存状态
        if (p.getStock() <= 0) dto.setStockStatus("OUT");
        else if (p.getMinStock() != null && p.getStock() <= p.getMinStock()) dto.setStockStatus("LOW");
        else if (p.getMaxStock() != null && p.getStock() >= p.getMaxStock()) dto.setStockStatus("HIGH");
        else dto.setStockStatus("NORMAL");
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}
