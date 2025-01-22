package com.example.erp.model;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCsv {
    private String employeeId;
    private String name;
    private String surname;
    private String email;
}
