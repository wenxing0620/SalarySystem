<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, java.util.List, java.util.Map" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    @SuppressWarnings("unchecked")
    List<Map<String, String>> recentLogs = (List<Map<String, String>>) request.getAttribute("recentLogs");
    if (recentLogs == null) recentLogs = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>系统首页 — 薪资管理系统</title>
    <style>
        .welcome-banner {
            background: linear-gradient(135deg, #1e293b 0%, #312e81 100%);
            border-radius: var(--radius);
            padding: 28px 32px;
            margin-bottom: 24px;
            color: #fff;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: var(--shadow-md);
            position: relative;
            overflow: hidden;
        }
        .welcome-banner::after {
            content: '';
            position: absolute;
            right: -40px; top: -40px;
            width: 180px; height: 180px;
            border-radius: 50%;
            background: rgba(255,255,255,0.04);
        }
        .welcome-banner h2 { font-size: 22px; font-weight: 700; letter-spacing: -0.4px; color: #fff; }
        .welcome-banner .welcome-sub { font-size: 13px; color: rgba(255,255,255,0.6); margin-top: 4px; }
        .welcome-date { font-size: 13px; color: rgba(255,255,255,0.5); text-align: right; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "dashboard"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <!-- 欢迎横幅 -->
        <div class="welcome-banner">
            <div>
                <h2>欢迎回来，<%= user.getUsername() %></h2>
                <div class="welcome-sub">薪资管理系统</div>
            </div>
            <div class="welcome-date">
                <%= java.time.LocalDate.now().toString() %>
            </div>
        </div>

        <%@ include file="_alerts.jsp" %>


        <!-- 最近日志 -->
        <div class="table-container">
            <h3>最近操作日志</h3>
            <div style="padding:0;">
            <table>
                <thead>
                    <tr>
                        <th>操作人</th>
                        <th>操作类型</th>
                        <th>操作时间</th>
                        <th>IP 地址</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (recentLogs.isEmpty()) { %>
                    <tr>
                        <td colspan="4" style="text-align:center;padding:30px;color:var(--c-faint);">
                            暂无操作日志
                        </td>
                    </tr>
                    <% } else {
                        for (Map<String, String> row : recentLogs) {
                            String actionType = row.get("actionType");
                            String badgeClass = "badge";
                            if (actionType != null) {
                                if (actionType.contains("SUCCESS") || actionType.contains("ADD") || actionType.contains("UPDATE")) badgeClass += " badge-success";
                                else if (actionType.contains("FAIL") || actionType.contains("DELETE")) badgeClass += " badge-danger";
                                else if (actionType.contains("EXPORT") || actionType.contains("IMPORT")) badgeClass += " badge-info";
                                else badgeClass += " badge-info";
                            }
                    %>
                    <tr>
                        <td><strong><%= row.get("username") %></strong></td>
                        <td><span class="<%= badgeClass %>"><%= actionType %></span></td>
                        <td style="color:var(--c-muted);"><%= row.get("createTime") %></td>
                        <td style="color:var(--c-faint);font-family:monospace;font-size:12px;"><%= row.get("ipAddress") %></td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>
