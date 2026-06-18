<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.empInfo, com.salarysystem.model.sysUser" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }
    empInfo emp = (empInfo) request.getAttribute("emp");
    if (emp == null) {
%>
    <div style="padding:40px;text-align:center;">员工不存在或无法加载<br><a class="btn btn-primary" href="<%= request.getContextPath() %>/emp-list">返回列表</a></div>
    <%
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>员工详情 - 薪资管理系统</title>
    <style>
        .info-table { width: 100%; border-collapse: collapse; }
        .info-table th { width: 120px; text-align: right; padding: 10px 15px; background: #f9f9f9; border-bottom: 1px solid #e0e0e0; color: #666; font-weight: normal; white-space: nowrap; }
        .info-table td { padding: 10px 15px; border-bottom: 1px solid #e0e0e0; color: #333; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "emp-list"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>员工详情 - <%= emp.getEmpNo() %></h2>
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/emp-list">« 返回列表</a>
        </div>

        <div class="table-container">
            <table class="info-table">
                <tr><th>员工编号</th><td><%= emp.getEmpNo() %></td></tr>
                <tr><th>姓名</th><td><%= emp.getEmpName() != null ? emp.getEmpName() : "-" %></td></tr>
                <tr><th>部门</th><td><%= emp.getDeptName() != null ? emp.getDeptName() : "-" %></td></tr>
                <tr><th>岗位</th><td><%= emp.getPosition() != null ? emp.getPosition() : "-" %></td></tr>
                <tr><th>身份证</th><td><%= emp.getIdCard() != null ? emp.getIdCard() : "-" %></td></tr>
                <tr><th>手机号</th><td><%= emp.getPhone() != null ? emp.getPhone() : "-" %></td></tr>
                <tr><th>住址</th><td><%= emp.getAddress() != null ? emp.getAddress() : "-" %></td></tr>
            </table>
        </div>
    </div>
</div>
</body>
</html>
