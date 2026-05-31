package com.salarysystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class salaryRecord {
    private Long recordId;
    private Integer empId;
    private String salaryMonth;
    private Integer expectedDays;
    private Integer actualDays;
    private BigDecimal basicSalary;
    private BigDecimal positionAllowance;
    private BigDecimal lunchAllowance;
    private BigDecimal overtimeSalary;
    private BigDecimal fullAttendSalary;
    private BigDecimal socialSecurity;
    private BigDecimal providentFund;
    private BigDecimal tax;
    private BigDecimal absenceDeduction;
    private BigDecimal actualSalary;
}
