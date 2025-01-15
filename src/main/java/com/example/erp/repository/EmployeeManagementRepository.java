package com.example.erp.repository;

import com.example.erp.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeManagementRepository extends JpaRepository<EmployeeEntity, Long> {
    EmployeeEntity getByEmployeeId(String employeeId);

}
