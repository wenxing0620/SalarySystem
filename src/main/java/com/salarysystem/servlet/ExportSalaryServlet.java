package com.salarysystem.servlet;

import com.salarysystem.model.empInfo;
import com.salarysystem.model.salaryRecord;
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/salary-export")
public class ExportSalaryServlet extends HttpServlet {

    private final SalaryRecordServiceImpl svc = new SalaryRecordServiceImpl();
    private final EmpInfoServiceImpl empSvc = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer userId = null;
        if (session != null && session.getAttribute("currentUser") != null) {
            userId = ((com.salarysystem.model.sysUser) session.getAttribute("currentUser")).getUserId();
        }

        String month = request.getParameter("month");
        String empName = request.getParameter("empName");
        String dept = request.getParameter("dept");

        try {
            List<salaryRecord> list = svc.findAll();

            // apply simple filters
            if (month != null && !month.trim().isEmpty()) {
                String m = month.trim();
                list.removeIf(r -> !m.equals(r.getSalaryMonth()));
            }

            // When filtering by employee name or dept, look up empInfo for each record
            if ((empName != null && !empName.trim().isEmpty()) || (dept != null && !dept.trim().isEmpty())) {
                String nameFilter = empName == null ? "" : empName.trim();
                String deptFilter = dept == null ? "" : dept.trim();
                list.removeIf(r -> {
                    try {
                        empInfo e = empSvc.findById(r.getEmpId());
                        if (e == null) return true;
                        boolean ok = true;
                        if (!nameFilter.isEmpty()) ok = e.getEmpName() != null && e.getEmpName().contains(nameFilter);
                        if (ok && !deptFilter.isEmpty()) ok = e.getDeptName() != null && e.getDeptName().contains(deptFilter);
                        return !ok;
                    } catch (SQLException ex) {
                        return true;
                    }
                });
            }

            // prepare CSV response
            String filename = "salary_export.csv";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);

            try (PrintWriter w = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
                // header
                w.println("emp_no,emp_name,dept_name,salary_month,basic_salary,position_allowance,social_security,tax,actual_salary");
                for (salaryRecord r : list) {
                    empInfo e = null;
                    try {
                        e = empSvc.findById(r.getEmpId());
                    } catch (SQLException ignore) {}
                    String empNo = e == null || e.getEmpNo() == null ? "" : e.getEmpNo();
                    String empNameOut = e == null || e.getEmpName() == null ? "" : e.getEmpName();
                    String deptOut = e == null || e.getDeptName() == null ? "" : e.getDeptName();
                    // escape commas by wrapping in quotes if necessary
                    String line = String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                            csvEscape(empNo), csvEscape(empNameOut), csvEscape(deptOut), r.getSalaryMonth(),
                            r.getBasicSalary() == null ? 0.0 : r.getBasicSalary().doubleValue(),
                            r.getPositionAllowance() == null ? 0.0 : r.getPositionAllowance().doubleValue(),
                            r.getSocialSecurity() == null ? 0.0 : r.getSocialSecurity().doubleValue(),
                            r.getTax() == null ? 0.0 : r.getTax().doubleValue(),
                            r.getActualSalary() == null ? 0.0 : r.getActualSalary().doubleValue());
                    w.println(line);
                }
                w.flush();
            }

            // log export
            try {
                logService.log(userId, "EXPORT_SALARY", request.getRemoteAddr());
            } catch (SQLException ignore) {}

        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("导出失败: " + e.getMessage());
        }
    }

    private String csvEscape(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}


