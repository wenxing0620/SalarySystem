package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.SalaryRecordDaoImpl;
import com.salarysystem.model.salaryRecord;
import com.salarysystem.service.SalaryRecordService;

import java.sql.SQLException;
import java.util.List;

public class SalaryRecordServiceImpl implements SalaryRecordService {

    private final SalaryRecordDaoImpl dao = new SalaryRecordDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    public void add(salaryRecord record) throws SQLException {
        dao.insert(record);
        try { logService.log(null, "ADD_SALARY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void update(salaryRecord record) throws SQLException {
        dao.update(record);
        try { logService.log(null, "UPDATE_SALARY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public salaryRecord findById(Long id) throws SQLException {
        return dao.findById(id);
    }

    @Override
    public salaryRecord findByEmpIdAndMonth(Integer empId, String salaryMonth) throws SQLException {
        return dao.findByEmpIdAndMonth(empId, salaryMonth);
    }

    @Override
    public List<salaryRecord> findByEmpId(Integer empId) throws SQLException {
        return dao.findByEmpId(empId);
    }

    public List<salaryRecord> findAll() throws SQLException {
        return dao.findAll();
    }
}
