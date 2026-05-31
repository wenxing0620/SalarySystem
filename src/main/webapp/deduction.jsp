<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser" %>
<%@ page import="com.salarysystem.model.taxDeduction" %>
<%@ page import="com.salarysystem.model.empInfo" %>
<%@ page import="com.salarysystem.servlet.DeductionServlet.DeductionViewRow" %>
<%@ page import="java.util.List" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String message = (String) session.getAttribute("message");
    if (message != null) {
        session.removeAttribute("message");
    }

    Boolean editMode = (Boolean) request.getAttribute("editMode");
    Boolean viewMode = (Boolean) request.getAttribute("viewMode");
    taxDeduction deduction = (taxDeduction) request.getAttribute("deduction");
    Integer year = (Integer) request.getAttribute("year");

    @SuppressWarnings("unchecked")
    List<DeductionViewRow> rows = (List<DeductionViewRow>) request.getAttribute("deductionRows");
    if (rows == null) rows = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    List<empInfo> allEmployees = (List<empInfo>) request.getAttribute("allEmployees");
    if (allEmployees == null) allEmployees = new java.util.ArrayList<>();

    Integer currentYear = (Integer) request.getAttribute("currentYear");
    if (currentYear == null) {
        currentYear = java.time.LocalDate.now().getYear();
    }

    String mode = "list";
    if (Boolean.TRUE.equals(editMode)) mode = "edit";
    if (Boolean.TRUE.equals(viewMode)) mode = "view";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>专项附加扣除管理 - 薪资管理系统</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .navbar { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .navbar-right { display: flex; gap: 20px; align-items: center; }
        .sidebar { width: 250px; background: white; min-height: calc(100vh - 60px); padding-top: 20px; border-right: 1px solid #e0e0e0; float: left; }
        .sidebar-menu { list-style: none; }
        .sidebar-menu a { display: block; padding: 12px 20px; color: #333; text-decoration: none; border-left: 3px solid transparent; transition: all 0.3s; }
        .sidebar-menu a:hover, .sidebar-menu a.active { background: #f5f5f5; border-left-color: #667eea; color: #667eea; }
        .main-content { margin-left: 250px; padding: 30px; }
        .message { margin: 10px 0; padding: 10px; background:#d4edda; border:1px solid #c3e6cb; color:#155724; border-radius:4px; }
        .container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
        h2 { margin-bottom: 20px; color: #333; }
        h3 { margin-bottom: 16px; color: #333; font-size: 18px; }
        .toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
        .table-container { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 13px; }
        table th { background: #f5f5f5; padding: 10px; text-align: left; border-bottom: 2px solid #e0e0e0; white-space: nowrap; }
        table td { padding: 10px; border-bottom: 1px solid #e0e0e0; }
        .number { text-align: right; white-space: nowrap; }
        .btn { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 12px; text-decoration: none; display: inline-block; }
        .btn-primary { background: #667eea; color: white; }
        .btn-primary:hover { background: #5568d3; }
        .btn-danger { background: #e74c3c; color: white; }
        .btn-danger:hover { background: #d43d2e; }
        .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }
        .form-group { margin-bottom: 12px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #333; }
        .form-group input, .form-group select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        .readonly { padding: 8px; background: #f9f9f9; border: 1px solid #eee; border-radius: 4px; min-height: 36px; }
        .hint { color: #888; font-size: 12px; margin-top: 8px; }
    </style>
</head>
<body>
<div class="navbar">
    <h1>薪资管理系统</h1>
    <div class="navbar-right">
        <span>欢迎, <%= user.getUsername() %></span>
        <a href="<%= request.getContextPath() %>/logout" style="color:white;text-decoration:none;">登出</a>
    </div>
</div>

<div class="sidebar">
    <ul class="sidebar-menu">
        <li><a href="<%= request.getContextPath() %>/dashboard">[首页]</a></li>
        <li><a href="<%= request.getContextPath() %>/emp-list">[员工管理]</a></li>
        <li><a href="<%= request.getContextPath() %>/salary-list">[薪资管理]</a></li>
        <li><a href="<%= request.getContextPath() %>/deduction" class="active">[专项附加扣除]</a></li>
        <li><a href="<%= request.getContextPath() %>/audit-log">[审计日志]</a></li>
        <li><a href="<%= request.getContextPath() %>/logout">[登出]</a></li>
    </ul>
</div>

<div class="main-content">
    <h2>专项附加扣除管理</h2>
    <% if (message != null) { %>
    <div class="message"><%= message %></div>
    <% } %>

    <% if ("edit".equals(mode) || "view".equals(mode)) { %>
    <div class="container">
        <h3><%= "view".equals(mode) ? "查看申报记录" : ((deduction != null && deduction.getDeductionId() != null) ? "修改申报记录" : "新增申报记录") %></h3>

        <% if ("view".equals(mode)) { %>
        <div class="form-row">
            <div class="form-group"><label>员工ID</label><div class="readonly"><%= deduction != null ? deduction.getEmpId() : "-" %></div></div>
            <div class="form-group"><label>申报年份</label><div class="readonly"><%= year != null ? year : "-" %></div></div>
            <div class="form-group"><label>子女教育</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getChildEdu() != null ? deduction.getChildEdu() : 0) %></div></div>
            <div class="form-group"><label>继续教育</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getContEdu() != null ? deduction.getContEdu() : 0) %></div></div>
            <div class="form-group"><label>大病医疗</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getMajorMed() != null ? deduction.getMajorMed() : 0) %></div></div>
            <div class="form-group"><label>住房贷款利息</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getHousingLoan() != null ? deduction.getHousingLoan() : 0) %></div></div>
            <div class="form-group"><label>住房租金</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getHousingRent() != null ? deduction.getHousingRent() : 0) %></div></div>
            <div class="form-group"><label>赡养老人</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getSupportElderly() != null ? deduction.getSupportElderly() : 0) %></div></div>
            <div class="form-group"><label>婴幼儿照护</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getBabyCare() != null ? deduction.getBabyCare() : 0) %></div></div>
        </div>
        <a class="btn btn-primary" href="<%= request.getContextPath() %>/deduction">[返回列表]</a>
        <% } else { %>
        <form method="post" action="<%= request.getContextPath() %>/deduction">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="deductionId" value="<%= deduction != null && deduction.getDeductionId() != null ? deduction.getDeductionId() : "" %>">

            <div class="form-row">
                <div class="form-group">
                    <label>员工</label>
                    <select name="empId" required>
                        <option value="">请选择员工</option>
                        <% for (empInfo e : allEmployees) { %>
                        <option value="<%= e.getEmpId() %>" <%= (deduction != null && deduction.getEmpId() != null && deduction.getEmpId().equals(e.getEmpId())) ? "selected" : "" %>>
                            <%= e.getEmpNo() %> - <%= e.getEmpName() %>
                        </option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label>申报年份</label>
                    <input type="number" name="year" min="2000" max="2100" required value="<%= year != null ? year : currentYear %>">
                    <div class="hint">同一员工同一年只能有一条申报记录</div>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group"><label>子女教育（元）</label><input type="number" name="childEdu" step="0.01" min="0" value="<%= deduction != null && deduction.getChildEdu() != null ? deduction.getChildEdu() : "0.00" %>"></div>
                <div class="form-group"><label>继续教育（元）</label><input type="number" name="contEdu" step="0.01" min="0" value="<%= deduction != null && deduction.getContEdu() != null ? deduction.getContEdu() : "0.00" %>"></div>
                <div class="form-group"><label>大病医疗（元）</label><input type="number" name="majorMed" step="0.01" min="0" value="<%= deduction != null && deduction.getMajorMed() != null ? deduction.getMajorMed() : "0.00" %>"></div>
                <div class="form-group"><label>住房贷款利息（元）</label><input type="number" name="housingLoan" step="0.01" min="0" value="<%= deduction != null && deduction.getHousingLoan() != null ? deduction.getHousingLoan() : "0.00" %>"></div>
                <div class="form-group"><label>住房租金（元）</label><input type="number" name="housingRent" step="0.01" min="0" value="<%= deduction != null && deduction.getHousingRent() != null ? deduction.getHousingRent() : "0.00" %>"></div>
                <div class="form-group"><label>赡养老人（元）</label><input type="number" name="supportElderly" step="0.01" min="0" value="<%= deduction != null && deduction.getSupportElderly() != null ? deduction.getSupportElderly() : "0.00" %>"></div>
                <div class="form-group"><label>婴幼儿照护（元）</label><input type="number" name="babyCare" step="0.01" min="0" value="<%= deduction != null && deduction.getBabyCare() != null ? deduction.getBabyCare() : "0.00" %>"></div>
            </div>

            <button class="btn btn-primary" type="submit">[保存]</button>
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/deduction">[取消]</a>
        </form>
        <% } %>
    </div>
    <% } %>

    <div class="container">
        <div class="toolbar">
            <h3>所有员工申报记录（核心信息已脱敏）</h3>
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/deduction?action=add">[新建申报]</a>
        </div>
        <div class="table-container">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>员工编号</th>
                    <th>姓名(脱敏)</th>
                    <th>手机号(脱敏)</th>
                    <th>身份证(脱敏)</th>
                    <th>部门</th>
                    <th>岗位</th>
                    <th>年份</th>
                    <th class="number">子女教育</th>
                    <th class="number">继续教育</th>
                    <th class="number">大病医疗</th>
                    <th class="number">住房贷款</th>
                    <th class="number">住房租金</th>
                    <th class="number">赡养老人</th>
                    <th class="number">婴幼儿照护</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <% if (rows.isEmpty()) { %>
                <tr>
                    <td colspan="16" style="text-align:center;color:#999;">暂无申报记录</td>
                </tr>
                <% } else {
                    for (DeductionViewRow row : rows) {
                        taxDeduction r = row.getRecord();
                %>
                <tr>
                    <td><%= r.getDeductionId() %></td>
                    <td><%= row.getEmpNo() %></td>
                    <td><%= row.getEmpNameMasked() %></td>
                    <td><%= row.getPhoneMasked() %></td>
                    <td><%= row.getIdCardMasked() %></td>
                    <td><%= row.getDeptName() %></td>
                    <td><%= row.getPosition() %></td>
                    <td><%= r.getDeclareYear() %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getChildEdu() != null ? r.getChildEdu() : 0) %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getContEdu() != null ? r.getContEdu() : 0) %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getMajorMed() != null ? r.getMajorMed() : 0) %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getHousingLoan() != null ? r.getHousingLoan() : 0) %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getHousingRent() != null ? r.getHousingRent() : 0) %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getSupportElderly() != null ? r.getSupportElderly() : 0) %></td>
                    <td class="number">¥<%= String.format("%.2f", r.getBabyCare() != null ? r.getBabyCare() : 0) %></td>
                    <td>
                        <a class="btn btn-primary" href="<%= request.getContextPath() %>/deduction?action=view&id=<%= r.getDeductionId() %>">查看</a>
                        <a class="btn btn-primary" href="<%= request.getContextPath() %>/deduction?action=edit&id=<%= r.getDeductionId() %>">修改</a>
                        <button class="btn btn-danger" type="button" onclick="deleteDeduction(<%= r.getDeductionId() %>)">删除</button>
                    </td>
                </tr>
                <% }} %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<form id="deleteForm" method="post" action="<%= request.getContextPath() %>/deduction" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="deductionId" id="deleteDeductionId" value="">
</form>

<script>
function deleteDeduction(id) {
    if (!confirm('确认删除该申报记录?')) {
        return;
    }
    document.getElementById('deleteDeductionId').value = id;
    document.getElementById('deleteForm').submit();
}
</script>
</body>
</html>
