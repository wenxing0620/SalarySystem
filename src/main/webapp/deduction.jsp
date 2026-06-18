<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, com.salarysystem.model.taxDeduction, com.salarysystem.model.empInfo, com.salarysystem.model.PageResult" %>
<%@ page import="com.salarysystem.servlet.DeductionServlet.DeductionViewRow" %>
<%@ page import="java.util.List" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    Boolean editMode = (Boolean) request.getAttribute("editMode");
    Boolean viewMode = (Boolean) request.getAttribute("viewMode");
    taxDeduction deduction = (taxDeduction) request.getAttribute("deduction");
    Integer year = (Integer) request.getAttribute("year");
    boolean showEditModal = Boolean.TRUE.equals(editMode);
    boolean showViewModal = Boolean.TRUE.equals(viewMode);

    @SuppressWarnings("unchecked")
    List<DeductionViewRow> rows = (List<DeductionViewRow>) request.getAttribute("deductionRows");
    if (rows == null) rows = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    List<empInfo> allEmployees = (List<empInfo>) request.getAttribute("allEmployees");
    if (allEmployees == null) allEmployees = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    PageResult<taxDeduction> pageResult = (PageResult<taxDeduction>) request.getAttribute("pageResult");

    String filterEmpId = request.getAttribute("filterEmpId") != null ? request.getAttribute("filterEmpId").toString() : "";
    String filterYear = request.getAttribute("filterYear") != null ? request.getAttribute("filterYear").toString() : "";
    Integer currentYear = request.getAttribute("currentYear") != null ? (Integer) request.getAttribute("currentYear") : java.time.LocalDate.now().getYear();

    String querySuffix = (!filterEmpId.isEmpty() ? "&filterEmpId=" + filterEmpId : "") + (!filterYear.isEmpty() ? "&filterYear=" + filterYear : "");
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>专项附加扣除管理 - 薪资管理系统</title>
    <style>
        .modal-actions { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
        .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; }
        .number { text-align: right; white-space: nowrap; }
        .readonly { padding: 8px; background: #f9f9f9; border: 1px solid #eee; border-radius: 4px; min-height: 30px; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "deduction"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>专项附加扣除管理</h2>
            <button class="btn btn-success" onclick="location.href='<%= request.getContextPath() %>/deduction?action=add'">+ 新建申报</button>
        </div>

        <%@ include file="_alerts.jsp" %>

        <!-- 筛选 -->
        <form class="filter-box" method="get" action="<%= request.getContextPath() %>/deduction">
            <div class="form-group" style="margin-bottom:0;">
                <label>筛选员工</label>
                <select name="filterEmpId" style="width:200px;">
                    <option value="">全部员工</option>
                    <% for (empInfo e : allEmployees) { %>
                    <option value="<%= e.getEmpId() %>" <%= filterEmpId.equals(String.valueOf(e.getEmpId())) ? "selected" : "" %>>
                        <%= e.getEmpNo() %> - <%= e.getEmpName() %>
                    </option>
                    <% } %>
                </select>
            </div>
            <div class="form-group" style="margin-bottom:0;">
                <label>筛选年份</label>
                <input type="number" name="filterYear" min="2000" max="2100" value="<%= filterYear %>" placeholder="全部年份" style="width:120px;">
            </div>
            <button type="submit" class="btn btn-primary">筛选</button>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/deduction">重置</a>
        </form>

        <!-- 列表 -->
        <div class="table-container">
            <h3>申报记录</h3>
            <div style="overflow-x:auto;">
                <table>
                    <thead>
                    <tr>
                        <th style="width:80px;">序号</th>
                        <th>员工编号</th>
                        <th>姓名</th>
                        <th>部门</th>
                        <th>年份</th>
                        <th class="number">子女教育</th>
                        <th class="number">继续教育</th>
                        <th class="number">大病医疗</th>
                        <th class="number">住房贷款</th>
                        <th class="number">住房租金</th>
                        <th class="number">赡养老人</th>
                        <th class="number">婴幼儿照护</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (rows.isEmpty()) { %>
                    <tr><td colspan="13" class="text-center" style="color:#999;padding:20px;">暂无申报记录</td></tr>
                    <% } else {
                        int index = 1; for (DeductionViewRow row : rows) {
                            taxDeduction r = row.getRecord();
                    %>
                    <tr>
                        <td><%= index++ %></td>
                        <td><%= row.getEmpNo() %></td>
                        <td><span class="masked"><%= row.getEmpNameMasked() %></span></td>
                        <td><%= row.getDeptName() %></td>
                        <td><%= r.getDeclareYear() %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getChildEdu() != null ? r.getChildEdu() : java.math.BigDecimal.ZERO) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getContEdu() != null ? r.getContEdu() : java.math.BigDecimal.ZERO) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getMajorMed() != null ? r.getMajorMed() : java.math.BigDecimal.ZERO) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getHousingLoan() != null ? r.getHousingLoan() : java.math.BigDecimal.ZERO) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getHousingRent() != null ? r.getHousingRent() : java.math.BigDecimal.ZERO) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getSupportElderly() != null ? r.getSupportElderly() : java.math.BigDecimal.ZERO) %></td>
                        <td class="number">¥<%= String.format("%.2f", r.getBabyCare() != null ? r.getBabyCare() : java.math.BigDecimal.ZERO) %></td>
                        <td>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/deduction?action=view&id=<%= r.getDeductionId() %>">查看</a>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/deduction?action=edit&id=<%= r.getDeductionId() %>">修改</a>
                            <a class="btn btn-danger btn-sm" href="javascript:void(0)" onclick="deleteDeduction(<%= r.getDeductionId() %>)">删除</a>
                        </td>
                    </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>

            <!-- Pagination -->
            <% if (pageResult != null && pageResult.getTotalPages() > 1) { %>
            <div class="pagination">
                <% if (pageResult.hasPrevPage()) { %>
                <a href="<%= request.getContextPath() %>/deduction?pageNo=1<%= querySuffix %>">首页</a>
                <a href="<%= request.getContextPath() %>/deduction?pageNo=<%= pageResult.getPageNo() - 1 %><%= querySuffix %>">上一页</a>
                <% } else { %><span class="disabled">首页</span><span class="disabled">上一页</span><% } %>
                <span class="current"><%= pageResult.getPageNo() %> / <%= pageResult.getTotalPages() %></span>
                <% if (pageResult.hasNextPage()) { %>
                <a href="<%= request.getContextPath() %>/deduction?pageNo=<%= pageResult.getPageNo() + 1 %><%= querySuffix %>">下一页</a>
                <a href="<%= request.getContextPath() %>/deduction?pageNo=<%= pageResult.getTotalPages() %><%= querySuffix %>">末页</a>
                <% } else { %><span class="disabled">下一页</span><span class="disabled">末页</span><% } %>
                <span>共 <%= pageResult.getTotalPages() %> 页，<%= pageResult.getTotalCount() %> 条</span>
            </div>
            <% } %>
        </div>
    </div>
