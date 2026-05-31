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
    <title>审计日志 - 薪资管理系统</title>
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
        }
        .navbar-right {
            display: flex;
            gap: 20px;
            align-items: center;
        }
        .navbar-right a, .navbar-right span {
            color: white;
            text-decoration: none;
        }
        .sidebar {
            width: 250px;
            background: white;
            min-height: calc(100vh - 60px);
            padding-top: 20px;
            border-right: 1px solid #e0e0e0;
            float: left;
        }
        .sidebar-menu a {
            display: block;
            padding: 12px 20px;
            color: #333;
            text-decoration: none;
            border-left: 3px solid transparent;
            transition: all 0.3s;
        }
        .sidebar-menu a.active {
            border-left-color: #667eea;
            color: #667eea;
        }
        .main-content {
            margin-left: 250px;
            padding: 30px;
        }
        .filter-box {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            gap: 15px;
            align-items: center;
            flex-wrap: wrap;
        }
        .filter-box input, .filter-box select {
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .filter-box button {
            padding: 8px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
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
            font-size: 13px;
        }
        table th {
            background: #f5f5f5;
            padding: 10px;
            text-align: left;
            font-weight: 600;
            border-bottom: 2px solid #e0e0e0;
        }
        table td {
            padding: 10px;
            border-bottom: 1px solid #e0e0e0;
        }
        table tr {
            transition: background 0.2s;
        }
        table tr:hover {
            background: #f9f9f9;
        }
        .status-ok {
            color: #27ae60;
            font-weight: bold;
        }
        .status-fail {
            color: #e74c3c;
            font-weight: bold;
        }
        .hmac-badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 3px;
            font-size: 11px;
            font-weight: bold;
        }
        .hmac-valid {
            background: #d5f4e6;
            color: #27ae60;
        }
        .hmac-invalid {
            background: #fadbd8;
            color: #e74c3c;
        }
        .btn-small {
            padding: 4px 8px;
            font-size: 11px;
            background: #3498db;
            color: white;
            border: none;
            border-radius: 3px;
            cursor: pointer;
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
        <li><a href="audit-log" class="active">📋 审计日志</a></li>
        <li><a href="logout">🚪 登出</a></li>
    </ul>
</div>

<div class="main-content">
    <h2>审计日志</h2>

    <div class="filter-box">
        <input type="date" id="startDate" placeholder="开始日期">
        <input type="date" id="endDate" placeholder="结束日期">
        <select id="actionType">
            <option value="">全部操作</option>
            <option value="LOGIN">登录</option>
            <option value="LOGOUT">登出</option>
            <option value="ADD_EMP">新增员工</option>
            <option value="UPDATE_EMP">修改员工</option>
            <option value="VIEW_SALARY">查看薪资</option>
            <option value="EXPORT_SALARY">导出薪资</option>
        </select>
        <input type="text" id="username" placeholder="操作人用户名">
        <button onclick="queryLogs()">查询</button>
    </div>

    <div class="table-container">
        <h3>操作日志（共 45 条）</h3>
        <table>
            <thead>
                <tr>
                    <th>日志ID</th>
                    <th>操作人</th>
                    <th>操作类型</th>
                    <th>操作IP</th>
                    <th>操作时间</th>
                    <th>HMAC 校验</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>LOG001</td>
                    <td>admin</td>
                    <td>LOGIN_SUCCESS</td>
                    <td>192.168.1.100</td>
                    <td>2026-05-29 14:30:25</td>
                    <td><span class="hmac-badge hmac-valid">✓ 有效</span></td>
                    <td><button class="btn-small" onclick="viewLog(1)">详情</button></td>
                </tr>
                <tr>
                    <td>LOG002</td>
                    <td>admin</td>
                    <td>VIEW_EMPLOYEE</td>
                    <td>192.168.1.100</td>
                    <td>2026-05-29 14:28:10</td>
                    <td><span class="hmac-badge hmac-valid">✓ 有效</span></td>
                    <td><button class="btn-small" onclick="viewLog(2)">详情</button></td>
                </tr>
                <tr>
                    <td>LOG003</td>
                    <td>admin</td>
                    <td>UPDATE_SALARY</td>
                    <td>192.168.1.100</td>
                    <td>2026-05-29 14:25:45</td>
                    <td><span class="hmac-badge hmac-valid">✓ 有效</span></td>
                    <td><button class="btn-small" onclick="viewLog(3)">详情</button></td>
                </tr>
                <tr>
                    <td>LOG004</td>
                    <td>audit</td>
                    <td>EXPORT_SALARY</td>
                    <td>192.168.1.105</td>
                    <td>2026-05-29 14:20:00</td>
                    <td><span class="hmac-badge hmac-invalid">✗ 异常</span></td>
                    <td><button class="btn-small" onclick="viewLog(4)">详情</button></td>
                </tr>
                <tr>
                    <td>LOG005</td>
                    <td>hr</td>
                    <td>ADD_EMPLOYEE</td>
                    <td>192.168.1.110</td>
                    <td>2026-05-29 14:15:30</td>
                    <td><span class="hmac-badge hmac-valid">✓ 有效</span></td>
                    <td><button class="btn-small" onclick="viewLog(5)">详情</button></td>
                </tr>
            </tbody>
        </table>
    </div>

    <div style="text-align: center; margin-top: 20px; color: #999;">
        <p>显示 1-5 / 45 条 | <a href="#" style="color: #667eea;">加载更多</a></p>
    </div>
</div>

<script>
function queryLogs() {
    alert('查询日志');
}
function viewLog(id) {
    alert('查看日志详情: ' + id);
}
</script>
</body>
</html>

