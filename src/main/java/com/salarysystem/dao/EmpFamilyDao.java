package com.salarysystem.dao;

import com.salarysystem.model.empFamily;

import java.sql.SQLException;
import java.util.List;

public interface EmpFamilyDao extends com.salarysystem.BaseDao {
    int insert(empFamily family) throws SQLException;
    int update(empFamily family) throws SQLException;
    int deleteById(Integer familyId) throws SQLException;
    empFamily findById(Integer familyId) throws SQLException;
    List<empFamily> findByEmpId(Integer empId) throws SQLException;
    List<empFamily> findAll() throws SQLException;
}

