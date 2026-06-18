<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, java.util.List, java.util.Map, java.util.HashMap" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    @SuppressWarnings("unchecked")
    List<Map<String, String>> recentLogs = (List<Map<String, String>>) request.getAttribute("recentLogs");
    if (recentLogs == null) recentLogs = new java.util.ArrayList<>();

    // 操作类型中英文映射
    Map<String, String> actionTypeMap = new HashMap<>();
    actionTypeMap.put("LOGIN_SUCCESS", "登录成功");
    actionTypeMap.put("LOGIN_FAIL", "登录失败");
    actionTypeMap.put("LOGIN_FAIL_UNKNOWN_USER", "登录失败(未知用户)");
    actionTypeMap.put("LOGOUT", "登出");
    actionTypeMap.put("ADD_EMP", "新增员工");
    actionTypeMap.put("UPDATE_EMP", "修改员工");
    actionTypeMap.put("DELETE_EMP", "删除员工");
    actionTypeMap.put("VIEW_EMP", "查看员工");
    actionTypeMap.put("ADD_DEPT", "新增部门");
    actionTypeMap.put("UPDATE_DEPT", "修改部门");
    actionTypeMap.put("DELETE_DEPT", "删除部门");
    actionTypeMap.put("ADD_FAMILY", "新增家属");
    actionTypeMap.put("UPDATE_FAMILY", "修改家属");
    actionTypeMap.put("DELETE_FAMILY", "删除家属");
    actionTypeMap.put("QUERY_FAMILY", "查询家属");
    actionTypeMap.put("ADD_DEDUCTION", "新增专项扣除");
    actionTypeMap.put("UPDATE_DEDUCTION", "修改专项扣除");
    actionTypeMap.put("DELETE_DEDUCTION", "删除专项扣除");
    actionTypeMap.put("QUERY_DEDUCTION", "查询专项扣除");
    actionTypeMap.put("ADD_SALARY", "新增薪资");
    actionTypeMap.put("UPDATE_SALARY", "修改薪资");
    actionTypeMap.put("DELETE_SALARY", "删除薪资");
    actionTypeMap.put("QUERY_SALARY", "查询薪资");
    actionTypeMap.put("CALCULATE_TAX", "计税");
    actionTypeMap.put("IMPORT_SALARY", "导入薪资");
    actionTypeMap.put("EXPORT_SALARY", "导出薪资");
    actionTypeMap.put("CREATE_USER", "创建用户");
    actionTypeMap.put("UPDATE_USER", "修改用户");
    actionTypeMap.put("DELETE_USER", "删除用户");
    actionTypeMap.put("RESET_PASSWORD", "重置密码");
    actionTypeMap.put("CHANGE_PASSWORD", "修改密码");
    actionTypeMap.put("UNLOCK_USER", "解锁用户");
    actionTypeMap.put("ADD_ROLE", "新增角色");
    actionTypeMap.put("UPDATE_ROLE", "修改角色");
    actionTypeMap.put("DELETE_ROLE", "删除角色");
    actionTypeMap.put("VIEW_AUDIT_LOG", "查看审计日志");
    actionTypeMap.put("VIEW_DASHBOARD", "查看首页");
    actionTypeMap.put("VIEW_USER_MANAGEMENT", "查看用户管理");
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
                        <td><span class="<%= badgeClass %>"><%= actionTypeMap.getOrDefault(actionType, actionType) %></span></td>
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
