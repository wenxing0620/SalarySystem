<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, com.salarysystem.model.empInfo, com.salarysystem.model.sysDept" %>
<%@ page import="com.salarysystem.util.DesensitizeUtil, java.util.List" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    @SuppressWarnings("unchecked")
    List<empInfo> empList = (List<empInfo>) request.getAttribute("empList");
    if (empList == null) empList = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    List<sysDept> deptList = (List<sysDept>) request.getAttribute("deptList");
    if (deptList == null) deptList = new java.util.ArrayList<>();

    empInfo editEmp = (empInfo) request.getAttribute("editEmployee");
    boolean editModalOpen = editEmp != null;

    String keyword = request.getAttribute("keyword") != null ? request.getAttribute("keyword").toString() : "";
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>员工管理 - 薪资管理系统</title>
    <style>
        .modal-actions { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "emp-list"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>员工管理</h2>
            <button class="btn btn-success" onclick="openAddModal()">+ 新增员工</button>
        </div>

        <%@ include file="_alerts.jsp" %>

        <form class="filter-box" method="get" action="<%= request.getContextPath() %>/emp-list">
            <label for="searchInput" style="position:absolute;left:-9999px;">搜索员工</label>
            <input type="text" id="searchInput" name="keyword" placeholder="搜索员工编号、姓名、部门、岗位、身份证、手机号..." value="<%= keyword %>" style="min-width:350px;" onkeydown="if(event.key==='Enter'){event.preventDefault();this.form.submit();}">
            <button type="submit" class="btn btn-primary">搜索</button>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/emp-list">重置</a>
        </form>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>员工编号</th>
                        <th>姓名</th>
                        <th>部门</th>
                        <th>岗位</th>
                        <th>身份证</th>
                        <th>手机号</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                <% for (empInfo e : empList) { %>
                    <tr>
                        <td><%= e.getEmpNo() %></td>
                        <td><%= DesensitizeUtil.maskName(e.getEmpName()) %></td>
                        <td><%= e.getDeptName() %></td>
                        <td><%= e.getPosition() %></td>
                        <td><span class="masked"><%= DesensitizeUtil.maskIdCard(e.getIdCard()) %></span></td>
                        <td><span class="masked"><%= DesensitizeUtil.maskPhone(e.getPhone()) %></span></td>
                        <td>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/emp-view?id=<%= e.getEmpId() %>">查看</a>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/emp-list?editId=<%= e.getEmpId() %>&keyword=<%= keyword %>">编辑</a>
                            <a class="btn btn-danger btn-sm" href="<%= request.getContextPath() %>/emp-delete?id=<%= e.getEmpId() %>" onclick="return confirm('确定要删除该员工吗？');">删除</a>
                        </td>
                    </tr>
                <% } %>
                <% if (empList.isEmpty()) { %>
                    <tr><td colspan="7" class="text-center" style="padding:30px;color:#999;">暂无员工数据</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- 新增员工弹窗 -->
<div class="modal-overlay" id="addModal">
    <div class="modal modal-wide">
        <h3>新增员工</h3>
        <form method="post" action="<%= request.getContextPath() %>/emp-add" onsubmit="return validateAddForm()">
            <div class="form-row">
                <div class="form-group" style="flex:1;">
                    <label>员工编号 *</label>
                    <input type="text" name="empNo" id="addEmpNo" required placeholder="如 EMP001">
                    <div class="help-text">必填项，需唯一</div>
                </div>
                <div class="form-group" style="flex:1;">
                    <label>姓名 *</label>
                    <input type="text" name="empName" id="addEmpName" required placeholder="如 李明">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group" style="flex:1;">
                    <label>部门</label>
                    <select name="deptName" id="addDeptName">
                        <option value="">-- 请选择部门 --</option>
                        <% for (sysDept d : deptList) { %>
                        <option value="<%= d.getDeptName() %>"><%= d.getDeptName() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group" style="flex:1;">
                    <label>岗位</label>
                    <input type="text" name="position" id="addPosition" placeholder="如 工程师">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group" style="flex:1;">
                    <label>身份证号 *</label>
                    <input type="text" name="idCard" id="addIdCard" required placeholder="18 位身份证号">
                </div>
                <div class="form-group" style="flex:1;">
                    <label>手机号</label>
                    <input type="text" name="phone" id="addPhone" placeholder="如 13912345678">
                </div>
            </div>
            <div class="form-group">
                <label>住址</label>
                <input type="text" name="address" id="addAddress" placeholder="如 北京市朝阳区...">
            </div>
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeAddModal()">取消</button>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<!-- 编辑员工弹窗 -->
<div class="modal-overlay<%= editModalOpen ? " show" : "" %>" id="editModal">
    <div class="modal modal-wide">
        <h3>编辑员工</h3>
        <form method="post" action="<%= request.getContextPath() %>/emp-edit">
            <input type="hidden" name="empId" value="<%= editModalOpen && editEmp.getEmpId() != null ? editEmp.getEmpId() : "" %>">
            <div class="form-row">
                <div class="form-group" style="flex:1;">
                    <label>员工编号 *</label>
                    <input type="text" name="empNo" required value="<%= editModalOpen && editEmp.getEmpNo() != null ? editEmp.getEmpNo() : "" %>">
                </div>
                <div class="form-group" style="flex:1;">
                    <label>姓名 *</label>
                    <input type="text" name="empName" required value="<%= editModalOpen && editEmp.getEmpName() != null ? editEmp.getEmpName() : "" %>">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group" style="flex:1;">
                    <label>部门</label>
                    <select name="deptName">
                        <option value="">-- 请选择部门 --</option>
                        <% for (sysDept d : deptList) {
                            String sel = editModalOpen && editEmp.getDeptName() != null && editEmp.getDeptName().equals(d.getDeptName()) ? "selected" : "";
                        %>
                        <option value="<%= d.getDeptName() %>" <%= sel %>><%= d.getDeptName() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group" style="flex:1;">
                    <label>岗位</label>
                    <input type="text" name="position" value="<%= editModalOpen && editEmp.getPosition() != null ? editEmp.getPosition() : "" %>">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group" style="flex:1;">
                    <label>身份证号 *</label>
                    <input type="text" name="idCard" required value="<%= editModalOpen && editEmp.getIdCard() != null ? editEmp.getIdCard() : "" %>">
                </div>
                <div class="form-group" style="flex:1;">
                    <label>手机号</label>
                    <input type="text" name="phone" value="<%= editModalOpen && editEmp.getPhone() != null ? editEmp.getPhone() : "" %>">
                </div>
            </div>
            <div class="form-group">
                <label>住址</label>
                <input type="text" name="address" value="<%= editModalOpen && editEmp.getAddress() != null ? editEmp.getAddress() : "" %>">
            </div>
            <div class="modal-actions">
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/emp-list<%= !keyword.isEmpty() ? "?keyword=" + keyword : "" %>">取消</a>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<script>
function openAddModal() {
    openModal('addModal');
}
function closeAddModal() {
    closeModal('addModal');
    // 清空表单
    document.getElementById('addEmpNo').value = '';
    document.getElementById('addEmpName').value = '';
    document.getElementById('addDeptName').value = '';
    document.getElementById('addPosition').value = '';
    document.getElementById('addIdCard').value = '';
    document.getElementById('addPhone').value = '';
    document.getElementById('addAddress').value = '';
}
function validateAddForm() {
    var empNo = document.getElementById('addEmpNo').value.trim();
    var empName = document.getElementById('addEmpName').value.trim();
    var idCard = document.getElementById('addIdCard').value.trim();
    if (!empNo) { alert('员工编号不能为空'); return false; }
    if (!empName) { alert('姓名不能为空'); return false; }
    if (!idCard) { alert('身份证号不能为空'); return false; }
    if (idCard.length !== 18) { alert('身份证号应为 18 位'); return false; }
    return true;
}

// 关闭编辑弹窗（点击取消链接已处理，这里处理遮罩点击）
function closeEditModal() {
    closeModal('editModal');
}
</script>
</body>
</html>
