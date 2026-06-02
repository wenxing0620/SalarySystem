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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String keyword = request.getParameter("keyword");
        String dept = request.getParameter("dept");
        String startMonth = request.getParameter("startMonth");
        String endMonth = request.getParameter("endMonth");

        try {
            List<salaryRecord> all = svc.findAll();
            List<empInfo> emps = empSvc.findAll();
            Map<Integer, empInfo> empMap = new HashMap<>();
            for (empInfo e : emps) empMap.put(e.getEmpId(), e);

            // Filter using same params as SalaryListServlet
            String kw = keyword != null ? keyword.toLowerCase() : "";
            String d = dept != null ? dept.toLowerCase() : "";
            String sm = startMonth != null ? startMonth.trim() : "";
            String em = endMonth != null ? endMonth.trim() : "";

            all.removeIf(r -> {
                empInfo e = empMap.get(r.getEmpId());
                if (!sm.isEmpty() && r.getSalaryMonth() != null && r.getSalaryMonth().compareTo(sm) < 0) return true;
                if (!em.isEmpty() && r.getSalaryMonth() != null && r.getSalaryMonth().compareTo(em) > 0) return true;
                if (!d.isEmpty() && (e == null || e.getDeptName() == null || !e.getDeptName().toLowerCase().contains(d))) return true;
                if (!kw.isEmpty()) {
                    if (e == null) return true;
                    boolean matchNo = e.getEmpNo() != null && e.getEmpNo().toLowerCase().contains(kw);
                    boolean matchName = e.getEmpName() != null && e.getEmpName().toLowerCase().contains(kw);
                    if (!matchNo && !matchName) return true;
                }
                return false;
            });

            // CSV output
            String filename = "salary_export.csv";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);

            try (PrintWriter w = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
                w.println("emp_no,emp_name,dept_name,salary_month,basic_salary,position_allowance,lunch_allowance,overtime_salary,full_attend_salary,social_security,provident_fund,tax,absence_deduction,actual_salary");
                for (salaryRecord r : all) {
                    empInfo e = empMap.get(r.getEmpId());
                    String empNo = e == null || e.getEmpNo() == null ? "" : csvEscape(e.getEmpNo());
                    String empNameOut = e == null || e.getEmpName() == null ? "" : csvEscape(e.getEmpName());
                    String deptOut = e == null || e.getDeptName() == null ? "" : csvEscape(e.getDeptName());
                    w.println(String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                            empNo, empNameOut, deptOut, r.getSalaryMonth(),
                            r.getBasicSalary() == null ? 0.0 : r.getBasicSalary().doubleValue(),
                            r.getPositionAllowance() == null ? 0.0 : r.getPositionAllowance().doubleValue(),
                            r.getLunchAllowance() == null ? 0.0 : r.getLunchAllowance().doubleValue(),
                            r.getOvertimeSalary() == null ? 0.0 : r.getOvertimeSalary().doubleValue(),
                            r.getFullAttendSalary() == null ? 0.0 : r.getFullAttendSalary().doubleValue(),
                            r.getSocialSecurity() == null ? 0.0 : r.getSocialSecurity().doubleValue(),
                            r.getProvidentFund() == null ? 0.0 : r.getProvidentFund().doubleValue(),
                            r.getTax() == null ? 0.0 : r.getTax().doubleValue(),
                            r.getAbsenceDeduction() == null ? 0.0 : r.getAbsenceDeduction().doubleValue(),
                            r.getActualSalary() == null ? 0.0 : r.getActualSalary().doubleValue()));
                }
                w.flush();
            }

            try { logService.log(userId, "EXPORT_SALARY", request.getRemoteAddr()); } catch (SQLException ignore) {}

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
