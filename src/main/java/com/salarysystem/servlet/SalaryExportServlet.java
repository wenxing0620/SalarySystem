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
        String month = req.getParameter("month");
        String empName = req.getParameter("empName");
        String dept = req.getParameter("dept");

        try {
            List<salaryRecord> all = salaryService.findAll();
            List<empInfo> emps = empService.findAll();
            Map<Integer, empInfo> empMap = new HashMap<>();
            for (empInfo e : emps) empMap.put(e.getEmpId(), e);

            // filter
            all.removeIf(r -> {
                empInfo e = empMap.get(r.getEmpId());
                if (month != null && !month.isEmpty() && (r.getSalaryMonth() == null || !r.getSalaryMonth().equals(month))) return true;
                if (empName != null && !empName.isEmpty()) {
                    if (e == null || e.getEmpName() == null || !e.getEmpName().toLowerCase().contains(empName.toLowerCase())) return true;
                }
                if (dept != null && !dept.isEmpty()) {
                    if (e == null || e.getDeptName() == null || !e.getDeptName().toLowerCase().contains(dept.toLowerCase())) return true;
                }
                return false;
            });

            // create excel
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("Salaries");
                int rownum = 0;
                Row header = sheet.createRow(rownum++);
                String[] cols = new String[]{"员工编号","员工姓名","部门","计薪月份","基本工资","岗位津贴","午餐补贴","加班工资","全勤工资","社保","公积金","个税","迟到扣款","实发工资"};
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
                    row.createCell(3).setCellValue(r.getSalaryMonth());
                    Cell c4 = row.createCell(4); c4.setCellValue(r.getBasicSalary().doubleValue()); c4.setCellStyle(moneyStyle);
                    Cell c5 = row.createCell(5); c5.setCellValue(r.getPositionAllowance().doubleValue()); c5.setCellStyle(moneyStyle);
                    Cell c6 = row.createCell(6); c6.setCellValue(r.getLunchAllowance().doubleValue()); c6.setCellStyle(moneyStyle);
                    Cell c7 = row.createCell(7); c7.setCellValue(r.getOvertimeSalary().doubleValue()); c7.setCellStyle(moneyStyle);
                    Cell c8 = row.createCell(8); c8.setCellValue(r.getFullAttendSalary().doubleValue()); c8.setCellStyle(moneyStyle);
                    Cell c9 = row.createCell(9); c9.setCellValue(r.getSocialSecurity().doubleValue()); c9.setCellStyle(moneyStyle);
                    Cell c10 = row.createCell(10); c10.setCellValue(r.getProvidentFund().doubleValue()); c10.setCellStyle(moneyStyle);
                    Cell c11 = row.createCell(11); c11.setCellValue(r.getTax().doubleValue()); c11.setCellStyle(moneyStyle);
                    Cell c12 = row.createCell(12); c12.setCellValue(r.getAbsenceDeduction().doubleValue()); c12.setCellStyle(moneyStyle);
                    Cell c13 = row.createCell(13); c13.setCellValue(r.getActualSalary().doubleValue()); c13.setCellStyle(moneyStyle);
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
            throw new ServletException(e);
        }
    }
}

