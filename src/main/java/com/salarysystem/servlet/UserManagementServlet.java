package com.salarysystem.servlet;

import com.salarysystem.model.PageResult;
import com.salarysystem.model.sysRole;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.SysLogServiceImpl;
import com.salarysystem.dao.impl.SysRoleDaoImpl;
import com.salarysystem.service.impl.SysUserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 用户管理 —— 仅系统管理员 (role_id=1) 可访问
 */
@WebServlet("/user-management")
public class UserManagementServlet extends HttpServlet {

    private final SysUserServiceImpl userService = new SysUserServiceImpl();
    private final SysRoleDaoImpl roleDao = new SysRoleDaoImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser currentUser = (sysUser) session.getAttribute("currentUser");
        if (!"系统管理员".equals(session.getAttribute("currentUserRole"))) {
            session.setAttribute("message", "权限不足：仅系统管理员可访问用户管理");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String action = safe(req.getParameter("action"));
        try {
            if ("edit".equals(action)) {
                Integer userId = parseInt(req.getParameter("id"));
                if (userId != null) {
                    sysUser u = userService.findById(userId);
                    req.setAttribute("editUser", u);
                }
            }

            prepareList(req);
            try {
                logService.log(currentUser.getUserId(), "VIEW_USER_MANAGEMENT", req.getRemoteAddr());
            } catch (Exception ignored) {}
            req.getRequestDispatcher("/user-management.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "加载用户列表失败，请稍后重试");
            req.getRequestDispatcher("/user-management.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser currentUser = (sysUser) session.getAttribute("currentUser");
        if (!"系统管理员".equals(session.getAttribute("currentUserRole"))) {
            session.setAttribute("message", "权限不足");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String action = safe(req.getParameter("action"));
        try {
            switch (action) {
                case "save": {
                    Integer userId = parseInt(req.getParameter("userId"));
                    String username = safe(req.getParameter("username"));
                    String password = safe(req.getParameter("password"));
                    Integer roleId = parseInt(req.getParameter("roleId"));
                    Integer empId = parseInt(req.getParameter("empId"));

                    if (username.isEmpty()) {
                        session.setAttribute("message", "用户名不能为空");
                        resp.sendRedirect(req.getContextPath() + "/user-management");
                        return;
                    }

                    if (userId == null) {
                        // 新增用户
                        if (password.isEmpty()) {
                            session.setAttribute("message", "新增用户时密码不能为空");
                            resp.sendRedirect(req.getContextPath() + "/user-management");
                            return;
                        }
                        // 检查用户名唯一性
                        sysUser exist = userService.findByUsername(username);
                        if (exist != null) {
                            session.setAttribute("message", "用户名已存在：" + username);
                            resp.sendRedirect(req.getContextPath() + "/user-management");
                            return;
                        }
                        sysUser newUser = new sysUser();
                        newUser.setUsername(username);
                        newUser.setRoleId(roleId != null ? roleId : 2);
                        newUser.setEmpId(empId);
                        userService.createUser(newUser, password);
                        logService.log(currentUser.getUserId(), "ADD_USER", req.getRemoteAddr());
                        session.setAttribute("message", "用户【" + username + "】创建成功");
                    } else {
                        // 编辑用户
                        sysUser u = userService.findById(userId);
                        if (u == null) {
                            session.setAttribute("message", "用户不存在");
                            resp.sendRedirect(req.getContextPath() + "/user-management");
                            return;
                        }
                        // 检查用户名唯一性（排除自身）
                        sysUser exist = userService.findByUsername(username);
                        if (exist != null && !exist.getUserId().equals(userId)) {
                            session.setAttribute("message", "用户名已存在：" + username);
                            resp.sendRedirect(req.getContextPath() + "/user-management");
                            return;
                        }
                        u.setUsername(username);
                        u.setRoleId(roleId != null ? roleId : u.getRoleId());
                        u.setEmpId(empId);
                        // 如果填写了新密码则更新
                        if (!password.isEmpty()) {
                            u.setPassword(com.salarysystem.util.SmCryptoUtil.hashSm3(password));
                            u.setPwdUpdateTime(java.time.LocalDateTime.now());
                        }
                        userService.updateUser(u);
                        logService.log(currentUser.getUserId(), "UPDATE_USER", req.getRemoteAddr());
                        session.setAttribute("message", "用户【" + username + "】更新成功");
                    }
                    break;
                }
                case "delete": {
                    Integer userId = parseInt(req.getParameter("userId"));
                    if (userId == null) {
                        session.setAttribute("message", "缺少用户ID");
                    } else if (userId.equals(currentUser.getUserId())) {
                        session.setAttribute("message", "不能删除自己");
                    } else {
                        sysUser u = userService.findById(userId);
                        if (u != null) {
                            userService.deleteUser(userId);
                            logService.log(currentUser.getUserId(), "DELETE_USER", req.getRemoteAddr());
                            session.setAttribute("message", "用户【" + u.getUsername() + "】已删除");
                        }
                    }
                    break;
                }
                case "resetPwd": {
                    Integer userId = parseInt(req.getParameter("userId"));
                    String newPwd = safe(req.getParameter("newPassword"));
                    if (userId == null || newPwd.isEmpty()) {
                        session.setAttribute("message", "用户ID和新密码不能为空");
                    } else {
                        try {
                            userService.resetPassword(userId, newPwd);
                            sysUser u = userService.findById(userId);
                            logService.log(currentUser.getUserId(), "RESET_PASSWORD", req.getRemoteAddr());
                            session.setAttribute("message", "用户【" + (u != null ? u.getUsername() : userId) + "】密码已重置");
                        } catch (IllegalArgumentException e) {
                            session.setAttribute("message", "重置失败：" + e.getMessage());
                        }
                    }
                    break;
                }
                case "unlock": {
                    Integer userId = parseInt(req.getParameter("userId"));
                    if (userId != null) {
                        userService.unlockUser(userId);
                        sysUser u = userService.findById(userId);
                        logService.log(currentUser.getUserId(), "UNLOCK_USER", req.getRemoteAddr());
                        session.setAttribute("message", "用户【" + (u != null ? u.getUsername() : userId) + "】已解锁");
                    }
                    break;
                }
                default:
                    session.setAttribute("message", "未知操作：" + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "操作失败，请稍后重试");
        }
        resp.sendRedirect(req.getContextPath() + "/user-management");
    }

    private void prepareList(HttpServletRequest req) throws SQLException {
        String keyword = safe(req.getParameter("keyword"));
        Integer roleId = parseInt(req.getParameter("filterRoleId"));
        int pageNo = Math.max(1, parseInt(req.getParameter("pageNo")) != null ? parseInt(req.getParameter("pageNo")) : 1);
        int pageSize = 10;

        PageResult<sysUser> pageResult = userService.findByFilter(keyword, roleId, pageNo, pageSize);
        List<sysRole> allRoles = roleDao.findAll();

        req.setAttribute("users", pageResult);
        req.setAttribute("allRoles", allRoles);
        req.setAttribute("keyword", keyword);
        req.setAttribute("filterRoleId", roleId != null ? roleId : "");
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return null; }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
