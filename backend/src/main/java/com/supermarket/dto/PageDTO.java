/**
 * 通用分页响应 DTO —— 所有分页查询接口的统一返回格式。
 *
 * 泛型 <T> 表示列表中每条数据的类型。
 * 例如 PageDTO<ProductDTO> 表示一个包含商品数据的分页结果。
 *
 * 类似 C++ 的 template<typename T> struct Page { vector<T> content; int totalPages; };
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page; // Spring Data 的分页对象

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

    /** 当前页的数据列表 */
    private List<T> content;

    /** 当前页码（从 0 开始） */
    private int currentPage;

    /** 每页大小 */
    private int pageSize;

    /** 总页数 */
    private int totalPages;

    /** 总记录数 */
    private long totalElements;

    /** 是否是第一页 */
    private boolean first;

    /** 是否是最后一页 */
    private boolean last;

    /**
     * 从 Spring Data 的 Page 对象构造分页响应。
     *
     * 用法：PageDTO<ProductDTO> result = PageDTO.from(productPage);
     *
     * @param page Spring Data JPA 的分页查询结果
     * @param <T>  数据类型
     * @return 统一格式的分页响应
     */
    public static <T> PageDTO<T> from(Page<T> page) {
        PageDTO<T> dto = new PageDTO<>();
        dto.setContent(page.getContent());
        dto.setCurrentPage(page.getNumber());
        dto.setPageSize(page.getSize());
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        return dto;
    }
}
