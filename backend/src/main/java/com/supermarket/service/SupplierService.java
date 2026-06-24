/**
 * 供应商服务 —— 供应商的增删改查。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.entity.Supplier;
import com.supermarket.repository.ProductRepository;
import com.supermarket.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    /** 查询所有合作中的供应商 */
    @Transactional(readOnly = true)
    public List<Supplier> findAll() {
        return supplierRepository.findByStatus(1);
    }

    /** 按 ID 查询 */
    @Transactional(readOnly = true)
    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("供应商不存在: id=" + id));
    }

    /** 新增供应商 */
    @Transactional
    public Supplier create(Supplier supplier) {
        if (supplierRepository.existsByName(supplier.getName())) {
            throw new IllegalArgumentException("供应商名称已存在: " + supplier.getName());
        }
        return supplierRepository.save(supplier);
    }

    /** 更新供应商 */
    @Transactional
    public Supplier update(Long id, Supplier updated) {
        Supplier existing = findById(id);
        if (!existing.getName().equals(updated.getName())
            && supplierRepository.existsByName(updated.getName())) {
            throw new IllegalArgumentException("供应商名称已存在: " + updated.getName());
        }
        existing.setName(updated.getName());
        existing.setContact(updated.getContact());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setStatus(updated.getStatus());
        return supplierRepository.save(existing);
    }

    /** 停用供应商（软删除），检查是否有商品仍引用此供应商 */
    @Transactional
    public void deactivate(Long id) {
        Supplier supplier = findById(id);
        if (!productRepository.findBySupplierId(id).isEmpty()) {
            throw new IllegalArgumentException("该供应商下还有关联商品，请先处理商品再停用供应商");
        }
        supplier.setStatus(0);
        supplierRepository.save(supplier);
    }
}
