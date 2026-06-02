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
    /** 查询某员工在指定月份范围内的所有薪资记录（按月份升序） */
    List<salaryRecord> findByEmpIdAndMonthRange(Integer empId, String startMonth, String endMonth) throws SQLException;
    /** 查询某员工在某年已缴纳的个税总额（不含目标月份） */
    java.math.BigDecimal sumTaxByEmpIdAndYearExcludeMonth(Integer empId, int year, String excludeMonth) throws SQLException;
}

