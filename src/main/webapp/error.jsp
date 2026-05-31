<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>系统错误</title>
    <style>
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .wrap { max-width: 720px; margin: 80px auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
        h1 { color: #e74c3c; font-size: 22px; margin-bottom: 12px; }
        p { color: #666; line-height: 1.6; }
        .btn { display: inline-block; margin-top: 16px; padding: 8px 16px; background: #667eea; color: #fff; text-decoration: none; border-radius: 4px; }
    </style>
</head>
<body>
<div class="wrap">
    <h1>系统出现异常</h1>
    <p>请稍后重试，或联系管理员。</p>
    <p>如果是数据库未初始化，请先执行 `create-tables.sql` 和 `init-data.sql`。</p>
    <a class="btn" href="login.jsp">返回登录</a>
</div>
</body>
</html>

