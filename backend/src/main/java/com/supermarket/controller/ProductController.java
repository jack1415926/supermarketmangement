/**
 * 商品控制器 —— /api/products
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.PageDTO;
import com.supermarket.dto.ProductDTO;
import com.supermarket.entity.Product;
import com.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 分页查询商品列表，支持关键字搜索 */
    @GetMapping
    public Result<PageDTO<ProductDTO>> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword
    ) {
        return Result.success(productService.findAll(page, size, keyword));
    }

    /** 搜索商品（POS 扫码/手动输入时模糊搜索） */
    @GetMapping("/search")
    public Result<PageDTO<ProductDTO>> search(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return Result.success(productService.findAll(page, size, keyword));
    }

    /** 按条形码查询（POS 扫码枪读取） */
    @GetMapping("/barcode/{barcode}")
    public Result<ProductDTO> findByBarcode(@PathVariable String barcode) {
        Product product = productService.findByBarcode(barcode);
        return Result.success(productService.toDTO(product));
    }

    /** 新增商品 */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Product> create(@RequestBody Product product) {
        return Result.success(productService.create(product));
    }

    /** 修改商品 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.update(id, product));
    }

    /** 下架商品 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deactivate(id);
        return Result.success();
    }
}
