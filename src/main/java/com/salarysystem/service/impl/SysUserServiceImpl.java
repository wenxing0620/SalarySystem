package com.salarysystem.service.impl;

import com.salarysystem.dao.impl.SysUserDaoImpl;
import com.salarysystem.dao.impl.SysLogDaoImpl;
import com.salarysystem.model.sysLog;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.SysUserService;
import com.salarysystem.util.SmCryptoUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SysUserServiceImpl implements SysUserService {

    private final SysUserDaoImpl userDao = new SysUserDaoImpl();
    private final SysLogDaoImpl logDao = new SysLogDaoImpl();

    private static final int MAX_FAIL = 5;
    private static final int LOCK_MINUTES = 30;
    private static final int PWD_EXPIRE_DAYS = 90;

    @Override
    public sysUser authenticate(String username, String plainPassword, String ipAddress) throws SQLException {
        sysUser user = userDao.findByUsername(username);
        if (user == null) {
            // optional: log failed attempt with unknown user
            sysLog l = new sysLog();
            l.setUserId(null);
            l.setActionType("LOGIN_FAIL_UNKNOWN_USER");
            l.setIpAddress(ipAddress);
            l.setCreateTime(LocalDateTime.now());
            logDao.insert(l);
            return null;
        }

        // check lock
        if (user.getLockTime() != null && LocalDateTime.now().isBefore(user.getLockTime())) {
            // still locked
            return null;
        }

        String hashed = SmCryptoUtil.hashSm3(plainPassword);
        if (hashed.equalsIgnoreCase(user.getPassword())) {
            // success: reset fail_count and lock_time
            user.setFailCount(0);
            user.setLockTime(null);
            userDao.update(user);

            sysLog l = new sysLog();
            l.setUserId(user.getUserId());
            l.setActionType("LOGIN_SUCCESS");
            l.setIpAddress(ipAddress);
            l.setCreateTime(LocalDateTime.now());
            logDao.insert(l);
            return user;
        } else {
            // failed: increment fail_count
            Integer fail = user.getFailCount() == null ? 0 : user.getFailCount();
            fail++;
            user.setFailCount(fail);
            if (fail >= MAX_FAIL) {
                user.setLockTime(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }
            userDao.update(user);

            sysLog l = new sysLog();
            l.setUserId(user.getUserId());
            l.setActionType("LOGIN_FAIL");
            l.setIpAddress(ipAddress);
            l.setCreateTime(LocalDateTime.now());
            logDao.insert(l);
            return null;
        }
    }

    @Override
    public sysUser findById(Integer userId) throws SQLException {
        return userDao.findById(userId);
    }

    @Override
    public sysUser findByUsername(String username) throws SQLException {
        return userDao.findByUsername(username);
    }

    @Override
    public void createUser(sysUser user, String plainPassword) throws SQLException {
        if (!checkPasswordComplexity(plainPassword)) throw new IllegalArgumentException("密码不满足复杂度要求");
        user.setPassword(SmCryptoUtil.hashSm3(plainPassword));
        user.setPwdUpdateTime(LocalDateTime.now());
        user.setFailCount(0);
        user.setLockTime(null);
        userDao.insert(user);

        sysLog l = new sysLog();
        l.setUserId(user.getUserId());
        l.setActionType("CREATE_USER");
        l.setIpAddress("SYSTEM");
        l.setCreateTime(LocalDateTime.now());
        logDao.insert(l);
    }

    @Override
    public void changePassword(Integer userId, String oldPlain, String newPlain) throws SQLException {
        sysUser user = userDao.findById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        String oldHash = SmCryptoUtil.hashSm3(oldPlain);
        if (!oldHash.equalsIgnoreCase(user.getPassword())) throw new IllegalArgumentException("旧密码不正确");
        if (!checkPasswordComplexity(newPlain)) throw new IllegalArgumentException("新密码不满足复杂度要求");
        user.setPassword(SmCryptoUtil.hashSm3(newPlain));
        user.setPwdUpdateTime(LocalDateTime.now());
        userDao.update(user);

        sysLog l = new sysLog();
        l.setUserId(user.getUserId());
        l.setActionType("CHANGE_PASSWORD");
        l.setIpAddress("SYSTEM");
        l.setCreateTime(LocalDateTime.now());
        logDao.insert(l);
    }

    @Override
    public boolean isPasswordExpired(sysUser user) {
        if (user == null || user.getPwdUpdateTime() == null) return true;
        return user.getPwdUpdateTime().plus(PWD_EXPIRE_DAYS, ChronoUnit.DAYS).isBefore(LocalDateTime.now());
    }

    @Override
    public void lockUser(Integer userId) throws SQLException {
        sysUser u = userDao.findById(userId);
        if (u == null) return;
        u.setLockTime(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        userDao.update(u);
    }

    @Override
    public void unlockUser(Integer userId) throws SQLException {
        sysUser u = userDao.findById(userId);
        if (u == null) return;
        u.setLockTime(null);
        u.setFailCount(0);
        userDao.update(u);
    }

    private boolean checkPasswordComplexity(String pwd) {
        if (pwd == null || pwd.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}

