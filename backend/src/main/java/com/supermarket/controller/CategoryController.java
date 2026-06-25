/**
 * 分类控制器 —— /api/categories
 *
 * @author 徐磊
 */
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.CategoryDTO;
import com.supermarket.entity.Category;
import com.supermarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 获取分类树（公开或认证后均可访问） */
    @Operation(summary = "获取分类树")
    @GetMapping
    public Result<List<CategoryDTO>> findAll() {
        return Result.success(categoryService.findAllAsTree());
    }

    /** 新增分类（管理员） */
    @Operation(summary = "新增分类")
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Category> create(@RequestBody Category category) {
        return Result.success(categoryService.create(category));
    }

    /** 修改分类 */
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        return Result.success(categoryService.update(id, category));
    }

    /** 删除分类 */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
