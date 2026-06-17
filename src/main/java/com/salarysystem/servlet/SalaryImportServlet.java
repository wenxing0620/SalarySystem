package com.salarysystem.servlet;

import com.salarysystem.model.empInfo;
import com.salarysystem.model.salaryRecord;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.service.impl.SalaryRecordServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@WebServlet("/salary-import-excel")
@MultipartConfig
public class SalaryImportServlet extends HttpServlet {

    private final SalaryRecordServiceImpl salaryService = new SalaryRecordServiceImpl();
    private final EmpInfoServiceImpl empService = new EmpInfoServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser user = (sysUser) session.getAttribute("currentUser");

        // 总经理只能查看，不能导入
        if ("总经理".equals(session.getAttribute("currentUserRole"))) {
            session.setAttribute("message", "权限不足：总经理只能查看，不能导入薪资");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
            return;
        }

        Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() <= 0) {
            session.setAttribute("message", "请选择要导入的 Excel 文件");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
            return;
        }

        String submittedName = filePart.getSubmittedFileName();
        if (submittedName == null || (!submittedName.toLowerCase(Locale.ROOT).endsWith(".xls") && !submittedName.toLowerCase(Locale.ROOT).endsWith(".xlsx"))) {
            session.setAttribute("message", "仅支持 .xls / .xlsx 格式文件");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
            return;
        }

        int imported = 0;
        int updated = 0;
        int skipped = 0;
        int emptyRows = 0;

        try (InputStream in = filePart.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
            if (wb.getNumberOfSheets() == 0) {
                session.setAttribute("message", "导入失败：Excel 中没有工作表");
                resp.sendRedirect(req.getContextPath() + "/salary-list");
                return;
            }

            DataFormatter formatter = new DataFormatter();
            Row headerRow = wb.getSheetAt(0).getRow(0);
            if (headerRow == null) {
                session.setAttribute("message", "导入失败：缺少表头行");
                resp.sendRedirect(req.getContextPath() + "/salary-list");
                return;
            }

            Map<String, Integer> headerMap = buildHeaderMap(headerRow, formatter);
            Map<String, Integer> aliasMap = buildAliasMap(headerMap);

            Map<String, empInfo> empCache = new HashMap<>();
            for (empInfo e : empService.findAll()) {
                if (e.getEmpNo() != null) empCache.put(e.getEmpNo().trim(), e);
            }

            for (int i = 1; i <= wb.getSheetAt(0).getLastRowNum(); i++) {
                Row row = wb.getSheetAt(0).getRow(i);
                if (row == null) { emptyRows++; continue; }

                String empIdText = cellText(row, aliasMap.get("emp_id"), formatter);
                String empNoText = cellText(row, aliasMap.get("emp_no"), formatter);
                String salaryMonth = normalizeMonth(cellText(row, aliasMap.get("salary_month"), formatter));

                Integer empId = null;
                if (!empIdText.isEmpty()) {
                    try {
                        empId = Integer.parseInt(empIdText);
                    } catch (Exception ignore) {
                        // If the cell under “员工ID” actually stores the employee number (e.g. EMP001),
                        // fall back to matching by empNo.
                        empInfo emp = empCache.get(empIdText.trim());
                        if (emp != null) {
                            empId = emp.getEmpId();
                        }
                    }
                }
                if (empId == null && !empNoText.isEmpty()) {
                    empInfo emp = empCache.get(empNoText.trim());
                    if (emp != null) empId = emp.getEmpId();
                }
                if (empId == null || salaryMonth.isEmpty()) {
                    skipped++;
                    continue;
                }

                empInfo emp = empService.findById(empId);
                if (emp == null) {
                    skipped++;
                    continue;
                }

                salaryRecord record = new salaryRecord();
                record.setEmpId(empId);
                record.setSalaryMonth(salaryMonth);
                record.setExpectedDays(parseInt(cellText(row, aliasMap.get("expected_days"), formatter)));
                record.setActualDays(parseInt(cellText(row, aliasMap.get("actual_days"), formatter)));
                record.setBasicSalary(parseDecimal(cellText(row, aliasMap.get("basic_salary"), formatter)));
                record.setPositionAllowance(parseDecimal(cellText(row, aliasMap.get("position_allowance"), formatter)));
                record.setLunchAllowance(parseDecimal(cellText(row, aliasMap.get("lunch_allowance"), formatter)));
                record.setOvertimeSalary(parseDecimal(cellText(row, aliasMap.get("overtime_salary"), formatter)));
                record.setFullAttendSalary(parseDecimal(cellText(row, aliasMap.get("full_attend_salary"), formatter)));
                record.setSocialSecurity(parseDecimal(cellText(row, aliasMap.get("social_security"), formatter)));
                record.setProvidentFund(parseDecimal(cellText(row, aliasMap.get("provident_fund"), formatter)));
                record.setTax(parseDecimal(cellText(row, aliasMap.get("tax"), formatter)));
                record.setAbsenceDeduction(parseDecimal(cellText(row, aliasMap.get("absence_deduction"), formatter)));
                record.setActualSalary(parseDecimal(cellText(row, aliasMap.get("actual_salary"), formatter)));

                try {
                    salaryRecord exists = salaryService.findByEmpIdAndMonth(empId, salaryMonth);
                    if (exists != null) {
                        record.setRecordId(exists.getRecordId());
                        salaryService.update(record);
                        updated++;
                    } else {
                        salaryService.add(record);
                        imported++;
                    }
                } catch (SQLException ex) {
                    skipped++;
                }
            }

            try {
                logService.log(user.getUserId(), "IMPORT_SALARY", req.getRemoteAddr());
            } catch (SQLException ignore) {}

            session.setAttribute("message", String.format("导入完成：新增 %d 条，更新 %d 条，跳过 %d 条，空行 %d 行", imported, updated, skipped, emptyRows));
            resp.sendRedirect(req.getContextPath() + "/salary-list");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "导入失败，请稍后重试");
            resp.sendRedirect(req.getContextPath() + "/salary-list");
        }
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String key = normalize(cellText(headerRow, i, formatter));
            if (!key.isEmpty()) {
                map.put(key, i);
            }
        }
        return map;
    }

    private Map<String, Integer> buildAliasMap(Map<String, Integer> headerMap) {
        Map<String, Integer> aliases = new HashMap<>();
        putIfPresent(aliases, headerMap, "emp_id", "emp_id", "员工ID", "员工编号", "员工id", "员工ID");
        putIfPresent(aliases, headerMap, "emp_no", "emp_no", "员工编号", "工号", "员工ID");
        putIfPresent(aliases, headerMap, "salary_month", "salary_month", "计薪月份", "月份", "薪资月份", "计薪月份");
        putIfPresent(aliases, headerMap, "expected_days", "expected_days", "应出勤天数", "应出勤", "本月应出勤天数", "应出勤天数");
        putIfPresent(aliases, headerMap, "actual_days", "actual_days", "实际出勤天数", "实际出勤", "实出勤", "实出勤天数", "出勤天数", "实际出勤状况", "出勤状况");
        putIfPresent(aliases, headerMap, "basic_salary", "basic_salary", "基本工资");
        putIfPresent(aliases, headerMap, "position_allowance", "position_allowance", "岗位津贴");
        putIfPresent(aliases, headerMap, "lunch_allowance", "lunch_allowance", "午餐补贴");
        putIfPresent(aliases, headerMap, "overtime_salary", "overtime_salary", "加班工资");
        putIfPresent(aliases, headerMap, "full_attend_salary", "full_attend_salary", "全勤工资");
        putIfPresent(aliases, headerMap, "social_security", "social_security", "社保", "扣社保");
        putIfPresent(aliases, headerMap, "provident_fund", "provident_fund", "公积金", "扣公积金");
        putIfPresent(aliases, headerMap, "tax", "tax", "应扣税", "个税");
        putIfPresent(aliases, headerMap, "absence_deduction", "absence_deduction", "迟到扣款", "缺勤扣款");
        putIfPresent(aliases, headerMap, "actual_salary", "actual_salary", "最终工资", "实发工资");
        return aliases;
    }

    private void putIfPresent(Map<String, Integer> target, Map<String, Integer> headerMap, String key, String... aliases) {
        for (String alias : aliases) {
            Integer idx = headerMap.get(normalize(alias));
            if (idx != null) {
                target.put(key, idx);
                return;
            }
        }
    }

    private String cellText(Row row, Integer index, DataFormatter formatter) {
        if (row == null || index == null) return "";
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT)
                .replace("（", "(")
                .replace("）", ")")
                .replace(" ", "")
                .replace("-", "_")
                .replace("/", "_");
    }

    private String normalizeMonth(String value) {
        if (value == null) return "";
        String v = value.trim();
        if (v.isEmpty()) return "";
        if (v.matches("^\\d{4}-\\d{1,2}$")) {
            String[] parts = v.split("-");
            return parts[0] + "-" + String.format("%02d", Integer.parseInt(parts[1]));
        }
        return v;
    }

    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return 0; }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return BigDecimal.ZERO;
        try { return new BigDecimal(value.trim()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}


