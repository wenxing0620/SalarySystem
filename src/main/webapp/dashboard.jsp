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
    <title>首页 - 薪资管理系统</title>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "dashboard"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <h2 style="margin-bottom:20px;">系统首页</h2>

        <%@ include file="_alerts.jsp" %>

        <div class="dashboard-grid">
            <div class="card">
                <h3>员工总数</h3>
                <div class="number"><%= request.getAttribute("empCount") != null ? request.getAttribute("empCount") : "0" %></div>
            </div>
            <div class="card">
                <h3>系统功能</h3>
                <div class="number" style="font-size:18px;">员工/家属/薪资/扣除</div>
            </div>
            <div class="card">
                <h3>当前用户</h3>
                <div class="number" style="font-size:18px;"><%= user.getUsername() %></div>
            </div>
            <div class="card">
                <h3>安全状态</h3>
                <div class="number" style="font-size:16px;color:#27ae60;">SM3/SM4/HMAC</div>
            </div>
        </div>

        <div class="table-container">
            <h3>最近操作日志</h3>
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
                        <td colspan="4" class="text-center" style="color:#999;padding:20px;">暂无操作日志</td>
                    </tr>
                    <% } else {
                        for (Map<String, String> row : recentLogs) { %>
                    <tr>
                        <td><%= row.get("username") %></td>
                        <td><%= row.get("actionType") %></td>
                        <td><%= row.get("createTime") %></td>
                        <td><%= row.get("ipAddress") %></td>
                    </tr>
                    <% } } %>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
