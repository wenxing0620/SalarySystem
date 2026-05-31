package com.salarysystem.servlet;

import com.salarysystem.model.empInfo;
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

@WebServlet("/emp-edit")
public class EmpEditServlet extends HttpServlet {

    private final EmpInfoServiceImpl svc = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/emp-list");
            return;
        }
        try {
            empInfo e = svc.findById(Integer.parseInt(id));
            request.setAttribute("emp", e);
            request.getRequestDispatcher("/emp-edit.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/emp-edit.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Get current user from session
        HttpSession session = request.getSession(false);
        sysUser currentUser = null;
        Integer userId = null;
        if (session != null) {
            currentUser = (sysUser) session.getAttribute("currentUser");
            if (currentUser != null) {
                userId = currentUser.getUserId();
            }
        }
        String clientIp = getClientIp(request);

        empInfo e = new empInfo();
        e.setEmpId(Integer.parseInt(request.getParameter("empId")));
        e.setEmpNo(request.getParameter("empNo"));
        e.setDeptName(request.getParameter("deptName"));
        e.setPosition(request.getParameter("position"));
        e.setEmpName(request.getParameter("empName"));
        e.setIdCard(request.getParameter("idCard"));
        e.setPhone(request.getParameter("phone"));
        e.setAddress(request.getParameter("address"));
        try {
            svc.update(e);
            // Log the operation
            try {
                logService.log(userId, "UPDATE_EMP", clientIp);
                System.out.println("Logged: user " + userId + " updated employee " + e.getEmpNo());
            } catch (SQLException logEx) {
                System.err.println("Failed to log operation: " + logEx.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/emp-list");
        } catch (SQLException ex) {
            request.setAttribute("error", "更新失败：" + ex.getMessage());
            request.getRequestDispatcher("/emp-edit.jsp").forward(request, response);
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

