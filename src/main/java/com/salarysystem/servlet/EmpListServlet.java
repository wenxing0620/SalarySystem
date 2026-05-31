package com.salarysystem.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.model.empInfo;

@WebServlet("/emp-list")
public class EmpListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        String keyword = request.getParameter("keyword");
        // load real employee list
        EmpInfoServiceImpl svc = new EmpInfoServiceImpl();
        try {
            List<empInfo> list = svc.findAll();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String normalized = keyword.trim().toLowerCase(Locale.ROOT);
                List<empInfo> filtered = new ArrayList<>();
                for (empInfo e : list) {
                    if (matches(e, normalized)) {
                        filtered.add(e);
                    }
                }
                list = filtered;
            }
            request.setAttribute("empList", list);
            request.setAttribute("keyword", keyword);
            request.setAttribute("error", null);  // Clear any previous errors
        } catch (SQLException e) {
            System.err.println("Failed to load employee list: " + e.getMessage());
            // Set empty list and error message instead of crashing
            request.setAttribute("empList", new ArrayList<>());
            request.setAttribute("keyword", keyword);
            request.setAttribute("error", "无法加载员工列表: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error loading employee list: " + e.getMessage());
            request.setAttribute("empList", new ArrayList<>());
            request.setAttribute("keyword", keyword);
            request.setAttribute("error", "系统错误: " + e.getMessage());
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

