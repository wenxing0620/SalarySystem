<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%@ page import="jakarta.servlet.http.HttpServletRequest" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<%!
    @SuppressWarnings("unchecked")
    private java.util.List<com.salarysystem.model.empInfo> getEmpList(HttpServletRequest request) {
        Object obj = request.getAttribute("empList");
        if (obj == null) {
            return new java.util.ArrayList<>();
        }
        return (java.util.List<com.salarysystem.model.empInfo>) obj;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>员工管理 - 薪资管理系统</title>
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
        .sidebar-menu a:hover, .sidebar-menu a.active {
            background: #f5f5f5;
            border-left-color: #667eea;
            color: #667eea;
        }
        .main-content {
            margin-left: 250px;
            padding: 30px;
            min-height: calc(100vh - 60px);
        }
        .header-section {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .search-box {
            display: flex;
            gap: 10px;
        }
        .search-box input {
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .search-box button {
            padding: 8px 15px;
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
        .masked {
            color: #999;
        }
        .btn {
            padding: 6px 12px;
            margin: 2px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            font-size: 12px;
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
        .btn-info {
            background: #3498db;
            color: white;
        }
        .btn-info:hover {
            background: #2980b9;
        }
        .btn-add {
            background: #27ae60;
            color: white;
            padding: 10px 20px;
        }
        .btn-add:hover {
            background: #229954;
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
        <li><a href="emp-list" class="active">👥 员工管理</a></li>
        <li><a href="salary-list">💰 薪资管理</a></li>
        <li><a href="deduction">📄 专项附加扣除</a></li>
        <li><a href="audit-log">📋 审计日志</a></li>
        <li><a href="logout">🚪 登出</a></li>
    </ul>
</div>

    <div class="main-content">
        <div class="header-section">
            <h2>员工管理</h2>
            <button class="btn-add" onclick="location.href='<%= request.getContextPath() %>/emp-add'">+ 新增员工</button>
        </div>

        <%
            // Display error message if present
            if (request.getAttribute("error") != null) {
        %>
        <div style="background: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; padding: 12px; margin-bottom: 20px; border-radius: 4px;">
            <strong>警告：</strong><%= request.getAttribute("error") %>
        </div>
        <%
            }
        %>

    <form class="search-box" style="margin-bottom: 20px;" method="get" action="<%= request.getContextPath() %>/emp-list">
        <label for="searchInput" style="position:absolute;left:-9999px;">搜索员工</label>
        <input type="text" id="searchInput" name="keyword" placeholder="搜索员工编号、姓名、部门、岗位、身份证、手机号..." value="<%= request.getAttribute("keyword") == null ? "" : request.getAttribute("keyword") %>" onkeydown="if(event.key==='Enter'){event.preventDefault(); document.querySelector('.search-box').submit();}">
        <button type="submit">搜索</button>
        <button type="button" onclick="window.location.href='<%= request.getContextPath() %>/emp-list'">重置</button>
    </form>

    <div class="table-container">
        <table>
            <thead>
                <tr>
                    <th>员工编号</th>
                    <th>姓名</th>
                    <th>部门</th>
                    <th>岗位</th>
                    <th>身份证</th>
                    <th>手机号</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
            <%
                java.util.List<com.salarysystem.model.empInfo> empList = getEmpList(request);
                for (com.salarysystem.model.empInfo e : empList) {
            %>
                <tr>
                    <td><%= e.getEmpNo() %></td>
                    <td><%= com.salarysystem.util.DesensitizeUtil.maskName(e.getEmpName()) %></td>
                    <td><%= e.getDeptName() %></td>
                    <td><%= e.getPosition() %></td>
                    <td><span class="masked"><%= com.salarysystem.util.DesensitizeUtil.maskIdCard(e.getIdCard()) %></span></td>
                    <td><span class="masked"><%= com.salarysystem.util.DesensitizeUtil.maskPhone(e.getPhone()) %></span></td>
                    <td>
                        <a class="btn btn-info" href="<%= request.getContextPath() %>/emp-view?id=<%= e.getEmpId() %>">查看</a>
                        <a class="btn btn-primary" href="<%= request.getContextPath() %>/emp-edit?id=<%= e.getEmpId() %>">编辑</a>
                        <a class="btn btn-danger" href="<%= request.getContextPath() %>/emp-delete?id=<%= e.getEmpId() %>" onclick="return confirm('确定要删除该员工吗？');">删除</a>
                    </td>
                </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>
</div>

<script>
function searchEmployee() {
    document.querySelector('.search-box').submit();
}
function viewEmployee(id) {
    alert('查看员工 ' + id);
}
function editEmployee(id) {
    alert('编辑员工 ' + id);
}
function deleteEmployee(id) {
    if (confirm('确定要删除该员工吗？')) {
        alert('已删除员工 ' + id);
    }
}
</script>
</body>
</html>

