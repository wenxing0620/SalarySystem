package com.salarysystem.service;

import com.salarysystem.model.empInfo;

import java.sql.SQLException;
import java.util.List;

public interface EmpInfoService {
    void create(empInfo emp) throws SQLException;
    void update(empInfo emp) throws SQLException;
    void delete(Integer empId) throws SQLException;
    empInfo findById(Integer empId) throws SQLException;
    empInfo findByEmpNo(String empNo) throws SQLException;
    List<empInfo> findAll() throws SQLException;
}

