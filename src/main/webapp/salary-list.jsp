<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, com.salarysystem.model.salaryRecord, com.salarysystem.model.empInfo" %>
<%@ page import="com.salarysystem.servlet.SalaryListServlet.SalaryRow" %>
<%@ page import="com.salarysystem.util.DesensitizeUtil, java.util.List, java.math.BigDecimal" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    Boolean editMode = (Boolean) request.getAttribute("editMode");
    Boolean viewMode = (Boolean) request.getAttribute("viewMode");
    salaryRecord record = (salaryRecord) request.getAttribute("salaryRecord");
    boolean showEditModal = Boolean.TRUE.equals(editMode);
    boolean showViewModal = Boolean.TRUE.equals(viewMode);

    @SuppressWarnings("unchecked")
    List<SalaryRow> rows = (List<SalaryRow>) request.getAttribute("salaryRows");
    if (rows == null) rows = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    List<empInfo> employees = (List<empInfo>) request.getAttribute("employees");
    if (employees == null) employees = new java.util.ArrayList<>();

    String keyword = request.getAttribute("keyword") == null ? "" : request.getAttribute("keyword").toString();
    String dept = request.getAttribute("dept") == null ? "" : request.getAttribute("dept").toString();
    String startMonth = request.getAttribute("startMonth") == null ? "" : request.getAttribute("startMonth").toString();
    String endMonth = request.getAttribute("endMonth") == null ? "" : request.getAttribute("endMonth").toString();
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>薪资管理 - 薪资管理系统</title>
    <style>
        .modal-actions { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
        .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; }
        .number { text-align: right; white-space: nowrap; }
        .readonly { padding: 8px; background: #f9f9f9; border: 1px solid #eee; border-radius: 4px; min-height: 36px; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "salary-list"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>薪资管理</h2>
            <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/salary-list?action=add">+ 新增薪资</a>
                <a class="btn btn-success" href="<%= request.getContextPath() %>/salary-export-excel?keyword=<%= java.net.URLEncoder.encode(keyword, "UTF-8") %>&dept=<%= java.net.URLEncoder.encode(dept, "UTF-8") %>&startMonth=<%= java.net.URLEncoder.encode(startMonth, "UTF-8") %>&endMonth=<%= java.net.URLEncoder.encode(endMonth, "UTF-8") %>">导出Excel</a>
                <a class="btn btn-success" href="javascript:void(0)" onclick="document.getElementById('importFile').click();">导入Excel</a>
                <span style="display:inline-flex;align-items:center;gap:6px;">
                    <input type="text" id="calcTaxMonth" placeholder="YYYY-MM" value="<%= startMonth %>" style="width:100px;padding:7px 8px;border:1px solid #ddd;border-radius:4px;font-size:12px;">
                    <a class="btn btn-warning" href="javascript:void(0)" onclick="calculateTax()" title="一键计算所有员工当月个税和实发工资">⚡ 一键计税</a>
                </span>
            </div>
        </div>

        <%@ include file="_alerts.jsp" %>

        <!-- 筛选 -->
        <form class="filter-box" method="get" action="<%= request.getContextPath() %>/salary-list">
            <input type="text" name="startMonth" placeholder="起始月份 YYYY-MM" value="<%= startMonth %>" style="width:130px;">
            <span style="color:#999;">至</span>
            <input type="text" name="endMonth" placeholder="截止月份 YYYY-MM" value="<%= endMonth %>" style="width:130px;">
            <input type="text" name="keyword" placeholder="员工编号/姓名" value="<%= keyword %>">
            <input type="text" name="dept" placeholder="部门" value="<%= dept %>">
            <button type="submit" class="btn btn-primary">查询</button>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/salary-list">重置</a>
        </form>

        <form id="importForm" method="post" action="<%= request.getContextPath() %>/salary-import-excel" enctype="multipart/form-data" style="display:none;">
            <input type="file" id="importFile" name="file" accept=".xls,.xlsx" onchange="document.getElementById('importForm').submit();">
        </form>

        <!-- 列表 -->
        <div class="table-container">
            <h3>薪资记录</h3>
            <div style="overflow-x:auto;">
                <table>
                    <thead>
                    <tr>
                        <th>流水ID</th>
                        <th>员工编号</th>
                        <th>员工姓名</th>
                        <th>部门</th>
                        <th>计薪月份</th>
                        <th class="number">应出勤</th>
                        <th class="number">实出勤</th>
                        <th class="number">基本工资</th>
                        <th class="number">岗位津贴</th>
                        <th class="number">午餐补贴</th>
                        <th class="number">加班工资</th>
                        <th class="number">全勤工资</th>
                        <th class="number">社保</th>
                        <th class="number">公积金</th>
                        <th class="number">应扣税</th>
                        <th class="number">迟到扣款</th>
                        <th class="number">最终工资</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (rows.isEmpty()) { %>
                    <tr><td colspan="18" class="text-center" style="color:#999;padding:20px;">暂无数据</td></tr>
                    <% } else { for (SalaryRow row : rows) { salaryRecord r = row.getRecord(); %>
                    <tr>
                        <td><%= r.getRecordId() %></td>
                        <td><%= row.getEmpNo() %></td>
                        <td><%= DesensitizeUtil.maskName(row.getEmpName()) %></td>
                        <td><%= row.getDeptName() %></td>
                        <td><%= r.getSalaryMonth() %></td>
                        <td class="number"><%= r.getExpectedDays() %></td>
                        <td class="number"><%= r.getActualDays() %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getBasicSalary() == null ? BigDecimal.ZERO : r.getBasicSalary()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getPositionAllowance() == null ? BigDecimal.ZERO : r.getPositionAllowance()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getLunchAllowance() == null ? BigDecimal.ZERO : r.getLunchAllowance()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getOvertimeSalary() == null ? BigDecimal.ZERO : r.getOvertimeSalary()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getFullAttendSalary() == null ? BigDecimal.ZERO : r.getFullAttendSalary()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getSocialSecurity() == null ? BigDecimal.ZERO : r.getSocialSecurity()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getProvidentFund() == null ? BigDecimal.ZERO : r.getProvidentFund()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getTax() == null ? BigDecimal.ZERO : r.getTax()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getAbsenceDeduction() == null ? BigDecimal.ZERO : r.getAbsenceDeduction()) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getActualSalary() == null ? BigDecimal.ZERO : r.getActualSalary()) %></td>
                        <td>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/salary-list?action=view&id=<%= r.getRecordId() %>">查看</a>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/salary-list?action=edit&id=<%= r.getRecordId() %>">修改</a>
                            <form method="post" action="<%= request.getContextPath() %>/salary-list" onsubmit="return confirm('确认删除该薪资记录？');" style="display:inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="recordId" value="<%= r.getRecordId() %>">
                                <button type="submit" class="btn btn-danger btn-sm">删除</button>
                            </form>
                        </td>
                    </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- 新增/编辑薪资弹窗 -->
<div class="modal-overlay<%= showEditModal ? " show" : "" %>" id="editModal">
    <div class="modal modal-wide" style="width:750px;">
        <h3><%= (record != null && record.getRecordId() != null) ? "修改薪资记录" : "新增薪资记录" %></h3>
        <form method="post" action="<%= request.getContextPath() %>/salary-list">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="recordId" value="<%= record != null && record.getRecordId() != null ? record.getRecordId() : "" %>">
            <div class="form-row">
                <div class="form-group">
                    <label>员工 *</label>
                    <select name="empId" required>
                        <option value="">请选择员工</option>
                        <% for (empInfo e : employees) {
                            String sel = (record != null && record.getEmpId() != null && record.getEmpId().equals(e.getEmpId())) ? "selected" : "";
                        %>
                        <option value="<%= e.getEmpId() %>" <%= sel %>><%= e.getEmpNo() %> - <%= e.getEmpName() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group"><label>计薪月份 *</label><input type="text" name="salaryMonth" placeholder="YYYY-MM" required value="<%= record != null && record.getSalaryMonth()!=null ? record.getSalaryMonth() : "" %>"></div>
                <div class="form-group"><label>应出勤天数</label><input type="number" name="expectedDays" value="<%= (record != null && record.getExpectedDays() != null) ? record.getExpectedDays() : 0 %>"></div>
                <div class="form-group"><label>实际出勤天数</label><input type="number" name="actualDays" value="<%= (record != null && record.getActualDays() != null) ? record.getActualDays() : 0 %>"></div>
                <div class="form-group"><label>基本工资</label><input type="number" step="0.01" name="basicSalary" value="<%= record != null && record.getBasicSalary()!=null ? record.getBasicSalary() : "0.00" %>"></div>
                <div class="form-group"><label>岗位津贴</label><input type="number" step="0.01" name="positionAllowance" value="<%= record != null && record.getPositionAllowance()!=null ? record.getPositionAllowance() : "0.00" %>"></div>
                <div class="form-group"><label>午餐补贴</label><input type="number" step="0.01" name="lunchAllowance" value="<%= record != null && record.getLunchAllowance()!=null ? record.getLunchAllowance() : "0.00" %>"></div>
                <div class="form-group"><label>加班工资</label><input type="number" step="0.01" name="overtimeSalary" value="<%= record != null && record.getOvertimeSalary()!=null ? record.getOvertimeSalary() : "0.00" %>"></div>
                <div class="form-group"><label>全勤工资</label><input type="number" step="0.01" name="fullAttendSalary" value="<%= record != null && record.getFullAttendSalary()!=null ? record.getFullAttendSalary() : "0.00" %>"></div>
                <div class="form-group"><label>社保</label><input type="number" step="0.01" name="socialSecurity" value="<%= record != null && record.getSocialSecurity()!=null ? record.getSocialSecurity() : "0.00" %>"></div>
                <div class="form-group"><label>公积金</label><input type="number" step="0.01" name="providentFund" value="<%= record != null && record.getProvidentFund()!=null ? record.getProvidentFund() : "0.00" %>"></div>
                <div class="form-group"><label>应扣税</label><input type="number" step="0.01" name="tax" value="<%= record != null && record.getTax()!=null ? record.getTax() : "0.00" %>"></div>
                <div class="form-group"><label>迟到扣款</label><input type="number" step="0.01" name="absenceDeduction" value="<%= record != null && record.getAbsenceDeduction()!=null ? record.getAbsenceDeduction() : "0.00" %>"></div>
                <div class="form-group"><label>最终工资</label><input type="number" step="0.01" name="actualSalary" value="<%= record != null && record.getActualSalary()!=null ? record.getActualSalary() : "0.00" %>"></div>
            </div>
            <div class="modal-actions">
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/salary-list">取消</a>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<!-- 查看薪资弹窗 -->
<div class="modal-overlay<%= showViewModal ? " show" : "" %>" id="viewModal">
    <div class="modal modal-wide" style="width:700px;">
        <h3>查看薪资记录</h3>
        <div class="form-row">
            <div class="form-group"><label>员工编号</label><div class="readonly"><%= (record != null && record.getEmpId() != null) ? record.getEmpId() : "-" %></div></div>
            <div class="form-group"><label>计薪月份</label><div class="readonly"><%= record != null ? record.getSalaryMonth() : "-" %></div></div>
            <div class="form-group"><label>应出勤天数</label><div class="readonly"><%= (record != null && record.getExpectedDays() != null) ? record.getExpectedDays() : 0 %></div></div>
            <div class="form-group"><label>实际出勤天数</label><div class="readonly"><%= (record != null && record.getActualDays() != null) ? record.getActualDays() : 0 %></div></div>
            <div class="form-group"><label>基本工资</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getBasicSalary()!=null ? record.getBasicSalary() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>岗位津贴</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getPositionAllowance()!=null ? record.getPositionAllowance() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>午餐补贴</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getLunchAllowance()!=null ? record.getLunchAllowance() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>加班工资</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getOvertimeSalary()!=null ? record.getOvertimeSalary() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>全勤工资</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getFullAttendSalary()!=null ? record.getFullAttendSalary() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>社保</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getSocialSecurity()!=null ? record.getSocialSecurity() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>公积金</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getProvidentFund()!=null ? record.getProvidentFund() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>应扣税</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getTax()!=null ? record.getTax() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>迟到扣款</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getAbsenceDeduction()!=null ? record.getAbsenceDeduction() : BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>最终工资</label><div class="readonly">¥<%= String.format("%.2f", record != null && record.getActualSalary()!=null ? record.getActualSalary() : BigDecimal.ZERO) %></div></div>
        </div>
        <div class="modal-actions">
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/salary-list">关闭</a>
        </div>
    </div>
</div>

<script>
function calculateTax() {
    var month = document.getElementById('calcTaxMonth').value.trim();
    if (!month) { alert('请输入计税月份（格式：YYYY-MM）'); return; }
    if (!/^\d{4}-\d{2}$/.test(month)) { alert('月份格式不正确，请使用 YYYY-MM 格式'); return; }
    if (!confirm('确认要对 ' + month + ' 所有员工执行一键计税？\n\n系统将根据累计预扣法自动计算个税和实发工资。')) return;
    var form = document.createElement('form');
    form.method = 'post';
    form.action = '<%= request.getContextPath() %>/salary-calculate-tax';
    var input = document.createElement('input');
    input.type = 'hidden'; input.name = 'month'; input.value = month;
    form.appendChild(input);
    document.body.appendChild(form);
    form.submit();
}
</script>
</body>
</html>
