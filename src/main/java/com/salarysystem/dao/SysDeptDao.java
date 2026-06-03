package com.salarysystem.dao;

import com.salarysystem.model.sysDept;

import java.sql.SQLException;
import java.util.List;

public interface SysDeptDao extends BaseDao {
    int insert(sysDept dept) throws SQLException;
    int update(sysDept dept) throws SQLException;
    int deleteById(Integer deptId) throws SQLException;
    sysDept findById(Integer deptId) throws SQLException;
    sysDept findByName(String deptName) throws SQLException;
    List<sysDept> findAll() throws SQLException;
    /** 统计某部门下的员工数 */
    int countEmpByDeptName(String deptName) throws SQLException;
}
