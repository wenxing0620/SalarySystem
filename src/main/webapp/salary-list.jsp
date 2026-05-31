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
    private java.util.List<com.salarysystem.model.salaryRecord> getSalaryList(HttpServletRequest request) {
        Object obj = request.getAttribute("salaryList");
        if (obj == null) {
            return new java.util.ArrayList<>();
        }
        return (java.util.List<com.salarysystem.model.salaryRecord>) obj;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>薪资管理 - 薪资管理系统</title>
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
        .sidebar-menu {
            list-style: none;
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
        }
        .filter-box {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            gap: 15px;
            align-items: center;
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
        table tr:hover {
            background: #f9f9f9;
        }
        .number {
            text-align: right;
        }
        .btn {
            padding: 6px 12px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
        }
        .btn-primary {
            background: #667eea;
            color: white;
        }
        .btn-danger {
            background: #e74c3c;
            color: white;
        }
        .btn-export {
            background: #27ae60;
            color: white;
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
        <li><a href="<%= request.getContextPath() %>/dashboard">[首页]</a></li>
        <li><a href="<%= request.getContextPath() %>/emp-list">[员工管理]</a></li>
        <li><a href="<%= request.getContextPath() %>/salary-list" class="active">[薪资管理]</a></li>
        <li><a href="<%= request.getContextPath() %>/deduction">[专项附加扣除]</a></li>
        <li><a href="<%= request.getContextPath() %>/audit-log">[审计日志]</a></li>
        <li><a href="<%= request.getContextPath() %>/logout">[登出]</a></li>
    </ul>
</div>

<div class="main-content">
    <h2>薪资管理</h2>
    <!-- 导入功能已移除；保留导出功能 -->

    <div class="filter-box">
        <form id="searchForm" method="get" action="<%= request.getContextPath() %>/salary-list" style="display:flex;gap:15px;align-items:center;width:100%;">
            <select name="month">
                <option value="">-- 选择月份 --</option>
                <option value="2026-05" <%= "2026-05".equals(request.getAttribute("month")) ? "selected" : "" %>>2026-05</option>
                <option value="2026-04" <%= "2026-04".equals(request.getAttribute("month")) ? "selected" : "" %>>2026-04</option>
                <option value="2026-03" <%= "2026-03".equals(request.getAttribute("month")) ? "selected" : "" %>>2026-03</option>
            </select>
            <label for="empName" style="position:absolute;left:-9999px;">员工姓名</label>
            <input type="text" id="empName" name="empName" placeholder="员工姓名" value="<%= request.getAttribute("empName") == null ? "" : request.getAttribute("empName") %>">
            <label for="dept" style="position:absolute;left:-9999px;">部门</label>
            <input type="text" id="dept" name="dept" placeholder="部门" value="<%= request.getAttribute("dept") == null ? "" : request.getAttribute("dept") %>">
            <button type="submit">查询</button>
            <button type="button" class="btn-export" onclick="location.href='<%= request.getContextPath() %>/salary-list'">重置</button>
            <button type="button" class="btn-export" onclick="exportData()" style="margin-left: auto;">📥 导出</button>
        </form>
        <!-- 导入功能已移除（由系统管理员控制）；若需恢复请联系开发者 -->
    </div>

    <div class="table-container">
        <table>
            <thead>
                <tr>
                    <th>员工编号</th>
                    <th>计薪月份</th>
                    <th>基本工资</th>
                    <th>岗位津贴</th>
                    <th>社保</th>
                    <th>个税</th>
                    <th>实发工资</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
            <%
                java.util.List<com.salarysystem.model.salaryRecord> salaryList = getSalaryList(request);
                if (salaryList == null || salaryList.isEmpty()) {
            %>
                <tr><td colspan="8" style="text-align:center;color:#999;">暂无数据</td></tr>
            <%
                } else {
                    for (com.salarysystem.model.salaryRecord r : salaryList) {
            %>
                <tr>
                    <td><%= r.getEmpId() %></td>
                    <td><%= r.getSalaryMonth() %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getBasicSalary()) %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getPositionAllowance()) %></td>
                    <td class="number">-¥<%= String.format("%.2f", r.getSocialSecurity()) %></td>
                    <td class="number">-¥<%= String.format("%.2f", r.getTax()) %></td>
                    <td class="number"><strong>¥<%= String.format("%.2f", r.getActualSalary()) %></strong></td>
                    <td>
                        <button class="btn btn-primary" onclick="viewSalary(<%= r.getRecordId() %>)">查看</button>
                        <button class="btn btn-danger" onclick="deleteSalary(<%= r.getRecordId() %>)">删除</button>
                    </td>
                </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>
</div>

<script>
function exportData() {
    var ctx = '<%= request.getContextPath() %>';
    var month = document.querySelector('select[name="month"]').value;
    var empName = document.getElementById('empName').value;
    var dept = document.getElementById('dept').value;
    var params = [];
    if (month) params.push('month=' + encodeURIComponent(month));
    if (empName) params.push('empName=' + encodeURIComponent(empName));
    if (dept) params.push('dept=' + encodeURIComponent(dept));
    var url = ctx + '/salary-export-excel' + (params.length ? ('?' + params.join('&')) : '');
    window.location = url;
}
function viewSalary(id) {
    alert('查看薪资记录 ' + id);
}
function deleteSalary(id) {
    if (confirm('确定删除该薪资记录吗？')) {
        alert('已删除');
    }
}
</script>
</body>
</html>

