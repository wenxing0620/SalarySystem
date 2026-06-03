<%@ page pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%
    sysUser navbarUser = (sysUser) session.getAttribute("currentUser");
%>
<div class="navbar">
    <h1>薪资管理系统</h1>
    <div class="navbar-right">
        <span>欢迎, <%= navbarUser != null ? navbarUser.getUsername() : "?" %></span>
        <a href="<%= request.getContextPath() %>/change-password">修改密码</a>
        <a href="<%= request.getContextPath() %>/logout">登出</a>
    </div>
</div>
