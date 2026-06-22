/**
 * 员工数据访问层 —— 对应 employees 表。
 *
 * @author 徐磊
 */
package com.supermarket.repository;

import com.supermarket.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /** 根据手机号查找员工（手机号唯一） */
    Optional<Employee> findByPhone(String phone);

    /** 根据职位查找员工（如查询所有收银员） */
    List<Employee> findByPosition(String position);

    /** 查询在职/离职员工 */
    List<Employee> findByStatus(Integer status);

    /** 检查手机号是否已存在 */
    boolean existsByPhone(String phone);
}
