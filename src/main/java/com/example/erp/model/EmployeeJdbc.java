package com.example.erp.model;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeJdbc {
    private String employeeId;
    private String name;
    private String surname;
    private String email;
}
