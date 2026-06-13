<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.salarysystem.model.sysUser, com.salarysystem.model.sysRole, com.salarysystem.model.PageResult" %>
<%@ page import="java.util.List" %>
<%
    sysUser currentUser = (sysUser) session.getAttribute("currentUser");
    if (currentUser == null) { response.sendRedirect("login.jsp"); return; }
    if (!"系统管理员".equals(session.getAttribute("currentUserRole"))) {
        session.setAttribute("message", "权限不足");
        response.sendRedirect("dashboard");
        return;
    }

    @SuppressWarnings("unchecked")
    PageResult<sysUser> users = (PageResult<sysUser>) request.getAttribute("users");

    @SuppressWarnings("unchecked")
    List<sysRole> allRoles = (List<sysRole>) request.getAttribute("allRoles");
    if (allRoles == null) allRoles = new java.util.ArrayList<>();

    String keyword = request.getAttribute("keyword") != null ? request.getAttribute("keyword").toString() : "";
    String filterRoleId = request.getAttribute("filterRoleId") != null ? request.getAttribute("filterRoleId").toString() : "";
%>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="_head.jsp" %>
    <title>用户管理 - 薪资管理系统</title>
    <style>
        .filter-toolbar { background: #f9f9f9; padding: 14px; border-radius: 6px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin-bottom: 15px; }
        .filter-toolbar input, .filter-toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; }
        .modal-actions { display: flex; gap: 10px; margin-top: 20px; justify-content: flex-end; }
    </style>
</head>
<body>
<%@ include file="_navbar.jsp" %>

