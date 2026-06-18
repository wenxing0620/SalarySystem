<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, com.salarysystem.model.sysLog, com.salarysystem.model.PageResult" %>
<%@ page import="java.util.List, java.util.Map, java.util.HashMap" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    @SuppressWarnings("unchecked")
    PageResult<sysLog> pageResult = (PageResult<sysLog>) request.getAttribute("pageResult");

    @SuppressWarnings("unchecked")
    List<String> allActionTypes = (List<String>) request.getAttribute("allActionTypes");
    if (allActionTypes == null) allActionTypes = new java.util.ArrayList<>();

    String actionType = request.getAttribute("actionType") != null ? request.getAttribute("actionType").toString() : "";
    String startDate = request.getAttribute("startDate") != null ? request.getAttribute("startDate").toString() : "";
    String endDate = request.getAttribute("endDate") != null ? request.getAttribute("endDate").toString() : "";
    String filterUserId = request.getAttribute("filterUserId") != null ? request.getAttribute("filterUserId").toString() : "";

    @SuppressWarnings("unchecked")
    Map<Integer, String> userNameMap = (Map<Integer, String>) request.getAttribute("userNameMap");
    if (userNameMap == null) userNameMap = new HashMap<>();

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

    // Build query string for pagination links
    String querySuffix = "";
    if (!startDate.isEmpty()) querySuffix += "&startDate=" + startDate;
    if (!endDate.isEmpty()) querySuffix += "&endDate=" + endDate;
    if (!actionType.isEmpty()) querySuffix += "&actionType=" + actionType;
    if (!filterUserId.isEmpty()) querySuffix += "&userId=" + filterUserId;
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>审计日志 - 薪资管理系统</title>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "audit-log"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>审计日志</h2>
        </div>

        <%@ include file="_alerts.jsp" %>

        <form class="filter-box" method="get" action="<%= request.getContextPath() %>/audit-log">
            <div class="form-group" style="margin-bottom:0;">
                <label>开始日期</label>
                <input type="date" name="startDate" value="<%= startDate %>" style="width:auto;">
            </div>
            <div class="form-group" style="margin-bottom:0;">
                <label>结束日期</label>
                <input type="date" name="endDate" value="<%= endDate %>" style="width:auto;">
            </div>
            <div class="form-group" style="margin-bottom:0;">
                <label>操作类型</label>
                <select name="actionType" style="width:auto;">
                    <option value="">全部</option>
                    <% for (String t : allActionTypes) { %>
                    <option value="<%= t %>" <%= t.equals(actionType) ? "selected" : "" %>><%= actionTypeMap.getOrDefault(t, t) %></option>
                    <% } %>
                </select>
            </div>
            <div class="form-group" style="margin-bottom:0;">
                <label>用户ID</label>
                <input type="number" name="userId" value="<%= filterUserId %>" placeholder="用户ID" style="width:90px;">
            </div>
            <button type="submit" class="btn btn-primary">查询</button>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/audit-log">重置</a>
        </form>

        <div class="table-container">
            <h3>操作日志</h3>

            <% if (pageResult != null && pageResult.getTotalCount() > 0) { %>
            <div class="info-text">共 <%= pageResult.getTotalCount() %> 条记录，第 <%= pageResult.getPageNo() %>/<%= pageResult.getTotalPages() %> 页</div>
            <table>
                <thead>
                <tr>
                    <th style="width:80px;">序号</th>
                    <th>操作人</th>
                    <th>操作类型</th>
                    <th>IP地址</th>
                    <th>操作时间</th>
                    <th>HMAC校验</th>
                </tr>
                </thead>
                <tbody>
                <% int index = 1; for (sysLog log : pageResult.getData()) { %>
                <tr>
                    <td><%= index++ %></td>
                    <td>
                        <% if (log.getUserId() != null) { %>
                            <%= userNameMap.getOrDefault(log.getUserId(), "用户#" + log.getUserId()) %>
                        <% } else { %>
                            <span class="text-muted">系统</span>
                        <% } %>
                    </td>
                    <td><%= actionTypeMap.getOrDefault(log.getActionType(), log.getActionType()) %></td>
                    <td><%= log.getIpAddress() %></td>
                    <td><%= log.getCreateTime() != null ? log.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "-" %></td>
                    <td>
                        <% if (log.getHmac() != null) { %>
                            <% if (log.isHmacValid()) { %>
                            <span class="hmac-valid">✓ 有效</span>
                            <% } else { %>
                            <span class="hmac-invalid" title="数据可能被篡改！">✗ 异常</span>
                            <% } %>
                        <% } else { %>
                            <span class="text-muted">-</span>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>

            <% if (pageResult.getTotalPages() > 1) { %>
            <div class="pagination">
                <% if (pageResult.hasPrevPage()) { %>
                <a href="<%= request.getContextPath() %>/audit-log?pageNo=1<%= querySuffix %>">首页</a>
                <a href="<%= request.getContextPath() %>/audit-log?pageNo=<%= pageResult.getPageNo() - 1 %><%= querySuffix %>">上一页</a>
                <% } else { %>
                <span class="disabled">首页</span>
                <span class="disabled">上一页</span>
                <% } %>
                <span class="current">第 <%= pageResult.getPageNo() %> / <%= pageResult.getTotalPages() %> 页</span>
                <% if (pageResult.hasNextPage()) { %>
                <a href="<%= request.getContextPath() %>/audit-log?pageNo=<%= pageResult.getPageNo() + 1 %><%= querySuffix %>">下一页</a>
                <a href="<%= request.getContextPath() %>/audit-log?pageNo=<%= pageResult.getTotalPages() %><%= querySuffix %>">末页</a>
                <% } else { %>
                <span class="disabled">下一页</span>
                <span class="disabled">末页</span>
                <% } %>
            </div>
            <% } %>

            <% } else { %>
            <div style="text-align:center;padding:40px;color:#999;">暂无日志记录</div>
            <% } %>
        </div>
    </div>
</div>
</body>
</html>
