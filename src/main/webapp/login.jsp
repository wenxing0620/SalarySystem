<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>登录 — 薪资管理系统</title>
    <style>
        .center-layout {
            min-height: 100vh; display: flex;
            background: #f8fafc;
        }
        .login-card {
            width: 420px; margin: auto;
            background: #fff; padding: 44px 36px;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 20px 50px -12px rgba(0,0,0,0.15);
            border: 1px solid #e2e8f0;
        }
        .login-header { text-align: center; margin-bottom: 30px; }
        .login-header .brand-icon {
            width: 52px; height: 52px;
            background: linear-gradient(135deg, #4338ca 0%, #6366f1 100%);
            border-radius: 14px;
            margin: 0 auto 16px;
            display: flex; align-items: center; justify-content: center;
            font-size: 22px; font-weight: 800; color: #fff;
            box-shadow: 0 8px 24px rgba(67,56,202,0.25);
        }
        .login-header h1 {
            font-size: 24px; font-weight: 800; color: #1e293b;
            letter-spacing: -0.5px; margin-bottom: 4px;
        }
        .login-header p { font-size: 14px; color: #64748b; }
        .input-group { margin-bottom: 18px; }
        .input-group label {
            display: block; font-size: 12px; font-weight: 600;
            color: #64748b; margin-bottom: 6px;
            text-transform: uppercase; letter-spacing: 0.5px;
        }
        .input-group input {
            width: 100%; padding: 11px 14px;
            border: 1px solid #e2e8f0; border-radius: 8px;
            font-size: 14px; font-family: inherit;
            color: #1e293b; background: #f8fafc;
            transition: all 0.2s ease;
        }
        .input-group input:focus {
            outline: none; border-color: #6366f1;
            background: #fff;
            box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
        }
        .input-group input::placeholder { color: #cbd5e1; }
        .login-submit {
            width: 100%; padding: 12px;
            background: linear-gradient(135deg, #4338ca 0%, #4f46e5 100%);
            color: #fff; border: none; border-radius: 8px;
            font-size: 15px; font-weight: 600; cursor: pointer;
            font-family: inherit; letter-spacing: 0.3px;
            transition: all 0.2s ease;
            margin-top: 6px;
            box-shadow: 0 4px 12px rgba(67,56,202,0.25);
        }
        .login-submit:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 20px rgba(67,56,202,0.35);
        }
        .footer-note {
            text-align: center; margin-top: 24px;
            padding: 14px; background: #f8fafc;
            border-radius: 8px; font-size: 12px; color: #94a3b8;
            line-height: 1.8;
        }
        .footer-note strong { color: #64748b; }
    </style>
</head>
<body>
<div class="center-layout">
    <div class="login-card">
        <div class="login-header">
            <h1>薪资管理系统</h1>
            <p>员工工资管理与安全审计平台</p>
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
                <div style="margin-top:4px;font-size:11px;opacity:0.8;word-break:break-all;"><%= errorDetail %></div>
                <% } %>
            </div>
        <% } %>

        <% if (success != null) { %>
            <div class="alert alert-success"><%= success %></div>
        <% } %>

        <form method="POST" action="<%= request.getContextPath() %>/login">
            <div class="input-group">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username" placeholder="请输入用户名" required autofocus>
            </div>
            <div class="input-group">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" placeholder="请输入密码" required>
            </div>
            <button type="submit" class="login-submit">登 录</button>
        </form>

        <div class="footer-note">
            <strong>测试账号</strong><br>
            admin / hr / finance / audit / gm<br>
            默认密码：<strong>Admin@123</strong>
        </div>
    </div>
</div>
</body>
</html>
