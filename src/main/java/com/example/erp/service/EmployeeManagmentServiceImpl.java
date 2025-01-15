package com.example.erp.service;


import com.example.erp.dto.EmployeeReq;
import com.example.erp.dto.EmployeeRes;
import com.example.erp.entity.EmployeeEntity;
import com.example.erp.repository.EmployeeManagementRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmployeeManagmentServiceImpl implements EmployeeManagementService {

    private final EmployeeManagementRepository employeeManagementRepository;
    private final ModelMapper modelMapper;
    public EmployeeManagmentServiceImpl(EmployeeManagementRepository employeeManagementRepository, ModelMapper modelMapper) {
        this.employeeManagementRepository = employeeManagementRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public EmployeeRes createEmployee(EmployeeReq employeeReq) {
        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setEmployeeId(employeeReq.getEmployeeId());
        employeeEntity.setName(employeeReq.getName());
        employeeEntity.setEmail(employeeReq.getEmail());
        employeeEntity.setSurname(employeeReq.getSurname());
        employeeEntity.setPhone(employeeReq.getPhone());
        employeeManagementRepository.save(employeeEntity);

        EmployeeRes employeeRes = modelMapper.map(employeeEntity, EmployeeRes.class);
        return employeeRes;
    }

    @Override
    @Transactional
    public EmployeeRes getEmployee(String employeeId) {
        EmployeeEntity employeeEntity = employeeManagementRepository.getByEmployeeId(employeeId);
        EmployeeRes employeeRes = modelMapper.map(employeeEntity, EmployeeRes.class);
        return employeeRes;
    }
}
