package com.salarysystem.servlet;

import com.salarysystem.model.empInfo;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/emp-view")
public class EmpViewServlet extends HttpServlet {

    private final EmpInfoServiceImpl svc = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/emp-list");
            return;
        }
        
        // Get current user from session
        HttpSession session = request.getSession(false);
        sysUser currentUser = null;
        Integer userId = null;
        if (session != null) {
            currentUser = (sysUser) session.getAttribute("currentUser");
            if (currentUser != null) {
                userId = currentUser.getUserId();
            }
        }
        String clientIp = getClientIp(request);
        
        try {
            empInfo e = svc.findById(Integer.parseInt(id));
            if (e == null) {
                request.setAttribute("error", "员工不存在");
            } else {
                request.setAttribute("emp", e);
                // Log the operation
                try {
                    logService.log(userId, "VIEW_EMP", clientIp);
                    System.out.println("Logged: user " + userId + " viewed employee " + e.getEmpNo());
                } catch (SQLException logEx) {
                    System.err.println("Failed to log operation: " + logEx.getMessage());
                }
            }
            request.getRequestDispatcher("/emp-view.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/emp-view.jsp").forward(request, response);
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

