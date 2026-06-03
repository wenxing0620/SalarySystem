package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.SysDeptDaoImpl;
import com.salarysystem.model.sysDept;
import com.salarysystem.service.SysDeptService;

import java.sql.SQLException;
import java.util.List;

public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptDaoImpl dao = new SysDeptDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    public void add(sysDept dept) throws SQLException {
        // 检查部门名称是否已存在
        if (dao.findByName(dept.getDeptName()) != null) {
            throw new SQLException("部门名称【" + dept.getDeptName() + "】已存在");
        }
        dao.insert(dept);
        try { logService.log(null, "ADD_DEPT", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void update(sysDept dept) throws SQLException {
        // 检查名称是否与其他部门冲突
        sysDept exist = dao.findByName(dept.getDeptName());
        if (exist != null && !exist.getDeptId().equals(dept.getDeptId())) {
            throw new SQLException("部门名称【" + dept.getDeptName() + "】已被其他部门使用");
        }
        // 同时更新 emp_info 中的 dept_name（保持引用一致性）
        sysDept old = dao.findById(dept.getDeptId());
        if (old != null && !old.getDeptName().equals(dept.getDeptName())) {
            updateEmpDeptName(old.getDeptName(), dept.getDeptName());
        }
        dao.update(dept);
        try { logService.log(null, "UPDATE_DEPT", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public void delete(Integer deptId) throws SQLException {
        String error = checkBeforeDelete(deptId);
        if (error != null) {
            throw new SQLException(error);
        }
        dao.deleteById(deptId);
        try { logService.log(null, "DELETE_DEPT", "SYSTEM"); } catch (SQLException ignored) {}
    }

    @Override
    public sysDept findById(Integer deptId) throws SQLException {
        return dao.findById(deptId);
    }

    @Override
    public List<sysDept> findAll() throws SQLException {
        return dao.findAll();
    }

    @Override
    public String checkBeforeDelete(Integer deptId) throws SQLException {
        sysDept dept = dao.findById(deptId);
        if (dept == null) {
            return "部门不存在";
        }
        int count = dao.countEmpByDeptName(dept.getDeptName());
        if (count > 0) {
            return "部门【" + dept.getDeptName() + "】下还有 " + count + " 名员工，请先转移或删除员工";
        }
        return null;
    }

    /**
     * 更新 emp_info 表中所有属于旧部门名称的记录为新名称
     */
    private void updateEmpDeptName(String oldName, String newName) throws SQLException {
        String sql = "UPDATE emp_info SET dept_name=? WHERE dept_name=?";
        try (java.sql.Connection conn = dao.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            ps.executeUpdate();
        }
    }
}
