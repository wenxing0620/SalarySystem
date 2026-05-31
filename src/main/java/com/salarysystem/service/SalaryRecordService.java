package com.salarysystem.service;

import com.salarysystem.model.salaryRecord;

import java.sql.SQLException;
import java.util.List;

public interface SalaryRecordService {
    void add(salaryRecord record) throws SQLException;
    void update(salaryRecord record) throws SQLException;
    salaryRecord findById(Long id) throws SQLException;
    salaryRecord findByEmpIdAndMonth(Integer empId, String salaryMonth) throws SQLException;
    List<salaryRecord> findByEmpId(Integer empId) throws SQLException;
}

