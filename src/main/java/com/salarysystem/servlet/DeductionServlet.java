package com.salarysystem.servlet;

import com.salarysystem.model.empInfo;
import com.salarysystem.model.sysUser;
import com.salarysystem.model.taxDeduction;
import com.salarysystem.service.impl.EmpInfoServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import com.salarysystem.service.impl.TaxDeductionServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/deduction")
public class DeductionServlet extends HttpServlet {

	private final TaxDeductionServiceImpl deductionService = new TaxDeductionServiceImpl();
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
				req.setAttribute("allEmployees", empService.findAll());
				req.setAttribute("year", LocalDateTime.now(ZoneId.of("Asia/Shanghai")).getYear());
			} else if ("edit".equals(action) || "view".equals(action)) {
				Integer deductionId = parseInt(req.getParameter("id"));
				if (deductionId == null) {
					session.setAttribute("message", "缺少记录ID");
					resp.sendRedirect(req.getContextPath() + "/deduction");
					return;
				}

				taxDeduction record = deductionService.findById(deductionId);
				if (record == null) {
					session.setAttribute("message", "申报记录不存在");
					resp.sendRedirect(req.getContextPath() + "/deduction");
					return;
				}

				req.setAttribute("deduction", record);
				req.setAttribute("year", record.getDeclareYear());
				req.setAttribute("allEmployees", empService.findAll());
				if ("edit".equals(action)) {
					req.setAttribute("editMode", true);
				} else {
					req.setAttribute("viewMode", true);
				}
			}

			prepareListData(req);
			logService.log(user.getUserId(), "QUERY_DEDUCTION", req.getRemoteAddr());
			req.getRequestDispatcher("/deduction.jsp").forward(req, resp);
		} catch (SQLException e) {
			throw new ServletException(e);
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
		String action = safe(req.getParameter("action"));

		try {
			if ("delete".equals(action)) {
				Integer deductionId = parseInt(req.getParameter("deductionId"));
				if (deductionId != null) {
					deductionService.delete(deductionId);
					logService.log(user.getUserId(), "DELETE_DEDUCTION", req.getRemoteAddr());
					session.setAttribute("message", "申报记录删除成功");
				} else {
					session.setAttribute("message", "删除失败：缺少记录ID");
				}
				resp.sendRedirect(req.getContextPath() + "/deduction");
				return;
			}

			if (!"save".equals(action)) {
				resp.sendRedirect(req.getContextPath() + "/deduction");
				return;
			}

			Integer deductionId = parseInt(req.getParameter("deductionId"));
			Integer empId = parseInt(req.getParameter("empId"));
			Integer year = parseInt(req.getParameter("year"));

			if (empId == null || year == null) {
				session.setAttribute("message", "保存失败：员工和年份不能为空");
				resp.sendRedirect(req.getContextPath() + "/deduction");
				return;
			}

			empInfo emp = empService.findById(empId);
			if (emp == null) {
				session.setAttribute("message", "保存失败：员工不存在");
				resp.sendRedirect(req.getContextPath() + "/deduction");
				return;
			}

			// Enforce one record per employee per year.
			taxDeduction existing = deductionService.findByEmpIdAndYear(empId, year);
			if (existing != null && (deductionId == null || !existing.getDeductionId().equals(deductionId))) {
				session.setAttribute("message", "保存失败：该员工在该年份已存在申报记录");
				resp.sendRedirect(req.getContextPath() + "/deduction");
				return;
			}

			taxDeduction record = new taxDeduction();
			record.setDeductionId(deductionId);
			record.setEmpId(empId);
			record.setDeclareYear(year);
			record.setChildEdu(parseDecimal(req.getParameter("childEdu")));
			record.setContEdu(parseDecimal(req.getParameter("contEdu")));
			record.setMajorMed(parseDecimal(req.getParameter("majorMed")));
			record.setHousingLoan(parseDecimal(req.getParameter("housingLoan")));
			record.setHousingRent(parseDecimal(req.getParameter("housingRent")));
			record.setSupportElderly(parseDecimal(req.getParameter("supportElderly")));
			record.setBabyCare(parseDecimal(req.getParameter("babyCare")));

			deductionService.addOrUpdate(record);
			logService.log(user.getUserId(), deductionId == null ? "ADD_DEDUCTION" : "UPDATE_DEDUCTION", req.getRemoteAddr());
			session.setAttribute("message", deductionId == null ? "申报记录新增成功" : "申报记录更新成功");
			resp.sendRedirect(req.getContextPath() + "/deduction");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	private void prepareListData(HttpServletRequest req) throws SQLException {
		List<taxDeduction> records = deductionService.findAll();
		List<empInfo> allEmployees = empService.findAll();
		Map<Integer, empInfo> empMap = new HashMap<>();
		for (empInfo emp : allEmployees) {
			empMap.put(emp.getEmpId(), emp);
		}

		List<DeductionViewRow> rows = new java.util.ArrayList<>();
		for (taxDeduction record : records) {
			empInfo emp = empMap.get(record.getEmpId());
			rows.add(new DeductionViewRow(record, emp));
		}

		req.setAttribute("deductionRows", rows);
		req.setAttribute("currentYear", LocalDateTime.now(ZoneId.of("Asia/Shanghai")).getYear());
		if (req.getAttribute("allEmployees") == null) {
			req.setAttribute("allEmployees", allEmployees);
		}
	}

	private Integer parseInt(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseDecimal(String value) {
		if (value == null || value.trim().isEmpty()) {
			return BigDecimal.ZERO;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	private String safe(String value) {
		return value == null ? "" : value.trim();
	}

	public static class DeductionViewRow {
		private final taxDeduction record;
		private final empInfo emp;

		public DeductionViewRow(taxDeduction record, empInfo emp) {
			this.record = record;
			this.emp = emp;
		}

		public taxDeduction getRecord() {
			return record;
		}

		public String getEmpNo() {
			return emp == null ? "-" : nullToDash(emp.getEmpNo());
		}

		public String getEmpNameMasked() {
			return maskName(emp == null ? null : emp.getEmpName());
		}

		public String getDeptName() {
			return emp == null ? "-" : nullToDash(emp.getDeptName());
		}

		public String getPosition() {
			return emp == null ? "-" : nullToDash(emp.getPosition());
		}

		public String getPhoneMasked() {
			return maskPhone(emp == null ? null : emp.getPhone());
		}

		public String getIdCardMasked() {
			return maskIdCard(emp == null ? null : emp.getIdCard());
		}

		private static String nullToDash(String value) {
			return (value == null || value.trim().isEmpty()) ? "-" : value;
		}

		private static String maskName(String name) {
			if (name == null || name.isEmpty()) {
				return "-";
			}
			if (name.length() == 1) {
				return "*";
			}
			return name.charAt(0) + "*";
		}

		private static String maskPhone(String phone) {
			if (phone == null || phone.length() < 7) {
				return "-";
			}
			return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
		}

		private static String maskIdCard(String idCard) {
			if (idCard == null || idCard.length() < 8) {
				return "-";
			}
			return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
		}
	}

}

