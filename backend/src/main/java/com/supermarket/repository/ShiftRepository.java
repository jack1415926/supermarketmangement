/**
 * 换班记录数据访问层 —— 对应 shifts 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /** 查找某收银员当前活跃的班次（用于交班操作） */
    Optional<Shift> findByCashierIdAndStatus(Long cashierId, String status);

    /** 查找某收银员的所有班次记录（分页） */
    List<Shift> findByCashierIdOrderByStartTimeDesc(Long cashierId);

    /** 按日期范围查询所有换班记录 */
    @Query("SELECT s FROM Shift s WHERE s.startTime >= :start AND s.startTime <= :end ORDER BY s.startTime DESC")
    List<Shift> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 查询所有活跃中的班次 */
    List<Shift> findByStatus(String status);
}
