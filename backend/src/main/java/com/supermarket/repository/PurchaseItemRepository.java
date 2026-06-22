/**
 * 进货明细数据访问层 —— 对应 purchase_items 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

    /** 根据进货单 ID 查询所有明细 */
    List<PurchaseItem> findByPurchaseId(Long purchaseId);

    /** 根据商品 ID 查询进货记录（用于追溯某商品的进货历史） */
    List<PurchaseItem> findByProductId(Long productId);

    /** 删除指定进货单的所有明细（用于修改进货单时整体替换） */
    void deleteByPurchaseId(Long purchaseId);
}
