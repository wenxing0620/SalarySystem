package com.salarysystem.service;

import com.salarysystem.model.salaryRecord;

import java.sql.SQLException;
import java.util.List;

public interface SalaryRecordService {
    void add(salaryRecord record) throws SQLException;
    void update(salaryRecord record) throws SQLException;
    void delete(Long recordId) throws SQLException;
    salaryRecord findById(Long id) throws SQLException;
    salaryRecord findByEmpIdAndMonth(Integer empId, String salaryMonth) throws SQLException;
    List<salaryRecord> findByEmpId(Integer empId) throws SQLException;
    List<salaryRecord> findAll() throws SQLException;

    /**
     * 一键计算所有员工指定月份的应扣税和实发工资
     * @return 结果描述字符串: "成功 X 人, 跳过 Y 人, 失败 Z 人"
     */
    String calculateAllTaxForMonth(String salaryMonth) throws SQLException;

    /**
     * 计算单个员工指定月份的个税（累计预扣法）
     * @return 计算后的薪资记录（已更新 tax 和 actual_salary）
     */
    salaryRecord calculateTaxForEmployee(Integer empId, String salaryMonth) throws SQLException;
}

