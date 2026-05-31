<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>薪资管理系统 - 登录</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .login-container {
            background: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            width: 100%;
            max-width: 400px;
        }
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        .login-header h1 {
            color: #333;
            font-size: 24px;
            margin-bottom: 10px;
        }
        .login-header p {
            color: #666;
            font-size: 14px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
        }
        .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        .form-group input:focus {
            outline: none;
            border-color: #667eea;
        }
        .login-btn {
            width: 100%;
            padding: 10px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            font-weight: 500;
            cursor: pointer;
            transition: transform 0.2s;
        }
        .login-btn:hover {
            transform: translateY(-2px);
        }
        .error-msg {
            color: #e74c3c;
            margin-bottom: 20px;
            padding: 10px;
            background: #fadbd8;
            border-radius: 4px;
            display: none;
        }
        .success-msg {
            color: #27ae60;
            margin-bottom: 20px;
            padding: 10px;
            background: #d5f4e6;
            border-radius: 4px;
            display: none;
        }
    </style>
</head>
<body>
<div class="login-container">
    <div class="login-header">
        <h1>薪资管理系统</h1>
        <p>员工工资管理平台</p>
    </div>

    <%
        String error = (String) request.getAttribute("error");
        if (error == null) {
            error = request.getParameter("error");
        }
        String success = request.getParameter("success");
    %>

    <% if (error != null) { %>
        <div class="error-msg" style="display: block;">
            <%= error %>
            <%
                String errorDetail = (String) request.getAttribute("errorDetail");
                if (errorDetail != null && !errorDetail.isEmpty()) {
            %>
            <div style="margin-top: 6px; font-size: 12px; color: #a94442; word-break: break-all;">
                <%= errorDetail %>
            </div>
            <% } %>
        </div>
    <% } %>

    <% if (success != null) { %>
        <div class="success-msg" style="display: block;">
            <%= success %>
        </div>
    <% } %>

    <form method="POST" action="login" class="login-form">
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

    <div style="text-align: center; margin-top: 20px; color: #999; font-size: 12px;">
        <p>测试账号: admin / Admin@123</p>
    </div>
</div>
</body>
</html>

