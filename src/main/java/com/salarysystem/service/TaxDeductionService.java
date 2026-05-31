package com.salarysystem.service;

import com.salarysystem.model.taxDeduction;

import java.sql.SQLException;
import java.util.List;

public interface TaxDeductionService {
    void addOrUpdate(taxDeduction deduction) throws SQLException;
    taxDeduction findById(Integer id) throws SQLException;
    taxDeduction findByEmpIdAndYear(Integer empId, Integer year) throws SQLException;
    List<taxDeduction> findByEmpId(Integer empId) throws SQLException;
    List<taxDeduction> findAll() throws SQLException;
    void delete(Integer deductionId) throws SQLException;
}

