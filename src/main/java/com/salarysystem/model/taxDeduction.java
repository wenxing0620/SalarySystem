package com.salarysystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class taxDeduction {
    private Integer deductionId;
    private Integer empId;
    private Integer declareYear;
    private BigDecimal childEdu;
    private BigDecimal contEdu;
    private BigDecimal majorMed;
    private BigDecimal housingLoan;
    private BigDecimal housingRent;
    private BigDecimal supportElderly;
    private BigDecimal babyCare;
}

