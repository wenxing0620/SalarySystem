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

@WebServlet("/emp-edit")
public class EmpEditServlet extends HttpServlet {

    private final EmpInfoServiceImpl svc = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 功能已移至 emp-list.jsp 弹窗
        response.sendRedirect(request.getContextPath() + "/emp-list");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        sysUser currentUser = null;
        Integer userId = null;
        if (session != null) {
            currentUser = (sysUser) session.getAttribute("currentUser");
            if (currentUser != null) {
                userId = currentUser.getUserId();
                if ("总经理".equals(session.getAttribute("currentUserRole"))) {
                    session.setAttribute("message", "权限不足：总经理只能查看，不能编辑员工");
                    response.sendRedirect(request.getContextPath() + "/emp-list");
                    return;
                }
            }
        }
        String clientIp = getClientIp(request);

        empInfo e = new empInfo();
        try { e.setEmpId(Integer.parseInt(request.getParameter("empId"))); } catch (NumberFormatException ex) {
            session.setAttribute("message", "参数错误：无效的员工ID");
            response.sendRedirect(request.getContextPath() + "/emp-list");
            return;
        }
        e.setEmpNo(request.getParameter("empNo"));
        e.setDeptName(request.getParameter("deptName"));
        e.setPosition(request.getParameter("position"));
        e.setEmpName(request.getParameter("empName"));
        e.setIdCard(request.getParameter("idCard"));
        e.setPhone(request.getParameter("phone"));
        e.setAddress(request.getParameter("address"));

        try {
            svc.update(e);
            try { logService.log(userId, "UPDATE_EMP", clientIp); } catch (SQLException ignored) {}
            session.setAttribute("message", "员工更新成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            session.setAttribute("message", "更新失败：" + ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/emp-list");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
