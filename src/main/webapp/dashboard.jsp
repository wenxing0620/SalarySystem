<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>薪资管理系统 - 首页</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            background: #f5f5f5;
        }
        .navbar {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .navbar h1 { font-size: 22px; }
        .navbar-right {
            display: flex;
            gap: 20px;
            align-items: center;
        }
        .navbar-right a, .navbar-right span {
            color: white;
            text-decoration: none;
            cursor: pointer;
        }
        .navbar-right a:hover {
            text-decoration: underline;
        }
        .sidebar {
            width: 250px;
            background: white;
            min-height: calc(100vh - 60px);
            padding-top: 20px;
            border-right: 1px solid #e0e0e0;
            float: left;
        }
        .sidebar-menu {
            list-style: none;
        }
        .sidebar-menu li {
            padding: 0;
        }
        .sidebar-menu a {
            display: block;
            padding: 12px 20px;
            color: #333;
            text-decoration: none;
            border-left: 3px solid transparent;
            transition: all 0.3s;
        }
        .sidebar-menu a:hover {
            background: #f5f5f5;
            border-left-color: #667eea;
            color: #667eea;
        }
        .main-content {
            margin-left: 250px;
            padding: 30px;
            min-height: calc(100vh - 60px);
        }
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            text-align: center;
        }
        .card h3 {
            color: #333;
            margin-bottom: 10px;
        }
        .card .number {
            font-size: 32px;
            color: #667eea;
            font-weight: bold;
        }
        .table-container {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        table th {
            background: #f5f5f5;
            padding: 12px;
            text-align: left;
            font-weight: 600;
            color: #333;
            border-bottom: 2px solid #e0e0e0;
        }
        table td {
            padding: 12px;
            border-bottom: 1px solid #e0e0e0;
        }
        table tr:hover {
            background: #f9f9f9;
        }
        .btn {
            padding: 8px 15px;
            margin: 5px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }
        .btn-primary {
            background: #667eea;
            color: white;
        }
        .btn-primary:hover {
            background: #5568d3;
        }
        .btn-danger {
            background: #e74c3c;
            color: white;
        }
        .btn-danger:hover {
            background: #c0392b;
        }
    </style>
</head>
<body>
<div class="navbar">
    <h1>薪资管理系统</h1>
    <div class="navbar-right">
        <span>欢迎, <%= user.getUsername() %></span>
        <a href="logout">登出</a>
    </div>
</div>

<div class="sidebar">
    <ul class="sidebar-menu">
        <li><a href="dashboard">📊 首页</a></li>
        <li><a href="emp-list">👥 员工管理</a></li>
        <li><a href="salary-list">💰 薪资管理</a></li>
        <li><a href="deduction">📄 专项附加扣除</a></li>
        <li><a href="audit-log">📋 审计日志</a></li>
        <li><a href="logout">🚪 登出</a></li>
    </ul>
</div>

<div class="main-content">
    <h2>系统首页</h2>

    <div class="dashboard-grid">
        <div class="card">
            <h3>员工总数</h3>
            <div class="number">128</div>
        </div>
        <div class="card">
            <h3>本月工资单</h3>
            <div class="number">128</div>
        </div>
        <div class="card">
            <h3>待处理任务</h3>
            <div class="number">5</div>
        </div>
        <div class="card">
            <h3>系统在线用户</h3>
            <div class="number">12</div>
        </div>
    </div>

    <div class="table-container">
        <h3>最近操作日志</h3>
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
                <tr>
                    <td>admin</td>
                    <td>登录</td>
                    <td>2026-05-29 10:30:00</td>
                    <td>192.168.1.1</td>
                </tr>
                <tr>
                    <td>admin</td>
                    <td>查看员工</td>
                    <td>2026-05-29 10:25:00</td>
                    <td>192.168.1.1</td>
                </tr>
                <tr>
                    <td>admin</td>
                    <td>修改工资</td>
                    <td>2026-05-29 10:20:00</td>
                    <td>192.168.1.1</td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>

