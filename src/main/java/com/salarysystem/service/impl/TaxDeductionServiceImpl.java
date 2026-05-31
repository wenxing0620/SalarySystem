package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.TaxDeductionDaoImpl;
import com.salarysystem.model.taxDeduction;
import com.salarysystem.service.TaxDeductionService;

import java.sql.SQLException;
import java.util.List;

public class TaxDeductionServiceImpl implements TaxDeductionService {

    private final TaxDeductionDaoImpl dao = new TaxDeductionDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    public void addOrUpdate(taxDeduction deduction) throws SQLException {
        if (deduction.getDeductionId() == null) dao.insert(deduction);
        else dao.update(deduction);
        try { logService.log(null, "UPDATE_DEDUCTION", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public taxDeduction findById(Integer id) throws SQLException {
        return dao.findById(id);
    }

    @Override
    public taxDeduction findByEmpIdAndYear(Integer empId, Integer year) throws SQLException {
        return dao.findByEmpIdAndYear(empId, year);
    }

    @Override
    public List<taxDeduction> findByEmpId(Integer empId) throws SQLException {
        return dao.findByEmpId(empId);
    }

    @Override
    public List<taxDeduction> findAll() throws SQLException {
        return dao.findAll();
    }

    @Override
    public void delete(Integer deductionId) throws SQLException {
        dao.deleteById(deductionId);
        try { logService.log(null, "DELETE_DEDUCTION", "SYSTEM"); } catch (SQLException ignored) {}
    }
}

