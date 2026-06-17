package com.salarysystem.servlet;

import com.salarysystem.model.sysLog;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
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
import java.util.*;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final EmpInfoServiceImpl empService = new EmpInfoServiceImpl();
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
        // 加载最近操作日志（最新 10 条）
        try {
            List<sysLog> recentLogs = logService.findRecent(10);

            // 构建 userId → username 映射
            Map<Integer, String> usernameMap = new HashMap<>();
            try {
                List<sysUser> allUsers = userService.findAll();
                for (sysUser u : allUsers) {
                    usernameMap.put(u.getUserId(), u.getUsername());
                }
            } catch (SQLException ignored) {}

            // 构建展示行：操作人、操作类型、时间、IP
            List<Map<String, String>> logRows = new ArrayList<>();
            for (sysLog l : recentLogs) {
                Map<String, String> row = new LinkedHashMap<>();
                String username = "-";
                if (l.getUserId() != null) {
                    username = usernameMap.getOrDefault(l.getUserId(), "用户#" + l.getUserId());
                }
                row.put("username", username);
                row.put("actionType", l.getActionType() != null ? l.getActionType() : "-");
                row.put("createTime", l.getCreateTime() != null
                        ? l.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : "-");
                row.put("ipAddress", l.getIpAddress() != null ? l.getIpAddress() : "-");
                logRows.add(row);
            }
            request.setAttribute("recentLogs", logRows);
        } catch (SQLException e) {
            request.setAttribute("recentLogs", new ArrayList<>());
        }

        // 记录日志
        try {
            logService.log(currentUser.getUserId(), "VIEW_DASHBOARD", request.getRemoteAddr());
        } catch (SQLException ignored) {}

        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}
