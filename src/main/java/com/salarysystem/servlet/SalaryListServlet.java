package com.salarysystem.servlet;

import com.salarysystem.model.salaryRecord;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.SalaryRecordServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/salary-list")
public class SalaryListServlet extends HttpServlet {

    private final SalaryRecordServiceImpl svc = new SalaryRecordServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // Get current user for logging
        sysUser currentUser = (sysUser) session.getAttribute("currentUser");
        Integer userId = currentUser != null ? currentUser.getUserId() : null;
        String clientIp = getClientIp(request);

        // Get filter parameters
        String month = request.getParameter("month");
        String empName = request.getParameter("empName");
        String dept = request.getParameter("dept");

        try {
            List<salaryRecord> list = svc.findAll();

            // Filter by month if provided
            if (month != null && !month.trim().isEmpty()) {
                String monthFilter = month.trim();
                list.removeIf(r -> !r.getSalaryMonth().equals(monthFilter));
            }

            // Log the query operation
            try {
                logService.log(userId, "QUERY_SALARY", clientIp);
            } catch (SQLException logEx) {
                System.err.println("Failed to log operation: " + logEx.getMessage());
            }

            request.setAttribute("salaryList", list);
            request.setAttribute("month", month);
            request.setAttribute("empName", empName);
            request.setAttribute("dept", dept);
            request.setAttribute("error", null);
        } catch (SQLException e) {
            System.err.println("Failed to load salary records: " + e.getMessage());
            request.setAttribute("salaryList", new ArrayList<>());
            request.setAttribute("error", "无法加载薪资记录: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error loading salary records: " + e.getMessage());
            request.setAttribute("salaryList", new ArrayList<>());
            request.setAttribute("error", "系统错误: " + e.getMessage());
        }

        request.getRequestDispatcher("/salary-list.jsp").forward(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

