<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.empInfo, com.salarysystem.model.sysUser" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }
    empInfo emp = (empInfo) request.getAttribute("emp");
    if (emp == null) emp = new empInfo();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑员工</title>
</head>
<body>
<h2>编辑员工</h2>
<%
    if (request.getAttribute("error") != null) {
%>
    <div style="color:red"><%= request.getAttribute("error") %></div>
<%
    }
%>
<form method="post" action="<%= request.getContextPath() %>/emp-edit">
    <input type="hidden" name="empId" value="<%= emp.getEmpId() == null ? "" : emp.getEmpId() %>">
    员工编号: <input name="empNo" value="<%= emp.getEmpNo() == null ? "" : emp.getEmpNo() %>" required><br>
    部门: <input name="deptName" value="<%= emp.getDeptName() == null ? "" : emp.getDeptName() %>"><br>
    岗位: <input name="position" value="<%= emp.getPosition() == null ? "" : emp.getPosition() %>"><br>
    姓名(明文，会在DAO加密): <input name="empName" value="<%= emp.getEmpName() == null ? "" : emp.getEmpName() %>"><br>
    身份证(明文，会在DAO加密): <input name="idCard" value="<%= emp.getIdCard() == null ? "" : emp.getIdCard() %>"><br>
    手机号(明文，会在DAO加密): <input name="phone" value="<%= emp.getPhone() == null ? "" : emp.getPhone() %>"><br>
    住址(明文，会在DAO加密): <input name="address" value="<%= emp.getAddress() == null ? "" : emp.getAddress() %>"><br>
    <button type="submit">保存</button>
    <a href="<%= request.getContextPath() %>/emp-list">取消</a>
</form>
</body>
</html>

