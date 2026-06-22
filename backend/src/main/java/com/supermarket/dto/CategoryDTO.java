/**
 * 分类 DTO —— 用于分类树的 API 响应。
 *
 * 每个节点包含自己的信息 + 子分类列表，形成嵌套的树形结构。
 *
 * JSON 示例：
 * {
 *   "id": 1, "name": "食品", "sortOrder": 1,
 *   "children": [
 *     { "id": 2, "name": "休闲食品", "children": [...] }
 *   ]
 * }
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;

    /** 子分类列表（递归嵌套，形成树形结构） */
    private List<CategoryDTO> children = new ArrayList<>();
}
