package com.salarysystem.servlet;

import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/emp-delete")
public class EmpDeleteServlet extends HttpServlet {

    private final EmpInfoServiceImpl svc = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");

        // Get current user from session
        HttpSession session = request.getSession(false);
        sysUser currentUser = null;
        Integer userId = null;
        if (session != null) {
            currentUser = (sysUser) session.getAttribute("currentUser");
            if (currentUser != null) {
                userId = currentUser.getUserId();
                // 总经理只能查看，不能删除
                if ("总经理".equals(session.getAttribute("currentUserRole"))) {
                    session.setAttribute("message", "权限不足：总经理只能查看，不能删除员工");
                    response.sendRedirect(request.getContextPath() + "/emp-list");
                    return;
                }
            }
        }
        String clientIp = getClientIp(request);

        if (id != null) {
            try {
                svc.delete(Integer.parseInt(id));
                // Log the operation
                try {
                    logService.log(userId, "DELETE_EMP", clientIp);
                    System.out.println("Logged: user " + userId + " deleted employee " + id);
                } catch (SQLException logEx) {
                    System.err.println("Failed to log operation: " + logEx.getMessage());
                }
            } catch (SQLException ignored) {
                System.err.println("Failed to delete employee: " + ignored.getMessage());
            }
        }
        response.sendRedirect(request.getContextPath() + "/emp-list");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