<div class="layout">
    <% request.setAttribute("activeSidebar", "user-management"); %>
    <%@ include file="_sidebar.jsp" %>

    <div class="main-content">
        <div class="header-section">
            <h2>用户管理</h2>
        </div>

        <%@ include file="_alerts.jsp" %>

        <!-- 筛选栏 -->
        <form class="filter-toolbar" method="get" action="<%= request.getContextPath() %>/user-management">
            <input type="text" name="keyword" placeholder="搜索用户名或员工编号..." value="<%= keyword %>" style="width:220px;">
            <select name="filterRoleId">
                <option value="">全部角色</option>
                <% for (sysRole r : allRoles) { %>
                <option value="<%= r.getRoleId() %>" <%= r.getRoleId().toString().equals(filterRoleId) ? "selected" : "" %>><%= r.getRoleName() %></option>
                <% } %>
            </select>
            <button type="submit" class="btn btn-primary">查询</button>
            <a class="btn btn-secondary" href="<%= request.getContextPath() %>/user-management">重置</a>
            <button type="button" class="btn btn-success" onclick="openAddModal()">+ 新增用户</button>
        </form>

        <!-- 用户列表 -->
        <div class="table-container">
            <% if (users != null && users.getTotalCount() > 0) { %>
            <div class="info-text">共 <%= users.getTotalCount() %> 条记录，第 <%= users.getPageNo() %>/<%= users.getTotalPages() %> 页</div>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>用户名</th>
                    <th>角色</th>
                    <th>关联员工ID</th>
                    <th>密码更新时间</th>
                    <th>失败次数</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <% for (sysUser u : users.getData()) {
                    String roleName = "";
                    for (sysRole r : allRoles) {
                        if (r.getRoleId().equals(u.getRoleId())) { roleName = r.getRoleName(); break; }
                    }
                    boolean locked = u.getLockTime() != null && java.time.LocalDateTime.now().isBefore(u.getLockTime());
                %>
                <tr>
                    <td><%= u.getUserId() %></td>
                    <td><strong><%= u.getUsername() %></strong></td>
                    <td><%= roleName %></td>
                    <td><%= u.getEmpId() != null ? u.getEmpId() : "-" %></td>
                    <td><%= u.getPwdUpdateTime() != null ? u.getPwdUpdateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-" %></td>
                    <td><%= u.getFailCount() != null ? u.getFailCount() : 0 %></td>
                    <td>
                        <% if (locked) { %>
                        <span class="badge badge-danger">已锁定</span>
                        <% } else { %>
                        <span class="badge badge-success">正常</span>
                        <% } %>
                    </td>
                    <td>
                        <button class="btn btn-primary btn-sm" onclick="openEditModal('<%= u.getUserId() %>','<%= u.getUsername() %>','<%= u.getRoleId() %>','<%= u.getEmpId() != null ? u.getEmpId() : "" %>')">编辑</button>
                        <% if (locked) { %>
                        <form method="post" action="<%= request.getContextPath() %>/user-management" style="display:inline;">
                            <input type="hidden" name="action" value="unlock">
                            <input type="hidden" name="userId" value="<%= u.getUserId() %>">
                            <button type="submit" class="btn btn-warning btn-sm" onclick="return confirm('确认解锁该用户？')">解锁</button>
                        </form>
                        <% } %>
                        <button class="btn btn-warning btn-sm" onclick="openResetPwdModal('<%= u.getUserId() %>','<%= u.getUsername() %>')">重置密码</button>
                        <% if (!u.getUserId().equals(currentUser.getUserId())) { %>
                        <form method="post" action="<%= request.getContextPath() %>/user-management" style="display:inline;" onsubmit="return confirm('确认删除用户【<%= u.getUsername() %>】？此操作不可撤销。');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="userId" value="<%= u.getUserId() %>">
                            <button type="submit" class="btn btn-danger btn-sm">删除</button>
                        </form>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>

            <% if (users.getTotalPages() > 1) { %>
            <div class="pagination">
                <% if (users.hasPrevPage()) { %>
                <a href="?pageNo=1&keyword=<%= keyword %>&filterRoleId=<%= filterRoleId %>">首页</a>
                <a href="?pageNo=<%= users.getPageNo()-1 %>&keyword=<%= keyword %>&filterRoleId=<%= filterRoleId %>">上一页</a>
                <% } else { %>
                <span class="disabled">首页</span>
                <span class="disabled">上一页</span>
                <% } %>
                <span class="current">第 <%= users.getPageNo() %> / <%= users.getTotalPages() %> 页</span>
                <% if (users.hasNextPage()) { %>
                <a href="?pageNo=<%= users.getPageNo()+1 %>&keyword=<%= keyword %>&filterRoleId=<%= filterRoleId %>">下一页</a>
                <a href="?pageNo=<%= users.getTotalPages() %>&keyword=<%= keyword %>&filterRoleId=<%= filterRoleId %>">末页</a>
                <% } else { %>
                <span class="disabled">下一页</span>
                <span class="disabled">末页</span>
                <% } %>
            </div>
            <% } %>

            <% } else { %>
            <div style="text-align:center;padding:40px;color:#999;">暂无用户</div>
            <% } %>
        </div>
    </div>
</div>

<!-- 新增/编辑弹窗 -->
<div class="modal-overlay" id="editModal">
    <div class="modal">
        <h3 id="editModalTitle">新增用户</h3>
        <form method="post" action="<%= request.getContextPath() %>/user-management">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="userId" id="editUserId">
            <div class="form-group">
                <label>用户名 *</label>
                <input type="text" name="username" id="editUsername" required placeholder="请输入登录用户名">
            </div>
            <div class="form-group">
                <label>密码 <span id="pwdRequired">*</span></label>
                <input type="password" name="password" id="editPassword" placeholder="长度8位以上，含大小写字母、数字、特殊字符">
            </div>
            <div class="form-group">
                <label>角色 *</label>
                <select name="roleId" id="editRoleId" required>
                    <% for (sysRole r : allRoles) { %>
                    <option value="<%= r.getRoleId() %>"><%= r.getRoleName() %></option>
                    <% } %>
                </select>
            </div>
            <div class="form-group">
                <label>关联员工ID（可选）</label>
                <input type="number" name="empId" id="editEmpId" placeholder="留空表示不关联员工">
            </div>
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeEditModal()">取消</button>
                <button type="submit" class="btn btn-success">保存</button>
            </div>
        </form>
    </div>
</div>

<!-- 重置密码弹窗 -->
<div class="modal-overlay" id="resetPwdModal">
    <div class="modal" style="width:380px;">
        <h3>重置密码</h3>
        <p style="color:#666;margin-bottom:14px;">用户：<strong id="resetPwdUsername"></strong></p>
        <form method="post" action="<%= request.getContextPath() %>/user-management">
            <input type="hidden" name="action" value="resetPwd">
            <input type="hidden" name="userId" id="resetPwdUserId">
            <div class="form-group">
                <label>新密码 *</label>
                <input type="password" name="newPassword" required placeholder="长度8位以上，含大小写字母、数字、特殊字符">
            </div>
            <div class="modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeResetPwdModal()">取消</button>
                <button type="submit" class="btn btn-warning">确认重置</button>
            </div>
        </form>
    </div>
</div>

<script>
function openAddModal() {
    document.getElementById('editModalTitle').textContent = '新增用户';
    document.getElementById('editUserId').value = '';
    document.getElementById('editUsername').value = '';
    document.getElementById('editPassword').value = '';
    document.getElementById('editPassword').required = true;
    document.getElementById('pwdRequired').style.display = 'inline';
    document.getElementById('editRoleId').value = '2';
    document.getElementById('editEmpId').value = '';
    openModal('editModal');
}
function openEditModal(id, username, roleId, empId) {
    document.getElementById('editModalTitle').textContent = '编辑用户';
    document.getElementById('editUserId').value = id;
    document.getElementById('editUsername').value = username;
    document.getElementById('editPassword').value = '';
    document.getElementById('editPassword').required = false;
    document.getElementById('pwdRequired').style.display = 'none';
    document.getElementById('editRoleId').value = roleId;
    document.getElementById('editEmpId').value = empId;
    openModal('editModal');
}
function closeEditModal() { closeModal('editModal'); }
function openResetPwdModal(id, username) {
    document.getElementById('resetPwdUserId').value = id;
    document.getElementById('resetPwdUsername').textContent = username;
    openModal('resetPwdModal');
}
function closeResetPwdModal() { closeModal('resetPwdModal'); }
</script>
</body>
</html>
