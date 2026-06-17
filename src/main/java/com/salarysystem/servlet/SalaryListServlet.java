package com.salarysystem.servlet;

import com.salarysystem.model.empInfo;
import com.salarysystem.model.salaryRecord;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.service.impl.SalaryRecordServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet({"/salary-list", "/salary"})
public class SalaryListServlet extends HttpServlet {

    private final SalaryRecordServiceImpl salaryService = new SalaryRecordServiceImpl();
    private final EmpInfoServiceImpl empService = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = safe(req.getParameter("action"));
        try {
            if ("add".equals(action)) {
                req.setAttribute("editMode", true);
                req.setAttribute("employees", empService.findAll());
                req.setAttribute("salaryRecord", new salaryRecord());
            } else if ("edit".equals(action) || "view".equals(action)) {
                Long id = parseLong(req.getParameter("id"));
                if (id == null) {
                    session.setAttribute("message", "缺少记录ID");
                    resp.sendRedirect(req.getContextPath() + "/salary-list");
                    return;
                }
                salaryRecord record = salaryService.findById(id);
                if (record == null) {
                    session.setAttribute("message", "薪资记录不存在");
                    resp.sendRedirect(req.getContextPath() + "/salary-list");
                    return;
                }
                req.setAttribute("salaryRecord", record);
                req.setAttribute("employees", empService.findAll());
                if ("edit".equals(action)) req.setAttribute("editMode", true);
                else req.setAttribute("viewMode", true);
            }

            prepareList(req);
            try {
                logService.log(((sysUser) session.getAttribute("currentUser")).getUserId(), "QUERY_SALARY", req.getRemoteAddr());
            } catch (Exception logEx) {
                // 日志写入失败不影响业务流程
            }
            req.getRequestDispatcher("/salary-list.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "薪资页面加载失败，请稍后重试");
            req.setAttribute("salaryRows", new ArrayList<>());
            req.setAttribute("employees", new ArrayList<>());
            req.getRequestDispatcher("/salary-list.jsp").forward(req, resp);
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
            session.setAttribute("message", "权限不足：总经理只能查看，不能操作薪资记录");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
            return;
        }

        String action = safe(req.getParameter("action"));
        try {
            if ("delete".equals(action)) {
                Long id = parseLong(req.getParameter("recordId"));
                if (id != null) {
                    salaryService.delete(id);
                    session.setAttribute("message", "薪资记录删除成功");
                } else {
                    session.setAttribute("message", "删除失败：缺少记录ID");
                }
                resp.sendRedirect(req.getContextPath() + "/salary-list");
                return;
            }

            if (!"save".equals(action)) {
                resp.sendRedirect(req.getContextPath() + "/salary-list");
                return;
            }

            Long recordId = parseLong(req.getParameter("recordId"));
            Integer empId = parseInt(req.getParameter("empId"));
            String salaryMonth = safe(req.getParameter("salaryMonth"));
            Integer expectedDays = parseInt(req.getParameter("expectedDays"));
            Integer actualDays = parseInt(req.getParameter("actualDays"));
            BigDecimal basicSalary = parseDecimal(req.getParameter("basicSalary"));
            BigDecimal positionAllowance = parseDecimal(req.getParameter("positionAllowance"));
            BigDecimal lunchAllowance = parseDecimal(req.getParameter("lunchAllowance"));
            BigDecimal overtimeSalary = parseDecimal(req.getParameter("overtimeSalary"));
            BigDecimal fullAttendSalary = parseDecimal(req.getParameter("fullAttendSalary"));
            BigDecimal socialSecurity = parseDecimal(req.getParameter("socialSecurity"));
            BigDecimal providentFund = parseDecimal(req.getParameter("providentFund"));
            BigDecimal tax = parseDecimal(req.getParameter("tax"));
            BigDecimal absenceDeduction = parseDecimal(req.getParameter("absenceDeduction"));
            BigDecimal actualSalary = parseDecimal(req.getParameter("actualSalary"));

            if (empId == null || salaryMonth.isEmpty()) {
                session.setAttribute("message", "保存失败：员工和月份不能为空");
                resp.sendRedirect(req.getContextPath() + "/salary-list");
                return;
            }

            empInfo emp = empService.findById(empId);
            if (emp == null) {
                session.setAttribute("message", "保存失败：员工不存在");
                resp.sendRedirect(req.getContextPath() + "/salary-list");
                return;
            }

            salaryRecord exists = salaryService.findByEmpIdAndMonth(empId, salaryMonth);
            if (exists != null && !exists.getRecordId().equals(recordId)) {
                session.setAttribute("message", "保存失败：该员工该月份已存在工资记录");
                resp.sendRedirect(req.getContextPath() + "/salary-list");
                return;
            }

            salaryRecord record = new salaryRecord();
            record.setRecordId(recordId);
            record.setEmpId(empId);
            record.setSalaryMonth(salaryMonth);
            record.setExpectedDays(expectedDays == null ? 0 : expectedDays);
            record.setActualDays(actualDays == null ? 0 : actualDays);
            record.setBasicSalary(basicSalary);
            record.setPositionAllowance(positionAllowance);
            record.setLunchAllowance(lunchAllowance);
            record.setOvertimeSalary(overtimeSalary);
            record.setFullAttendSalary(fullAttendSalary);
            record.setSocialSecurity(socialSecurity);
            record.setProvidentFund(providentFund);
            record.setTax(tax);
            record.setAbsenceDeduction(absenceDeduction);
            record.setActualSalary(actualSalary);

            if (recordId == null) {
                salaryService.add(record);
                session.setAttribute("message", "薪资记录新增成功");
            } else {
                salaryService.update(record);
                session.setAttribute("message", "薪资记录更新成功");
            }
            resp.sendRedirect(req.getContextPath() + "/salary-list");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "操作失败，请稍后重试");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
        }
    }

    private void prepareList(HttpServletRequest req) throws SQLException {
        // 读取筛选参数
        String keyword = safe(req.getParameter("keyword"));
        String dept = safe(req.getParameter("dept"));
        String startMonth = safe(req.getParameter("startMonth"));
        String endMonth = safe(req.getParameter("endMonth"));

        List<salaryRecord> salaryList = salaryService.findAll();
        List<empInfo> employees = empService.findAll();
        Map<Integer, empInfo> empMap = new HashMap<>();
        for (empInfo emp : employees) {
            empMap.put(emp.getEmpId(), emp);
        }

        // 多条件筛选
        String kw = keyword.toLowerCase();
        String d = dept.toLowerCase();
        salaryList.removeIf(r -> {
            empInfo e = empMap.get(r.getEmpId());
            // 时间段筛选
            if (!startMonth.isEmpty() && r.getSalaryMonth() != null
                    && r.getSalaryMonth().compareTo(startMonth) < 0) return true;
            if (!endMonth.isEmpty() && r.getSalaryMonth() != null
                    && r.getSalaryMonth().compareTo(endMonth) > 0) return true;
            // 部门筛选
            if (!d.isEmpty() && (e == null || e.getDeptName() == null
                    || !e.getDeptName().toLowerCase().contains(d))) return true;
            // 关键词筛选（员工编号 或 员工姓名）
            if (!kw.isEmpty()) {
                if (e == null) return true;
                boolean matchNo = e.getEmpNo() != null && e.getEmpNo().toLowerCase().contains(kw);
                boolean matchName = e.getEmpName() != null && e.getEmpName().toLowerCase().contains(kw);
                if (!matchNo && !matchName) return true;
            }
            return false;
        });

        List<SalaryRow> rows = new ArrayList<>();
        for (salaryRecord r : salaryList) {
            rows.add(new SalaryRow(r, empMap.get(r.getEmpId())));
        }

        // 回传筛选参数
        req.setAttribute("keyword", keyword);
        req.setAttribute("dept", dept);
        req.setAttribute("startMonth", startMonth);
        req.setAttribute("endMonth", endMonth);

        req.setAttribute("salaryRows", rows);
        req.setAttribute("employees", employees);
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Long.parseLong(value.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return null; }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(value.trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 薪资记录展示行：关联 salaryRecord 和 empInfo，供 JSP 使用
     */
    public static class SalaryRow {
        private final salaryRecord record;
        private final empInfo emp;

        public SalaryRow(salaryRecord record, empInfo emp) {
            this.record = record;
            this.emp = emp;
        }

        public salaryRecord getRecord() {
            return record;
        }

        public String getEmpNo() {
            return emp == null || emp.getEmpNo() == null ? "-" : emp.getEmpNo();
        }

        public String getEmpName() {
            return emp == null || emp.getEmpName() == null ? "-" : emp.getEmpName();
        }

        public String getDeptName() {
            return emp == null || emp.getDeptName() == null ? "-" : emp.getDeptName();
        }

        public String getPosition() {
            return emp == null || emp.getPosition() == null ? "-" : emp.getPosition();
        }
    }
}
