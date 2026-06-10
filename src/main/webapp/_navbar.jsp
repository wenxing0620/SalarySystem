<%@ page pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%
    sysUser navbarUser = (sysUser) session.getAttribute("currentUser");
    String username = navbarUser != null ? navbarUser.getUsername() : "?";
    String initial = username.substring(0, 1).toUpperCase();
%>
<div class="navbar">
    <h1>薪资管理系统</h1>
    <div class="navbar-right">
        <div class="user-chip">
            <div class="avatar"><%= initial %></div>
            <span><%= username %></span>
        </div>
        <a href="<%= request.getContextPath() %>/change-password">修改密码</a>
        <a href="<%= request.getContextPath() %>/logout">登出</a>
    </div>
</div>
