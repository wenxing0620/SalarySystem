package com.salarysystem.service;

import com.salarysystem.model.sysUser;

import java.sql.SQLException;

public interface SysUserService {
    /**
     * Authenticate user with plain password. Returns user on success, null on failure.
     */
    sysUser authenticate(String username, String plainPassword, String ipAddress) throws SQLException;

    sysUser findById(Integer userId) throws SQLException;

    sysUser findByUsername(String username) throws SQLException;

    void createUser(sysUser user, String plainPassword) throws SQLException;

    void changePassword(Integer userId, String oldPlain, String newPlain) throws SQLException;

    boolean isPasswordExpired(sysUser user);

    void lockUser(Integer userId) throws SQLException;

    void unlockUser(Integer userId) throws SQLException;
}

