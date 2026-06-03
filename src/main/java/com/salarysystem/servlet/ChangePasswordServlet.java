package com.salarysystem.servlet;

import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.SysUserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final SysUserServiceImpl userService = new SysUserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        req.getRequestDispatcher("/change-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser user = (sysUser) session.getAttribute("currentUser");
        String oldPassword = req.getParameter("oldPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        // 校验
        if (oldPassword == null || oldPassword.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()) {
            req.setAttribute("error", "密码不能为空");
            req.getRequestDispatcher("/change-password.jsp").forward(req, resp);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            req.setAttribute("error", "两次输入的新密码不一致");
            req.getRequestDispatcher("/change-password.jsp").forward(req, resp);
            return;
        }

        if (newPassword.equals(oldPassword)) {
            req.setAttribute("error", "新密码不能与旧密码相同");
            req.getRequestDispatcher("/change-password.jsp").forward(req, resp);
            return;
        }

        try {
            userService.changePassword(user.getUserId(), oldPassword.trim(), newPassword.trim());
            // 清除过期标记
            session.removeAttribute("pendingPasswordChange");
            req.setAttribute("success", "密码修改成功");
            req.getRequestDispatcher("/change-password.jsp").forward(req, resp);
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/change-password.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "系统错误：" + e.getMessage());
            req.getRequestDispatcher("/change-password.jsp").forward(req, resp);
        }
    }
}
