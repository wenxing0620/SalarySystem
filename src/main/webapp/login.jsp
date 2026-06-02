<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>登录 - 薪资管理系统</title>
    <style>
        .center-layout { min-height: 100vh; display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        .login-header { text-align: center; margin-bottom: 30px; }
        .login-header h1 { color: #333; font-size: 24px; margin-bottom: 10px; }
        .login-header p { color: #666; font-size: 14px; }
        .login-btn {
            width: 100%; padding: 10px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white; border: none; border-radius: 4px;
            font-size: 16px; font-weight: 500; cursor: pointer;
            transition: transform 0.2s;
        }
        .login-btn:hover { transform: translateY(-2px); }
        .footer-note { text-align: center; margin-top: 20px; color: #999; font-size: 12px; }
    </style>
</head>
<body>
<div class="center-layout">
    <div class="login-card">
        <div class="login-header">
            <h1>薪资管理系统</h1>
            <p>员工工资管理平台</p>
        </div>

        <%
            String error = (String) request.getAttribute("error");
            if (error == null) error = request.getParameter("error");
            String success = request.getParameter("success");
        %>

        <% if (error != null) { %>
            <div class="alert alert-error">
                <%= error %>
                <%
                    String errorDetail = (String) request.getAttribute("errorDetail");
                    if (errorDetail != null && !errorDetail.isEmpty()) {
                %>
                <div style="margin-top:6px;font-size:12px;color:#a94442;word-break:break-all;"><%= errorDetail %></div>
                <% } %>
            </div>
        <% } %>

        <% if (success != null) { %>
            <div class="alert alert-success"><%= success %></div>
        <% } %>

        <form method="POST" action="<%= request.getContextPath() %>/login">
            <div class="form-group">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username" placeholder="输入用户名" required>
            </div>
            <div class="form-group">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" placeholder="输入密码" required>
            </div>
            <button type="submit" class="login-btn">登录</button>
        </form>

        <div class="footer-note">
            <p>测试账号: admin / Admin@123</p>
        </div>
    </div>
</div>
</body>
</html>
