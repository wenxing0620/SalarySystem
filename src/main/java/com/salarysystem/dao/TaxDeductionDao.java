package com.salarysystem.dao;

import com.salarysystem.model.taxDeduction;

import java.sql.SQLException;
import java.util.List;

public interface TaxDeductionDao extends com.salarysystem.BaseDao {
    int insert(taxDeduction deduction) throws SQLException;
    int update(taxDeduction deduction) throws SQLException;
    int deleteById(Integer deductionId) throws SQLException;
    taxDeduction findById(Integer deductionId) throws SQLException;
    taxDeduction findByEmpIdAndYear(Integer empId, Integer year) throws SQLException;
    List<taxDeduction> findByEmpId(Integer empId) throws SQLException;
}

