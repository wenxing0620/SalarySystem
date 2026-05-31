package com.salarysystem.dao;

import com.salarysystem.model.salaryRecord;

import java.sql.SQLException;
import java.util.List;

public interface SalaryRecordDao extends com.salarysystem.BaseDao {
    int insert(salaryRecord record) throws SQLException;
    int update(salaryRecord record) throws SQLException;
    int deleteById(Long recordId) throws SQLException;
    salaryRecord findById(Long recordId) throws SQLException;
    salaryRecord findByEmpIdAndMonth(Integer empId, String salaryMonth) throws SQLException;
    List<salaryRecord> findByEmpId(Integer empId) throws SQLException;
    List<salaryRecord> findAll() throws SQLException;
}

