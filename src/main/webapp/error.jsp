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
    %>
    <p><strong>异常类型：</strong><%= exception.getClass().getName() %></p>
    <p><strong>异常消息：</strong><%= exception.getMessage() != null ? exception.getMessage() : "(无消息)" %></p>
    <%
        Throwable cause = exception.getCause();
        if (cause != null) {
    %>
    <p><strong>根因：</strong><%= cause.getClass().getName() %> - <%= cause.getMessage() != null ? cause.getMessage() : "(无消息)" %></p>
    <%
        }
    %>
    <p><strong>请求URI：</strong><%= requestUri != null ? requestUri : "未知" %></p>
    <p><strong>Servlet：</strong><%= servletName != null ? servletName : "未知" %></p>
    <p><strong>状态码：</strong><%= statusCode != null ? statusCode : "未知" %></p>
    <details>
        <summary>查看堆栈跟踪</summary>
        <pre><%
            if (exception != null) {
                java.io.StringWriter sw = new java.io.StringWriter();
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                exception.printStackTrace(pw);
                pw.flush();
                out.print(sw.toString().replace("<", "&lt;").replace(">", "&gt;"));
            }
        %></pre>
    </details>
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
