<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }
    Boolean pending = (Boolean) session.getAttribute("pendingPasswordChange");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>修改密码 - 薪资管理系统</title>
    <style>
        .center-layout { min-height: 100vh; display: flex; justify-content: center; align-items: center; background: #f5f5f5; }
        .subtitle { text-align: center; color: #e67e22; font-size: 14px; margin-bottom: 24px; }
        .skip-link { text-align: center; margin-top: 16px; }
        .skip-link a { color: #999; font-size: 13px; text-decoration: none; }
        .skip-link a:hover { text-decoration: underline; }
        .full-width { width: 100%; }
    </style>
</head>
<body>
<div class="center-layout">
    <div class="login-card">
        <h2>修改密码</h2>

        <% if (Boolean.TRUE.equals(pending)) { %>
        <div class="subtitle">⚠ 您的密码已超过90天未修改，请先更新密码</div>
        <% } %>

        <% if (error != null) { %><div class="alert alert-error"><%= error %></div><% } %>
        <% if (success != null) { %>
        <div class="alert alert-success"><%= success %></div>
        <div class="text-center mt-20">
            <a class="btn btn-primary btn-lg" href="<%= request.getContextPath() %>/dashboard">进入系统</a>
        </div>
        <% } else { %>

        <form method="post" action="<%= request.getContextPath() %>/change-password">
            <div class="form-group">
                <label>当前用户名</label>
                <input type="text" value="<%= user.getUsername() %>" disabled>
            </div>
            <div class="form-group">
                <label>旧密码</label>
                <input type="password" name="oldPassword" required placeholder="请输入当前密码">
            </div>
            <div class="form-group">
                <label>新密码</label>
                <input type="password" name="newPassword" required placeholder="长度8位以上，含大小写字母、数字、特殊字符">
                <div class="help-text">长度 ≥8 位，必须包含大写字母、小写字母、数字、特殊字符</div>
            </div>
            <div class="form-group">
                <label>确认新密码</label>
                <input type="password" name="confirmPassword" required placeholder="再次输入新密码">
            </div>
            <button type="submit" class="btn btn-primary full-width">确认修改</button>
        </form>

        <% if (!Boolean.TRUE.equals(pending)) { %>
        <div class="skip-link">
            <a href="<%= request.getContextPath() %>/dashboard">返回首页</a>
        </div>
        <% } %>

        <% } %>
    </div>
</div>
</body>
</html>
