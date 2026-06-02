package com.salarysystem.servlet;

import com.salarysystem.model.sysLog;
import com.salarysystem.model.sysUser;
import com.salarysystem.model.PageResult;
import com.salarysystem.service.impl.SysLogServiceImpl;
import com.salarysystem.service.impl.SysUserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/audit-log")
public class AuditLogServlet extends HttpServlet {

    private final SysLogServiceImpl logService = new SysLogServiceImpl();
    private final SysUserServiceImpl userService = new SysUserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        sysUser currentUser = (sysUser) session.getAttribute("currentUser");

        try {
            // 获取筛选参数
            String actionType = request.getParameter("actionType");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            String userIdStr = request.getParameter("userId");
            int pageNo = parseIntOrDefault(request.getParameter("pageNo"), 1);
            int pageSize = 15;

            Integer filterUserId = null;
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try { filterUserId = Integer.parseInt(userIdStr); } catch (Exception ignored) {}
            }

            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startTime = LocalDate.parse(startDateStr).atStartOfDay();
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endTime = LocalDate.parse(endDateStr).atTime(LocalTime.MAX);
            }

            // 查询日志（分页）
            PageResult<sysLog> pageResult = logService.findByFilters(
                    filterUserId, (actionType != null && !actionType.isEmpty()) ? actionType : null,
                    startTime, endTime, pageNo, pageSize);

            // 获取所有操作类型（供筛选下拉框）
            List<String> allActionTypes = logService.findAllActionTypes();

            request.setAttribute("pageResult", pageResult);
            request.setAttribute("allActionTypes", allActionTypes);
            request.setAttribute("actionType", actionType != null ? actionType : "");
            request.setAttribute("startDate", startDateStr != null ? startDateStr : "");
            request.setAttribute("endDate", endDateStr != null ? endDateStr : "");
            request.setAttribute("filterUserId", userIdStr != null ? userIdStr : "");

            // 记录审计员查看日志的操作
            try {
                logService.log(currentUser.getUserId(), "VIEW_AUDIT_LOG", getClientIp(request));
            } catch (SQLException ignored) {}

            request.getRequestDispatcher("/audit-log.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "审计日志加载失败：" + e.getClass().getSimpleName() + " - " + e.getMessage());
            request.getRequestDispatcher("/audit-log.jsp").forward(request, response);
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return defaultValue; }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
