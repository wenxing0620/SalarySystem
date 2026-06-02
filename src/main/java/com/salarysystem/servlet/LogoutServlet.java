package com.salarysystem.servlet;

import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.SysLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                // 在销毁 session 前记录登出日志
                sysUser user = (sysUser) session.getAttribute("currentUser");
                if (user != null) {
                    try {
                        logService.log(user.getUserId(), "LOGOUT", getClientIp(request));
                    } catch (Exception ignored) {
                        // 日志记录失败不影响登出流程
                    }
                }
                session.invalidate();
            }
        } catch (Exception e) {
            // 确保任何异常都不影响登出
            e.printStackTrace();
        }
        // URL 编码中文参数，避免乱码或白屏
        String msg = URLEncoder.encode("已安全登出", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login.jsp?success=" + msg);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
