/**
 * 员工服务 —— 员工信息的增删改查。
 *
 * @author 徐磊
 */
package com.supermarket.service;

import com.supermarket.entity.Employee;
import com.supermarket.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findByStatus(1);
    }

    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("员工不存在: id=" + id));
    }

    /** 按职位查询（如查询所有收银员） */
    public List<Employee> findByPosition(String position) {
        return employeeRepository.findByPosition(position);
    }

    @Transactional
    public Employee create(Employee employee) {
        if (employee.getPhone() != null && employeeRepository.existsByPhone(employee.getPhone())) {
            throw new IllegalArgumentException("手机号已存在: " + employee.getPhone());
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, Employee updated) {
        Employee existing = findById(id);
        if (updated.getPhone() != null && !updated.getPhone().equals(existing.getPhone())
            && employeeRepository.existsByPhone(updated.getPhone())) {
            throw new IllegalArgumentException("手机号已存在: " + updated.getPhone());
        }
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setPosition(updated.getPosition());
        existing.setSalary(updated.getSalary());
        existing.setHireDate(updated.getHireDate());
        existing.setStatus(updated.getStatus());
        return employeeRepository.save(existing);
    }

    /** 离职处理 */
    @Transactional
    public void deactivate(Long id) {
        Employee employee = findById(id);
        employee.setStatus(0);
        employeeRepository.save(employee);
    }
}
