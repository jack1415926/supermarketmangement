/**
 * 供应商数据访问层 —— 对应 suppliers 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /** 根据供应商名称查找 */
    Optional<Supplier> findByName(String name);

    /** 根据联系人查找 */
    List<Supplier> findByContactContaining(String keyword);

    /** 查询所有合作中的供应商 */
    List<Supplier> findByStatus(Integer status);

    /** 检查供应商名称是否已存在 */
    boolean existsByName(String name);
}
