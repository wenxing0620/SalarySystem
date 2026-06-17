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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SalaryExportServlet", urlPatterns = {"/salary-export-excel"})
public class SalaryExportServlet extends HttpServlet {

    private final SalaryRecordServiceImpl salaryService = new SalaryRecordServiceImpl();
    private final EmpInfoServiceImpl empService = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String dept = req.getParameter("dept");
        String startMonth = req.getParameter("startMonth");
        String endMonth = req.getParameter("endMonth");

        try {
            List<salaryRecord> all = salaryService.findAll();
            List<empInfo> emps = empService.findAll();
            Map<Integer, empInfo> empMap = new HashMap<>();
            for (empInfo e : emps) empMap.put(e.getEmpId(), e);

            // filter (same logic as SalaryListServlet)
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

            // create excel
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("Salaries");
                int rownum = 0;
                Row header = sheet.createRow(rownum++);
                String[] cols = new String[]{"员工编号","员工姓名","部门","岗位","计薪月份","应出勤天数","实际出勤天数","基本工资","岗位津贴","午餐补贴","加班工资","全勤工资","社保","公积金","应扣税","迟到扣款","最终工资"};
                for (int i = 0; i < cols.length; i++) {
                    Cell c = header.createCell(i);
                    c.setCellValue(cols[i]);
                }

                CreationHelper createHelper = wb.getCreationHelper();
                CellStyle moneyStyle = wb.createCellStyle();
                DataFormat df = createHelper.createDataFormat();
                moneyStyle.setDataFormat(df.getFormat("0.00"));

                for (salaryRecord r : all) {
                    Row row = sheet.createRow(rownum++);
                    empInfo e = empMap.get(r.getEmpId());
                    row.createCell(0).setCellValue(e == null ? String.valueOf(r.getEmpId()) : (e.getEmpNo() == null ? String.valueOf(r.getEmpId()) : e.getEmpNo()));
                    row.createCell(1).setCellValue(e == null ? "" : (e.getEmpName() == null ? "" : e.getEmpName()));
                    row.createCell(2).setCellValue(e == null ? "" : (e.getDeptName() == null ? "" : e.getDeptName()));
                    row.createCell(3).setCellValue(e == null ? "" : (e.getPosition() == null ? "" : e.getPosition()));
                    row.createCell(4).setCellValue(r.getSalaryMonth());
                    row.createCell(5).setCellValue(r.getExpectedDays() == null ? 0 : r.getExpectedDays());
                    row.createCell(6).setCellValue(r.getActualDays() == null ? 0 : r.getActualDays());
                    Cell c7 = row.createCell(7); c7.setCellValue(r.getBasicSalary() == null ? 0.0 : r.getBasicSalary().doubleValue()); c7.setCellStyle(moneyStyle);
                    Cell c8 = row.createCell(8); c8.setCellValue(r.getPositionAllowance() == null ? 0.0 : r.getPositionAllowance().doubleValue()); c8.setCellStyle(moneyStyle);
                    Cell c9 = row.createCell(9); c9.setCellValue(r.getLunchAllowance() == null ? 0.0 : r.getLunchAllowance().doubleValue()); c9.setCellStyle(moneyStyle);
                    Cell c10 = row.createCell(10); c10.setCellValue(r.getOvertimeSalary() == null ? 0.0 : r.getOvertimeSalary().doubleValue()); c10.setCellStyle(moneyStyle);
                    Cell c11 = row.createCell(11); c11.setCellValue(r.getFullAttendSalary() == null ? 0.0 : r.getFullAttendSalary().doubleValue()); c11.setCellStyle(moneyStyle);
                    Cell c12 = row.createCell(12); c12.setCellValue(r.getSocialSecurity() == null ? 0.0 : r.getSocialSecurity().doubleValue()); c12.setCellStyle(moneyStyle);
                    Cell c13 = row.createCell(13); c13.setCellValue(r.getProvidentFund() == null ? 0.0 : r.getProvidentFund().doubleValue()); c13.setCellStyle(moneyStyle);
                    Cell c14 = row.createCell(14); c14.setCellValue(r.getTax() == null ? 0.0 : r.getTax().doubleValue()); c14.setCellStyle(moneyStyle);
                    Cell c15 = row.createCell(15); c15.setCellValue(r.getAbsenceDeduction() == null ? 0.0 : r.getAbsenceDeduction().doubleValue()); c15.setCellStyle(moneyStyle);
                    Cell c16 = row.createCell(16); c16.setCellValue(r.getActualSalary() == null ? 0.0 : r.getActualSalary().doubleValue()); c16.setCellStyle(moneyStyle);
                }

                for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

                resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
                resp.setHeader("Content-Disposition", "attachment; filename=salary_export_" + ts + ".xlsx");
                try (OutputStream out = resp.getOutputStream()) {
                    wb.write(out);
                    out.flush();
                }
            }

            // audit
            try { logService.log(null, "EXPORT_SALARY", req.getRemoteAddr()); } catch (SQLException ignored) {}

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("导出失败，请稍后重试", e);
        }
    }
}

