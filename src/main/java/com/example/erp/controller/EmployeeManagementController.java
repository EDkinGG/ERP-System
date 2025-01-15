package com.example.erp.controller;


import com.example.erp.dto.EmployeeReq;
import com.example.erp.dto.EmployeeRes;
import com.example.erp.service.EmployeeManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/employee")
public class EmployeeManagementController {
    private final EmployeeManagementService employeeManagementService;

    public EmployeeManagementController(EmployeeManagementService employeeManagementService) {
        this.employeeManagementService = employeeManagementService;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeRes> getEmployee(@PathVariable String employeeId) {
        EmployeeRes employeeRes = employeeManagementService.getEmployee(employeeId);
        return ResponseEntity.ok(employeeRes);
    }

    @PostMapping
    public ResponseEntity<EmployeeRes> createEmployee(@RequestBody @Validated EmployeeReq employeeReq) {
        EmployeeRes employeeRes = employeeManagementService.createEmployee(employeeReq);
        return ResponseEntity.ok(employeeRes);
    }

}
