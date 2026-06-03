package com.salarysystem.servlet;

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

/**
 * 一键计算所有员工指定月份的个税和实发工资
 */
@WebServlet("/salary-calculate-tax")
public class CalculateTaxServlet extends HttpServlet {

    private final SalaryRecordServiceImpl salaryService = new SalaryRecordServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser user = (sysUser) session.getAttribute("currentUser");

        // 总经理只能查看，不能执行计税
        if (user != null && user.getRoleId() != null && user.getRoleId() == 4) {
            session.setAttribute("message", "权限不足：总经理只能查看，不能执行计税操作");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
            return;
        }

        String month = req.getParameter("month");

        if (month == null || month.trim().isEmpty()) {
            session.setAttribute("message", "计税失败：请指定计薪月份");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
            return;
        }

        month = month.trim();
        // 校验月份格式 YYYY-MM
        if (!month.matches("^\\d{4}-\\d{2}$")) {
            session.setAttribute("message", "计税失败：月份格式不正确，请使用 YYYY-MM 格式");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
            return;
        }

        try {
            String result = salaryService.calculateAllTaxForMonth(month);
            session.setAttribute("message", month + " " + result);

            // 记录审计日志
            try {
                logService.log(user.getUserId(), "CALCULATE_TAX", getClientIp(req));
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("message", "计税失败：系统错误 - " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/salary-list");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // GET 请求也允许执行（方便测试），重定向到 POST 逻辑
        doPost(req, resp);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
