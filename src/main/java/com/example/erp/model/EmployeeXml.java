package com.example.erp.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "employee")
public class EmployeeXml {
    //@XmlElement(name = "employee_id")
    private String employee_id;

    //@XmlElement(name = "name")
    private String name;

    //@XmlElement(name = "surname")
    private String surname;

    //@XmlElement(name = "email")
    private String email;
}
