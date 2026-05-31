package com.salarysystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class empInfo {
    private Integer empId;
    private String empNo;
    private String deptName;
    private String position;
    private String empName;
    private String idCard;
    private String phone;
    private String address;
    private String dataHash;
}
