<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.empInfo, com.salarysystem.model.sysUser" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }
    empInfo emp = (empInfo) request.getAttribute("emp");
    if (emp == null) {
%>
    <div>员工不存在或无法加载</div>
    <a href="<%= request.getContextPath() %>/emp-list">返回列表</a>
<%
    } else {
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>查看员工</title>
</head>
<body>
<h2>员工详情 - <%= emp.getEmpNo() %></h2>
<ul>
    <li>姓名: <%= emp.getEmpName() %></li>
    <li>部门: <%= emp.getDeptName() %></li>
    <li>岗位: <%= emp.getPosition() %></li>
    <li>身份证: <%= emp.getIdCard() %></li>
    <li>手机号: <%= emp.getPhone() %></li>
    <li>住址: <%= emp.getAddress() %></li>
</ul>
<a href="<%= request.getContextPath() %>/emp-list">返回列表</a>
</body>
</html>
<%
    }
%>

