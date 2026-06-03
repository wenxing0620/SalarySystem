<%@ page pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%
    sysUser sidebarUser = (sysUser) session.getAttribute("currentUser");
    Integer roleId = (sidebarUser != null) ? sidebarUser.getRoleId() : null;
    boolean isAdmin = roleId != null && roleId == 1;
    boolean isHR = roleId != null && (roleId == 1 || roleId == 2 || roleId == 4);
    boolean isFinance = roleId != null && (roleId == 1 || roleId == 3 || roleId == 4);
    boolean isAudit = roleId != null && (roleId == 1 || roleId == 5);
    String active = (String) request.getAttribute("activeSidebar");
    if (active == null) active = "";
%>
<div class="sidebar">
    <ul class="sidebar-menu">
        <li><a href="<%= request.getContextPath() %>/dashboard" <%= "dashboard".equals(active) ? "class=\"active\"" : "" %>>[首页]</a></li>
        <% if (isAdmin) { %>
        <li><a href="<%= request.getContextPath() %>/user-management" <%= "user-management".equals(active) ? "class=\"active\"" : "" %>>[用户管理]</a></li>
        <% } %>
        <% if (isHR) { %>
        <li><a href="<%= request.getContextPath() %>/emp-list" <%= "emp-list".equals(active) ? "class=\"active\"" : "" %>>[员工管理]</a></li>
        <li><a href="<%= request.getContextPath() %>/dept" <%= "dept".equals(active) ? "class=\"active\"" : "" %>>[部门管理]</a></li>
        <li><a href="<%= request.getContextPath() %>/family" <%= "family".equals(active) ? "class=\"active\"" : "" %>>[家属管理]</a></li>
        <% } %>
        <% if (isFinance) { %>
        <li><a href="<%= request.getContextPath() %>/salary-list" <%= "salary-list".equals(active) ? "class=\"active\"" : "" %>>[薪资管理]</a></li>
        <li><a href="<%= request.getContextPath() %>/deduction" <%= "deduction".equals(active) ? "class=\"active\"" : "" %>>[专项附加扣除]</a></li>
        <% } %>
        <% if (isAudit) { %>
        <li><a href="<%= request.getContextPath() %>/audit-log" <%= "audit-log".equals(active) ? "class=\"active\"" : "" %>>[审计日志]</a></li>
        <% } %>
        <li><a href="<%= request.getContextPath() %>/logout">[登出]</a></li>
    </ul>
</div>
