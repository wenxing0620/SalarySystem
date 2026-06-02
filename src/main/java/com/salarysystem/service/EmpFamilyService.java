package com.salarysystem.service;

import com.salarysystem.model.empFamily;

import java.sql.SQLException;
import java.util.List;

public interface EmpFamilyService {
    void add(empFamily family) throws SQLException;
    void update(empFamily family) throws SQLException;
    void delete(Integer familyId) throws SQLException;
    empFamily findById(Integer familyId) throws SQLException;
    List<empFamily> findByEmpId(Integer empId) throws SQLException;
    List<empFamily> findAll() throws SQLException;
}

