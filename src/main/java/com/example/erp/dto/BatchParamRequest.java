package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchParamRequest {
    @NotBlank(message = "paramKey can not be null")
    private String paramKey;

    @NotBlank(message = "paramKey can not be null")
    private String paramValue;
}
