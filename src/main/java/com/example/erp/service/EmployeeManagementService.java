package com.example.erp.service;


import com.example.erp.dto.EmployeeReq;
import com.example.erp.dto.EmployeeRes;

public interface EmployeeManagementService {

    EmployeeRes createEmployee(EmployeeReq employeeReq);

    EmployeeRes getEmployee(String employeeId);

}
