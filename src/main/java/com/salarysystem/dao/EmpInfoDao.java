package com.salarysystem.dao;

import com.salarysystem.model.empInfo;

import java.sql.SQLException;
import java.util.List;

public interface EmpInfoDao extends com.salarysystem.BaseDao {
    int insert(empInfo emp) throws SQLException;
    int update(empInfo emp) throws SQLException;
    int deleteById(Integer empId) throws SQLException;
    empInfo findById(Integer empId) throws SQLException;
    empInfo findByEmpNo(String empNo) throws SQLException;
    List<empInfo> findAll() throws SQLException;
}

