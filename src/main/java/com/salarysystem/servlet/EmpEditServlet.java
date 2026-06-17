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
        String empNo = request.getParameter("empNo");
        String empName = request.getParameter("empName");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");

        // 必填字段校验
        if (empNo == null || empNo.trim().isEmpty() ||
            empName == null || empName.trim().isEmpty() ||
            idCard == null || idCard.trim().isEmpty()) {
            session.setAttribute("message", "员工编号、姓名、身份证不能为空");
            response.sendRedirect(request.getContextPath() + "/emp-list");
            return;
        }

        String idCardStr = idCard.trim();
        if (!idCardStr.matches("^\\d{17}[\\dXx]$")) {
            session.setAttribute("message", "身份证号应为18位，最后一位可为数字或X");
            response.sendRedirect(request.getContextPath() + "/emp-list");
            return;
        }

        String phoneStr = phone != null ? phone.trim() : "";
        if (!phoneStr.isEmpty() && !phoneStr.matches("^\\d{11}$")) {
            session.setAttribute("message", "手机号应为11位数字");
            response.sendRedirect(request.getContextPath() + "/emp-list");
            return;
        }

        e.setEmpNo(empNo.trim());
        e.setDeptName(request.getParameter("deptName"));
        e.setPosition(request.getParameter("position"));
        e.setEmpName(empName.trim());
        e.setIdCard(idCardStr);
        e.setPhone(phoneStr);
        e.setAddress(request.getParameter("address"));

        try {
            // 身份证唯一性检查（排除自身）
            for (empInfo emp : svc.findAll()) {
                if (idCardStr.equals(emp.getIdCard()) && !emp.getEmpId().equals(e.getEmpId())) {
                    session.setAttribute("message", "该身份证号已被其他员工使用");
                    response.sendRedirect(request.getContextPath() + "/emp-list");
                    return;
                }
            }

            // 员工编号唯一性检查（排除自身）
            empInfo existByNo = svc.findByEmpNo(empNo.trim());
            if (existByNo != null && !existByNo.getEmpId().equals(e.getEmpId())) {
                session.setAttribute("message", "员工编号【" + empNo.trim() + "】已存在");
                response.sendRedirect(request.getContextPath() + "/emp-list");
                return;
            }

            svc.update(e);
            try { logService.log(userId, "UPDATE_EMP", clientIp); } catch (SQLException ignored) {}
            session.setAttribute("message", "员工更新成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            session.setAttribute("message", "更新失败，请稍后重试");
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
