package com.salarysystem.service;

import com.salarysystem.model.sysDept;

import java.sql.SQLException;
import java.util.List;

public interface SysDeptService {
    void add(sysDept dept) throws SQLException;
    void update(sysDept dept) throws SQLException;
    void delete(Integer deptId) throws SQLException;
    sysDept findById(Integer deptId) throws SQLException;
    List<sysDept> findAll() throws SQLException;
    /** 删除部门前检查是否有关联员工，返回错误消息（null 表示可以删除） */
    String checkBeforeDelete(Integer deptId) throws SQLException;
}
