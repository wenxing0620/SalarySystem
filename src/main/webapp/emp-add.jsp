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
    <title>新增员工 - 薪资管理系统</title>
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
        .container {
            max-width: 600px;
            margin: 30px auto;
            padding: 20px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        h2 {
            margin-bottom: 20px;
            color: #333;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: 600;
            color: #333;
        }
        input, textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            font-family: inherit;
        }
        input:focus, textarea:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 5px rgba(102, 126, 234, 0.3);
        }
        .form-buttons {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }
        button, a.btn {
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            font-size: 14px;
            transition: all 0.3s;
        }
        button[type="submit"] {
            background: #667eea;
            color: white;
            flex: 1;
        }
        button[type="submit"]:hover {
            background: #5568d3;
        }
        a.btn-cancel {
            background: #999;
            color: white;
            flex: 1;
            text-align: center;
        }
        a.btn-cancel:hover {
            background: #777;
        }
        .alert {
            padding: 12px;
            margin-bottom: 20px;
            border-radius: 4px;
        }
        .alert-error {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        .alert-success {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
        }
        .help-text {
            font-size: 12px;
            color: #999;
            margin-top: 3px;
        }
    </style>
</head>
<body>
<div class="navbar">
    <h1>薪资管理系统</h1>
    <div class="navbar-right">
        <span>欢迎, <%= user.getUsername() %></span>
        <a href="<%= request.getContextPath() %>/logout">登出</a>
    </div>
</div>

<div class="container">
    <h2>新增员工</h2>

    <%
        if (request.getAttribute("error") != null) {
    %>
    <div class="alert alert-error">
        <strong>错误：</strong><%= request.getAttribute("error") %>
    </div>
    <%
        }
    %>

    <form method="post" action="<%= request.getContextPath() %>/emp-add" onsubmit="return validateForm()">
        <div class="form-group">
            <label for="empNo">员工编号 *</label>
            <input type="text" id="empNo" name="empNo" required placeholder="如 EMP001">
            <div class="help-text">必填项，需唯一</div>
        </div>

        <div class="form-group">
            <label for="deptName">部门</label>
            <input type="text" id="deptName" name="deptName" placeholder="如 技术部">
        </div>

        <div class="form-group">
            <label for="position">岗位</label>
            <input type="text" id="position" name="position" placeholder="如 工程师">
        </div>

        <div class="form-group">
            <label for="empName">姓名 *</label>
            <input type="text" id="empName" name="empName" required placeholder="如 李明">
            <div class="help-text">必填项，会被 SM4 加密存储</div>
        </div>

        <div class="form-group">
            <label for="idCard">身份证号 *</label>
            <input type="text" id="idCard" name="idCard" required placeholder="18 位身份证号">
            <div class="help-text">必填项，会被 SM4 加密存储</div>
        </div>

        <div class="form-group">
            <label for="phone">手机号</label>
            <input type="text" id="phone" name="phone" placeholder="如 13912345678">
            <div class="help-text">会被 SM4 加密存储</div>
        </div>

        <div class="form-group">
            <label for="address">住址</label>
            <textarea id="address" name="address" placeholder="如 北京市朝阳区..."></textarea>
            <div class="help-text">会被 SM4 加密存储</div>
        </div>

        <div class="form-buttons">
            <button type="submit">保存</button>
            <a href="<%= request.getContextPath() %>/emp-list" class="btn-cancel">取消</a>
        </div>
    </form>
</div>

<script>
function validateForm() {
    let empNo = document.getElementById('empNo').value.trim();
    let empName = document.getElementById('empName').value.trim();
    let idCard = document.getElementById('idCard').value.trim();

    if (!empNo) {
        alert('员工编号不能为空');
        return false;
    }
    if (!empName) {
        alert('姓名不能为空');
        return false;
    }
    if (!idCard) {
        alert('身份证号不能为空');
        return false;
    }
    if (idCard.length !== 18) {
        alert('身份证号应为 18 位');
        return false;
    }
    return true;
}
</script>
</body>
</html>


