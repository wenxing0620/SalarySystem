<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, com.salarysystem.model.sysDept" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    sysUser user = (sysUser) session.getAttribute("currentUser");
    if (user == null) { response.sendRedirect("login.jsp"); return; }

    @SuppressWarnings("unchecked")
    List<sysDept> deptList = (List<sysDept>) request.getAttribute("deptList");
    if (deptList == null) deptList = new java.util.ArrayList<>();

    @SuppressWarnings("unchecked")
    Map<Integer, Integer> empCountMap = (Map<Integer, Integer>) request.getAttribute("empCountMap");

    sysDept editDept = (sysDept) request.getAttribute("editDept");
    boolean editModalOpen = editDept != null;
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>部门管理 - 薪资管理系统</title>
    <style>
        .modal-actions { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "dept"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>部门管理</h2>
            <button class="btn btn-success" onclick="openAddModal()">+ 新增部门</button>
        </div>

        <%@ include file="_alerts.jsp" %>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th style="width:80px;">序号</th>
                        <th>部门名称</th>
                        <th>备注</th>
                        <th style="width:100px;">员工数</th>
                        <th style="width:140px;">操作</th>
                    </tr>
                </thead>
                <tbody>
                <% if (deptList.isEmpty()) { %>
                    <tr><td colspan="5" class="text-center" style="padding:30px;color:#999;">暂无部门数据</td></tr>
                <% } else {
                    int index = 1;
                    for (sysDept d : deptList) {
                        int empCount = empCountMap != null && empCountMap.containsKey(d.getDeptId())
                                ? empCountMap.get(d.getDeptId()) : 0;
                %>
                    <tr>
                        <td><%= index++ %></td>
                        <td><strong><%= d.getDeptName() %></strong></td>
                        <td><span class="text-muted"><%= d.getRemark() != null && !d.getRemark().isEmpty() ? d.getRemark() : "-" %></span></td>
                        <td><span class="badge"><%= empCount %> 人</span></td>
                        <td>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/dept?editId=<%= d.getDeptId() %>">编辑</a>
                            <a class="btn btn-danger btn-sm" href="javascript:void(0)"
                               onclick="deleteDept(<%= d.getDeptId() %>, '<%= d.getDeptName() %>', <%= empCount %>)">删除</a>
                        </td>
                    </tr>
                <% } } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- 新增部门弹窗 -->
<div class="modal-overlay" id="addModal">
    <div class="modal">
        <h3>新增部门</h3>
        <form method="post" action="<%= request.getContextPath() %>/dept">
            <input type="hidden" name="action" value="save">
            <div class="form-group">
                <label>部门名称 *</label>
                <input type="text" name="deptName" id="addDeptName" required placeholder="如 技术部">
            </div>
            <div class="form-group">
                <label>备注</label>
                <input type="text" name="remark" id="addRemark" placeholder="可选备注">
            </div>
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeAddModal()">取消</button>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<!-- 编辑部门弹窗 -->
<div class="modal-overlay<%= editModalOpen ? " show" : "" %>" id="editModal">
    <div class="modal">
        <h3>编辑部门</h3>
        <form method="post" action="<%= request.getContextPath() %>/dept">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="deptId" value="<%= editModalOpen ? editDept.getDeptId() : "" %>">
            <div class="form-group">
                <label>部门名称 *</label>
                <input type="text" name="deptName" required value="<%= editModalOpen && editDept.getDeptName() != null ? editDept.getDeptName() : "" %>">
            </div>
            <div class="form-group">
                <label>备注</label>
                <input type="text" name="remark" value="<%= editModalOpen && editDept.getRemark() != null ? editDept.getRemark() : "" %>">
            </div>
            <div class="modal-actions">
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/dept">取消</a>
                <button type="submit" class="btn btn-success">更新</button>
            </div>
        </form>
    </div>
</div>

<script>
function openAddModal() { openModal('addModal'); }
function closeAddModal() {
    closeModal('addModal');
    document.getElementById('addDeptName').value = '';
    document.getElementById('addRemark').value = '';
}
function deleteDept(deptId, deptName, empCount) {
    if (empCount > 0) {
        alert('部门【' + deptName + '】下还有 ' + empCount + ' 名员工，无法删除。\n请先将员工转移到其他部门或删除员工。');
        return;
    }
    if (!confirm('确定要删除部门【' + deptName + '】吗？此操作不可撤销。')) return;
    submitForm('<%= request.getContextPath() %>/dept', { action: 'delete', deptId: deptId });
}
</script>
</body>
</html>
