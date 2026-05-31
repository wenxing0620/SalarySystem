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

@WebServlet("/emp-add")
public class EmpAddServlet extends HttpServlet {

    private final EmpInfoServiceImpl svc = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/emp-add.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

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

        // Validate required fields
        String empNo = request.getParameter("empNo");
        String deptName = request.getParameter("deptName");
        String position = request.getParameter("position");
        String empName = request.getParameter("empName");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        if (empNo == null || empNo.trim().isEmpty() ||
            empName == null || empName.trim().isEmpty() ||
            idCard == null || idCard.trim().isEmpty()) {
            request.setAttribute("error", "员工编号、姓名、身份证不能为空");
            request.getRequestDispatcher("/emp-add.jsp").forward(request, response);
            return;
        }

        empInfo e = new empInfo();
        e.setEmpNo(empNo.trim());
        e.setDeptName(deptName != null ? deptName.trim() : "");
        e.setPosition(position != null ? position.trim() : "");
        e.setEmpName(empName.trim());
        e.setIdCard(idCard.trim());
        e.setPhone(phone != null ? phone.trim() : "");
        e.setAddress(address != null ? address.trim() : "");

        try {
            svc.create(e);
            System.out.println("Employee created successfully: " + empNo);
            // Log the operation
            try {
                logService.log(userId, "ADD_EMP", clientIp);
                System.out.println("Logged: user " + userId + " added employee " + empNo);
            } catch (SQLException logEx) {
                System.err.println("Failed to log operation: " + logEx.getMessage());
            }
            // Redirect to employee list
            response.sendRedirect(request.getContextPath() + "/emp-list");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Failed to create employee: " + ex.getMessage());
            request.setAttribute("error", "添加失败：" + ex.getMessage());
            request.getRequestDispatcher("/emp-add.jsp").forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Unexpected error: " + ex.getMessage());
            request.setAttribute("error", "系统错误：" + ex.getMessage());
            request.getRequestDispatcher("/emp-add.jsp").forward(request, response);
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

