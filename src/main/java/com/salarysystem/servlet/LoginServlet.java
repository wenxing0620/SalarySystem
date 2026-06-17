package com.salarysystem.servlet;

import com.salarysystem.dao.impl.SysRoleDaoImpl;
import com.salarysystem.model.sysRole;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.SysUserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final SysUserServiceImpl userService = new SysUserServiceImpl();
    private final SysRoleDaoImpl roleDao = new SysRoleDaoImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String clientIp = getClientIp(request);

        if (username == null || password == null) {
            request.setAttribute("error", "用户名或密码不能为空");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            sysUser user = userService.authenticate(username, password, clientIp);
            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("currentUser", user);

                // 加载角色名称存入 session，供后续权限判断使用
                try {
                    sysRole role = roleDao.findById(user.getRoleId());
                    session.setAttribute("currentUserRole", role != null ? role.getRoleName() : "未知");
                } catch (Exception e) {
                    session.setAttribute("currentUserRole", "未知");
                }

                // 检查密码是否过期（90天）
                if (userService.isPasswordExpired(user)) {
                    session.setAttribute("pendingPasswordChange", true);
                    response.sendRedirect(request.getContextPath() + "/change-password");
                } else {
                    response.sendRedirect(request.getContextPath() + "/dashboard");
                }
            } else {
                request.setAttribute("error", "用户名或密码错误，或账号已锁定");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("doesn't exist") || msg.contains("does not exist")) {
                request.setAttribute("error", "数据库表不存在，请先初始化数据库");
            } else {
                request.setAttribute("error", "系统错误，请稍后重试");
            }
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

