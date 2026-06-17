package com.salarysystem.servlet;

import com.salarysystem.model.empFamily;
import com.salarysystem.model.empInfo;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.EmpFamilyServiceImpl;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import com.salarysystem.util.DesensitizeUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/family")
public class FamilyServlet extends HttpServlet {

    private final EmpFamilyServiceImpl familyService = new EmpFamilyServiceImpl();
    private final EmpInfoServiceImpl empService = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser user = (sysUser) session.getAttribute("currentUser");
        String action = safe(req.getParameter("action"));

        try {
            if ("add".equals(action)) {
                req.setAttribute("editMode", true);
            } else if ("edit".equals(action)) {
                Integer id = parseInt(req.getParameter("id"));
                if (id != null) {
                    empFamily family = familyService.findById(id);
                    if (family != null) {
                        req.setAttribute("family", family);
                        req.setAttribute("editMode", true);
                    } else {
                        session.setAttribute("message", "家属记录不存在");
                    }
                }
            }

            prepareList(req);
            logService.log(user.getUserId(), "QUERY_FAMILY", req.getRemoteAddr());
            req.getRequestDispatcher("/family.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "家属页面加载失败，请稍后重试");
            req.setAttribute("familyRows", new ArrayList<>());
            req.setAttribute("allEmployees", new ArrayList<>());
            req.getRequestDispatcher("/family.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser user = (sysUser) session.getAttribute("currentUser");

        // 总经理只能查看，不能增删改
        if ("总经理".equals(session.getAttribute("currentUserRole"))) {
            session.setAttribute("message", "权限不足：总经理只能查看，不能操作家属信息");
            resp.sendRedirect(req.getContextPath() + "/family");
            return;
        }

        String action = safe(req.getParameter("action"));

        try {
            if ("delete".equals(action)) {
                Integer id = parseInt(req.getParameter("id"));
                if (id != null) {
                    familyService.delete(id);
                    logService.log(user.getUserId(), "DELETE_FAMILY", req.getRemoteAddr());
                    session.setAttribute("message", "家属记录删除成功");
                }
                resp.sendRedirect(req.getContextPath() + "/family");
                return;
            }

            if (!"save".equals(action)) {
                resp.sendRedirect(req.getContextPath() + "/family");
                return;
            }

            // 保存/更新
            Integer familyId = parseInt(req.getParameter("familyId"));
            Integer empId = parseInt(req.getParameter("empId"));
            String relation = safe(req.getParameter("relation"));
            String name = safe(req.getParameter("name"));
            String idCard = safe(req.getParameter("idCard"));

            if (empId == null || relation.isEmpty() || name.isEmpty() || idCard.isEmpty()) {
                session.setAttribute("message", "保存失败：员工、关系、姓名、身份证均不能为空");
                resp.sendRedirect(req.getContextPath() + "/family");
                return;
            }

            // 身份证格式校验（18位）
            if (!idCard.matches("^\\d{17}[\\dXx]$")) {
                session.setAttribute("message", "家属身份证号应为18位，最后一位可为数字或X");
                resp.sendRedirect(req.getContextPath() + "/family");
                return;
            }

            empInfo emp = empService.findById(empId);
            if (emp == null) {
                session.setAttribute("message", "保存失败：员工不存在");
                resp.sendRedirect(req.getContextPath() + "/family");
                return;
            }

            empFamily family = new empFamily();
            family.setFamilyId(familyId);
            family.setEmpId(empId);
            family.setRelation(relation);
            family.setName(name);
            family.setIdCard(idCard);

            if (familyId == null) {
                familyService.add(family);
                logService.log(user.getUserId(), "ADD_FAMILY", req.getRemoteAddr());
                session.setAttribute("message", "家属记录新增成功");
            } else {
                familyService.update(family);
                logService.log(user.getUserId(), "UPDATE_FAMILY", req.getRemoteAddr());
                session.setAttribute("message", "家属记录更新成功");
            }
            resp.sendRedirect(req.getContextPath() + "/family");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "操作失败，请稍后重试");
            resp.sendRedirect(req.getContextPath() + "/family");
        }
    }

    private void prepareList(HttpServletRequest req) throws SQLException {
        String keyword = safe(req.getParameter("keyword"));
        Integer filterEmpId = parseInt(req.getParameter("filterEmpId"));

        List<empFamily> families = familyService.findAll();
        List<empInfo> employees = empService.findAll();
        Map<Integer, empInfo> empMap = new HashMap<>();
        for (empInfo e : employees) empMap.put(e.getEmpId(), e);

        // 筛选
        if (filterEmpId != null) {
            families.removeIf(f -> !filterEmpId.equals(f.getEmpId()));
        }
        if (!keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            families.removeIf(f -> {
                empInfo e = empMap.get(f.getEmpId());
                return !(contains(f.getRelation(), kw) ||
                         contains(f.getName(), kw) ||
                         contains(f.getIdCard(), kw) ||
                         (e != null && (contains(e.getEmpNo(), kw) || contains(e.getEmpName(), kw))));
            });
        }

        List<FamilyViewRow> rows = new ArrayList<>();
        for (empFamily f : families) {
            rows.add(new FamilyViewRow(f, empMap.get(f.getEmpId())));
        }

        req.setAttribute("familyRows", rows);
        req.setAttribute("allEmployees", employees);
        req.setAttribute("keyword", keyword);
        req.setAttribute("filterEmpId", filterEmpId != null ? filterEmpId : "");
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return null; }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    /**
     * 家属展示行：关联 empFamily 和 empInfo
     */
    public static class FamilyViewRow {
        private final empFamily family;
        private final empInfo emp;

        public FamilyViewRow(empFamily family, empInfo emp) {
            this.family = family;
            this.emp = emp;
        }

        public empFamily getFamily() { return family; }
        public String getEmpNo() { return emp != null && emp.getEmpNo() != null ? emp.getEmpNo() : "-"; }
        public String getEmpName() { return emp != null && emp.getEmpName() != null ? emp.getEmpName() : "-"; }
        public String getDeptName() { return emp != null && emp.getDeptName() != null ? emp.getDeptName() : "-"; }
        public String getNameMasked() { return DesensitizeUtil.maskName(family.getName()); }
        public String getIdCardMasked() { return DesensitizeUtil.maskIdCard(family.getIdCard()); }
    }
}
