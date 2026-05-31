package com.salarysystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class empFamily {
    private Integer familyId;
    private Integer empId;
    private String relation;
    private String name;
    private String idCard;
}
