package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.EmpFamilyDaoImpl;
import com.salarysystem.model.empFamily;
import com.salarysystem.service.EmpFamilyService;

import java.sql.SQLException;
import java.util.List;

public class EmpFamilyServiceImpl implements EmpFamilyService {

    private final EmpFamilyDaoImpl dao = new EmpFamilyDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    public void add(empFamily family) throws SQLException {
        dao.insert(family);
        try { logService.log(null, "ADD_FAMILY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void update(empFamily family) throws SQLException {
        dao.update(family);
        try { logService.log(null, "UPDATE_FAMILY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void delete(Integer familyId) throws SQLException {
        dao.deleteById(familyId);
        try { logService.log(null, "DELETE_FAMILY", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public empFamily findById(Integer familyId) throws SQLException {
        return dao.findById(familyId);
    }

    @Override
    public List<empFamily> findByEmpId(Integer empId) throws SQLException {
        return dao.findByEmpId(empId);
    }

    @Override
    public List<empFamily> findAll() throws SQLException {
        return dao.findAll();
    }
}

