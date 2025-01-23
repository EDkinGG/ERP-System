package com.example.erp.service;

import com.example.erp.model.EmployeeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    List<EmployeeResponse> list;

    public List<EmployeeResponse> restCallToGetEmployees() {
        RestTemplate restTemplate = new RestTemplate();
        EmployeeResponse[] employeeResponses =
                restTemplate.getForObject("http://localhost:8081/api/v1/employee",
                        EmployeeResponse[].class);

        list = new ArrayList<EmployeeResponse>();

        for (EmployeeResponse employeeResponse : employeeResponses) {
            list.add(employeeResponse);
        }
        return list;
    }

    public EmployeeResponse getEmployee(Long id, String name ) {
        System.out.println("getEmployees id "+id+" name "+name);
        if ( list == null )
        {
            restCallToGetEmployees();
        }
        if(list != null && !list.isEmpty() )
        {
            return list.remove(0);
        }
        return null;
    }
}
