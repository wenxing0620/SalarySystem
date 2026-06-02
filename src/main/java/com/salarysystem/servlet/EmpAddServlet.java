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
                if (currentUser.getRoleId() != null && currentUser.getRoleId() == 4) {
                    session.setAttribute("message", "权限不足：总经理只能查看，不能新增员工");
                    response.sendRedirect(request.getContextPath() + "/emp-list");
                    return;
                }
            }
        }
        String clientIp = getClientIp(request);

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
            session.setAttribute("message", "员工编号、姓名、身份证不能为空");
            response.sendRedirect(request.getContextPath() + "/emp-list");
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
            try { logService.log(userId, "ADD_EMP", clientIp); } catch (SQLException ignored) {}
            session.setAttribute("message", "员工新增成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            session.setAttribute("message", "添加失败：" + ex.getMessage());
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
