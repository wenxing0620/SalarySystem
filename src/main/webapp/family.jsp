<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, com.salarysystem.model.empFamily, com.salarysystem.model.empInfo" %>
<%@ page import="com.salarysystem.servlet.FamilyServlet.FamilyViewRow" %>
<%@ page import="com.salarysystem.util.DesensitizeUtil, java.util.List" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    empFamily editFamily = (empFamily) request.getAttribute("family");
    boolean editModalOpen = editFamily != null && editFamily.getFamilyId() != null;

    @SuppressWarnings("unchecked")
    List<FamilyViewRow> rows = (List<FamilyViewRow>) request.getAttribute("familyRows");
    if (rows == null) rows = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    List<empInfo> allEmployees = (List<empInfo>) request.getAttribute("allEmployees");
    if (allEmployees == null) allEmployees = new java.util.ArrayList<>();

    String keyword = request.getAttribute("keyword") != null ? request.getAttribute("keyword").toString() : "";
    String filterEmpId = request.getAttribute("filterEmpId") != null ? request.getAttribute("filterEmpId").toString() : "";

    String[] relations = {"父亲", "母亲", "儿子", "女儿", "配偶", "祖父", "祖母", "外祖父", "外祖母", "其他"};
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>家属管理 - 薪资管理系统</title>
    <style>
        .modal-actions { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
        .form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 14px; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "family"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>家属管理</h2>
            <button class="btn btn-success" onclick="openAddModal()">+ 新增家属</button>
        </div>

        <%@ include file="_alerts.jsp" %>

        <!-- 筛选 -->
        <form class="filter-box" method="get" action="<%= request.getContextPath() %>/family">
            <select name="filterEmpId">
                <option value="">全部员工</option>
                <% for (empInfo e : allEmployees) { %>
                <option value="<%= e.getEmpId() %>" <%= filterEmpId.equals(String.valueOf(e.getEmpId())) ? "selected" : "" %>>
                    <%= e.getEmpNo() %> - <%= e.getEmpName() %>
                </option>
                <% } %>
            </select>
            <input type="text" name="keyword" placeholder="搜索姓名/身份证/关系..." value="<%= keyword %>">
            <button type="submit" class="btn btn-primary">筛选</button>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/family">重置</a>
        </form>

        <!-- 列表 -->
        <div class="table-container">
            <h3>家属列表</h3>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>员工编号</th>
                    <th>员工姓名</th>
                    <th>部门</th>
                    <th>关系</th>
                    <th>姓名(脱敏)</th>
                    <th>身份证(脱敏)</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <% if (rows.isEmpty()) { %>
                <tr><td colspan="8" class="text-center" style="color:#999;padding:20px;">暂无家属记录</td></tr>
                <% } else {
                    for (FamilyViewRow row : rows) {
                        empFamily f = row.getFamily();
                %>
                <tr>
                    <td><%= f.getFamilyId() %></td>
                    <td><%= row.getEmpNo() %></td>
                    <td><%= DesensitizeUtil.maskName(row.getEmpName()) %></td>
                    <td><%= row.getDeptName() %></td>
                    <td><%= f.getRelation() %></td>
                    <td><span class="masked"><%= row.getNameMasked() %></span></td>
                    <td><span class="masked"><%= row.getIdCardMasked() %></span></td>
                    <td>
                        <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/family?action=edit&id=<%= f.getFamilyId() %>">编辑</a>
                        <a class="btn btn-danger btn-sm" href="javascript:void(0)" onclick="deleteFamily(<%= f.getFamilyId() %>)">删除</a>
                    </td>
                </tr>
                <% }} %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- 新增家属弹窗 -->
<div class="modal-overlay" id="addModal">
    <div class="modal">
        <h3>新增家属</h3>
        <form method="post" action="<%= request.getContextPath() %>/family" onsubmit="return validateFamilyForm()">
            <input type="hidden" name="action" value="save">
            <div class="form-row">
                <div class="form-group">
                    <label>所属员工 *</label>
                    <select name="empId" id="addEmpId" required>
                        <option value="">请选择员工</option>
                        <% for (empInfo e : allEmployees) { %>
                        <option value="<%= e.getEmpId() %>"><%= e.getEmpNo() %> - <%= e.getEmpName() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label>关系 *</label>
                    <select name="relation" id="addRelation" required>
                        <option value="">请选择关系</option>
                        <% for (String rel : relations) { %>
                        <option value="<%= rel %>"><%= rel %></option>
                        <% } %>
                    </select>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>姓名 *</label>
                    <input type="text" name="name" id="addName" required placeholder="家属姓名">
                </div>
                <div class="form-group">
                    <label>身份证号 *</label>
                    <input type="text" name="idCard" id="addIdCard" required placeholder="18位身份证号">
                </div>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeAddModal()">取消</button>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<!-- 编辑家属弹窗 -->
<div class="modal-overlay<%= editModalOpen ? " show" : "" %>" id="editModal">
    <div class="modal">
        <h3>编辑家属</h3>
        <form method="post" action="<%= request.getContextPath() %>/family" onsubmit="return validateFamilyForm()">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="familyId" value="<%= editModalOpen ? editFamily.getFamilyId() : "" %>">
            <div class="form-row">
                <div class="form-group">
                    <label>所属员工 *</label>
                    <select name="empId" required>
                        <option value="">请选择员工</option>
                        <% for (empInfo e : allEmployees) {
                            String sel = editModalOpen && editFamily.getEmpId() != null && editFamily.getEmpId().equals(e.getEmpId()) ? "selected" : "";
                        %>
                        <option value="<%= e.getEmpId() %>" <%= sel %>><%= e.getEmpNo() %> - <%= e.getEmpName() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label>关系 *</label>
                    <select name="relation" required>
                        <option value="">请选择关系</option>
                        <% for (String rel : relations) {
                            String sel = editModalOpen && rel.equals(editFamily.getRelation()) ? "selected" : "";
                        %>
                        <option value="<%= rel %>" <%= sel %>><%= rel %></option>
                        <% } %>
                    </select>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>姓名 *</label>
                    <input type="text" name="name" required value="<%= editModalOpen && editFamily.getName() != null ? editFamily.getName() : "" %>">
                </div>
                <div class="form-group">
                    <label>身份证号 *</label>
                    <input type="text" name="idCard" required value="<%= editModalOpen && editFamily.getIdCard() != null ? editFamily.getIdCard() : "" %>">
                </div>
            </div>
            <div class="modal-actions">
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/family">取消</a>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<script>
function validateFamilyForm() {
    var idCard = (document.getElementById('addIdCard') || document.getElementById('editIdCard')).value.trim();
    if (idCard.length !== 18 || !/^\d{17}[\dXx]$/.test(idCard)) {
        alert('身份证号应为18位数字，最后一位可为数字或X');
        return false;
    }
    return true;
}
function openAddModal() { openModal('addModal'); }
function closeAddModal() {
    closeModal('addModal');
    document.getElementById('addEmpId').value = '';
    document.getElementById('addRelation').value = '';
    document.getElementById('addName').value = '';
    document.getElementById('addIdCard').value = '';
}
function deleteFamily(id) {
    if (!confirm('确认删除该家属记录？')) return;
    submitForm('<%= request.getContextPath() %>/family', { action: 'delete', id: id });
}
</script>
</body>
</html>
