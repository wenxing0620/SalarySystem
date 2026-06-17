package com.salarysystem.servlet;

import com.salarysystem.model.empInfo;
import com.salarysystem.model.sysDept;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.service.impl.SysDeptServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@WebServlet("/emp-list")
public class EmpListServlet extends HttpServlet {

    private final EmpInfoServiceImpl empService = new EmpInfoServiceImpl();
    private final SysDeptServiceImpl deptService = new SysDeptServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String keyword = request.getParameter("keyword");

        try {
            // 加载员工列表
            List<empInfo> list = empService.findAll();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalized = keyword.trim().toLowerCase(Locale.ROOT);
                List<empInfo> filtered = new ArrayList<>();
                for (empInfo e : list) {
                    if (matches(e, normalized)) filtered.add(e);
                }
                list = filtered;
            }
            request.setAttribute("empList", list);
            request.setAttribute("keyword", keyword);

            // 加载部门列表（供弹窗下拉框使用）
            try {
                request.setAttribute("deptList", deptService.findAll());
            } catch (SQLException e) {
                request.setAttribute("deptList", new ArrayList<>());
            }

            // 编辑模式：加载指定员工数据到弹窗
            String editId = request.getParameter("editId");
            if (editId != null && !editId.isEmpty()) {
                try {
                    empInfo editEmp = empService.findById(Integer.parseInt(editId));
                    request.setAttribute("editEmployee", editEmp);
                } catch (Exception ignored) {}
            }

            request.setAttribute("error", null);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("empList", new ArrayList<>());
            request.setAttribute("deptList", new ArrayList<>());
            request.setAttribute("keyword", keyword);
            request.setAttribute("error", "无法加载员工列表，请稍后重试");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("empList", new ArrayList<>());
            request.setAttribute("deptList", new ArrayList<>());
            request.setAttribute("keyword", keyword);
            request.setAttribute("error", "系统错误，请稍后重试");
        }
        request.getRequestDispatcher("/emp-list.jsp").forward(request, response);
    }

    private boolean matches(empInfo e, String keyword) {
        return contains(e.getEmpNo(), keyword)
                || contains(e.getEmpName(), keyword)
                || contains(e.getDeptName(), keyword)
                || contains(e.getPosition(), keyword)
                || contains(e.getIdCard(), keyword)
                || contains(e.getPhone(), keyword);
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
