<%@ page pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%
    String _role = (String) session.getAttribute("currentUserRole");
    boolean isAdmin = "系统管理员".equals(_role);
    boolean isHR = "系统管理员".equals(_role) || "人事管理员".equals(_role) || "总经理".equals(_role);
    boolean isFinance = "系统管理员".equals(_role) || "财务管理员".equals(_role) || "总经理".equals(_role);
    boolean isAudit = "系统管理员".equals(_role) || "审计管理员".equals(_role);
    boolean isGM = "总经理".equals(_role);
    String active = (String) request.getAttribute("activeSidebar");
    if (active == null) active = "";
    String ctx = request.getContextPath();
%>
<!-- debug: _role=<%= _role %>, isAdmin=<%= isAdmin %>, isHR=<%= isHR %>, isFinance=<%= isFinance %>, isAudit=<%= isAudit %>, isGM=<%= isGM %> -->
<div class="sidebar">
    <!-- 当前角色指示 (部署后可删除此区块) -->
    <div style="padding:4px 16px; font-size:10px; color:#64748b; background:#0f172a;">
        DEBUG: role=<%= _role != null ? _role : "null" %>, HR=<%= isHR %>, Finance=<%= isFinance %>
    </div>
    <!-- 业务管理 -->
    <div class="sidebar-label">业务管理</div>
    <ul class="sidebar-menu">
        <li><a href="<%= ctx %>/dashboard" <%= "dashboard".equals(active) ? "class=\"active\"" : "" %>>系统首页</a></li>
        <% if (isHR) { %>
        <li><a href="<%= ctx %>/emp-list" <%= "emp-list".equals(active) ? "class=\"active\"" : "" %>>员工管理</a></li>
        <li><a href="<%= ctx %>/dept" <%= "dept".equals(active) ? "class=\"active\"" : "" %>>部门管理</a></li>
        <li><a href="<%= ctx %>/family" <%= "family".equals(active) ? "class=\"active\"" : "" %>>家属管理</a></li>
        <% } %>
        <% if (isFinance) { %>
        <li><a href="<%= ctx %>/salary-list" <%= "salary-list".equals(active) ? "class=\"active\"" : "" %>>薪资管理</a></li>
        <li><a href="<%= ctx %>/deduction" <%= "deduction".equals(active) ? "class=\"active\"" : "" %>>专项附加扣除</a></li>
        <% } %>
    </ul>

    <% if (isGM) { %>
    <div style="padding:6px 16px; margin:8px 10px; background:rgba(245,158,11,0.12); border-radius:6px; font-size:11px; color:#f59e0b;">
        只读模式 — 您仅可查看数据
    </div>
    <% } %>

    <!-- 系统管理 -->
    <% if (isAdmin || isAudit) { %>
    <div class="sidebar-divider"></div>
    <div class="sidebar-label">系统管理</div>
    <ul class="sidebar-menu">
        <% if (isAdmin) { %>
        <li><a href="<%= ctx %>/user-management" <%= "user-management".equals(active) ? "class=\"active\"" : "" %>>用户管理</a></li>
        <% } %>
        <% if (isAudit) { %>
        <li><a href="<%= ctx %>/audit-log" <%= "audit-log".equals(active) ? "class=\"active\"" : "" %>>审计日志</a></li>
        <% } %>
    </ul>
    <% } %>

    <!-- 底部登出 -->
    <div class="sidebar-divider"></div>
    <ul class="sidebar-menu">
        <li><a href="<%= ctx %>/logout">退出登录</a></li>
    </ul>
</div>
