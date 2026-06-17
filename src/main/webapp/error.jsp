<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>系统错误 - 薪资管理系统</title>
    <style>
        .error-wrap { max-width: 720px; margin: 80px auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
        .error-wrap h1 { color: #e74c3c; font-size: 22px; margin-bottom: 12px; }
        .error-wrap p { color: #666; line-height: 1.6; margin-bottom: 6px; }
        .error-wrap details { margin-top: 10px; }
        .error-wrap summary { cursor: pointer; color: #667eea; }
        .error-wrap pre { background: #f5f5f5; padding: 10px; overflow-x: auto; font-size: 11px; max-height: 400px; }
    </style>
</head>
<body>
<div class="error-wrap">
    <h1>系统出现异常</h1>
    <%
        String errorAttr = (String) request.getAttribute("error");
        String msg = request.getParameter("msg");
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        String servletName = (String) request.getAttribute("jakarta.servlet.error.servlet_name");

        if (errorAttr != null && !errorAttr.isEmpty()) {
    %>
    <p style="color:#e74c3c;font-weight:bold;"><%= errorAttr %></p>
    <%
        } else if (exception != null) {
            // 记录详细错误到服务器日志，页面只显示简要信息
            exception.printStackTrace();
    %>
    <p>系统出现异常，请稍后重试或联系管理员。</p>
    <%
        } else if (msg != null && !msg.isEmpty()) {
    %>
    <p><%= msg %></p>
    <%
        } else {
    %>
    <p>请稍后重试，或联系管理员。</p>
    <%
        }
    %>
    <p>如果是数据库未初始化，请先执行 <code>create-tables.sql</code> 和 <code>init-data.sql</code>。</p>
    <a class="btn btn-primary" href="<%= request.getContextPath() %>/login.jsp" style="margin-top:16px;">返回登录</a>
</div>
</body>
</html>