</div>

<!-- 新增/编辑申报弹窗 -->
<div class="modal-overlay<%= showEditModal ? " show" : "" %>" id="editModal">
    <div class="modal modal-wide">
        <h3><%= (deduction != null && deduction.getDeductionId() != null) ? "修改申报记录" : "新增申报记录" %></h3>
        <form method="post" action="<%= request.getContextPath() %>/deduction">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="deductionId" value="<%= deduction != null && deduction.getDeductionId() != null ? deduction.getDeductionId() : "" %>">
            <div class="form-row">
                <div class="form-group">
                    <label>员工 *</label>
                    <select name="empId" required>
                        <option value="">请选择员工</option>
                        <% for (empInfo e : allEmployees) {
                            String sel = (deduction != null && deduction.getEmpId() != null && deduction.getEmpId().equals(e.getEmpId())) ? "selected" : "";
                        %>
                        <option value="<%= e.getEmpId() %>" <%= sel %>><%= e.getEmpNo() %> - <%= e.getEmpName() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label>申报年份 *</label>
                    <input type="number" name="year" min="2000" max="2100" required value="<%= year != null ? year : currentYear %>">
                    <div class="help-text">同一员工同一年只能有一条申报记录</div>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group"><label>子女教育（元）</label><input type="number" name="childEdu" step="0.01" min="0" value="<%= deduction != null && deduction.getChildEdu() != null ? deduction.getChildEdu() : "0.00" %>"></div>
                <div class="form-group"><label>继续教育（元）</label><input type="number" name="contEdu" step="0.01" min="0" value="<%= deduction != null && deduction.getContEdu() != null ? deduction.getContEdu() : "0.00" %>"></div>
                <div class="form-group"><label>大病医疗（元）</label><input type="number" name="majorMed" step="0.01" min="0" value="<%= deduction != null && deduction.getMajorMed() != null ? deduction.getMajorMed() : "0.00" %>"></div>
                <div class="form-group"><label>住房贷款利息（元）</label><input type="number" name="housingLoan" step="0.01" min="0" value="<%= deduction != null && deduction.getHousingLoan() != null ? deduction.getHousingLoan() : "0.00" %>"></div>
                <div class="form-group"><label>住房租金（元）</label><input type="number" name="housingRent" step="0.01" min="0" value="<%= deduction != null && deduction.getHousingRent() != null ? deduction.getHousingRent() : "0.00" %>"></div>
                <div class="form-group"><label>赡养老人（元）</label><input type="number" name="supportElderly" step="0.01" min="0" value="<%= deduction != null && deduction.getSupportElderly() != null ? deduction.getSupportElderly() : "0.00" %>"></div>
                <div class="form-group"><label>婴幼儿照护（元）</label><input type="number" name="babyCare" step="0.01" min="0" value="<%= deduction != null && deduction.getBabyCare() != null ? deduction.getBabyCare() : "0.00" %>"></div>
            </div>
            <div class="modal-actions">
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/deduction">取消</a>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<!-- 查看申报弹窗 -->
<div class="modal-overlay<%= showViewModal ? " show" : "" %>" id="viewModal">
    <div class="modal modal-wide">
        <h3>查看申报记录</h3>
        <div class="form-row">
            <div class="form-group"><label>员工ID</label><div class="readonly"><%= deduction != null ? deduction.getEmpId() : "-" %></div></div>
            <div class="form-group"><label>申报年份</label><div class="readonly"><%= year != null ? year : "-" %></div></div>
            <div class="form-group"><label>子女教育</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getChildEdu() != null ? deduction.getChildEdu() : java.math.BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>继续教育</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getContEdu() != null ? deduction.getContEdu() : java.math.BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>大病医疗</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getMajorMed() != null ? deduction.getMajorMed() : java.math.BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>住房贷款利息</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getHousingLoan() != null ? deduction.getHousingLoan() : java.math.BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>住房租金</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getHousingRent() != null ? deduction.getHousingRent() : java.math.BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>赡养老人</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getSupportElderly() != null ? deduction.getSupportElderly() : java.math.BigDecimal.ZERO) %></div></div>
            <div class="form-group"><label>婴幼儿照护</label><div class="readonly">¥<%= String.format("%.2f", deduction != null && deduction.getBabyCare() != null ? deduction.getBabyCare() : java.math.BigDecimal.ZERO) %></div></div>
        </div>
        <div class="modal-actions">
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/deduction">关闭</a>
        </div>
    </div>
</div>

<script>
function deleteDeduction(id) {
    if (!confirm('确认删除该申报记录？')) return;
    submitForm('<%= request.getContextPath() %>/deduction', { action: 'delete', deductionId: id });
}
</script>
</body>
</html>
