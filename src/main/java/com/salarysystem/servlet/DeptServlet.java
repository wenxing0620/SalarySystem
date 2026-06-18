package com.salarysystem.servlet;

import com.salarysystem.model.PageResult;
import com.salarysystem.model.sysDept;
import com.salarysystem.model.sysUser;
import com.salarysystem.service.impl.SysDeptServiceImpl;
import com.salarysystem.service.impl.SysLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/dept")
public class DeptServlet extends HttpServlet {

    private final SysDeptServiceImpl deptService = new SysDeptServiceImpl();
    private final SysLogServiceImpl logService = new SysLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            List<sysDept> allDeptList = deptService.findAll();

            // 统计每个部门的员工数
            java.util.Map<Integer, Integer> empCountMap = new java.util.LinkedHashMap<>();
            com.salarysystem.dao.impl.SysDeptDaoImpl dao = new com.salarysystem.dao.impl.SysDeptDaoImpl();
            for (sysDept d : allDeptList) {
                empCountMap.put(d.getDeptId(), dao.countEmpByDeptName(d.getDeptName()));
            }

            // 分页：每页10条
            int pageNo = Math.max(1, parseInt(req.getParameter("pageNo")));
            int pageSize = 10;
            PageResult<sysDept> pageResult = paginate(allDeptList, pageNo, pageSize);

            req.setAttribute("pageResult", pageResult);
            req.setAttribute("deptList", pageResult.getData());
            req.setAttribute("empCountMap", empCountMap);

            // 编辑模式：加载指定部门到表单
            String editId = req.getParameter("editId");
            if (editId != null && !editId.isEmpty()) {
                try {
                    sysDept editDept = deptService.findById(Integer.parseInt(editId));
                    req.setAttribute("editDept", editDept);
                } catch (NumberFormatException ignored) {}
            }

            req.getRequestDispatcher("/dept.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "加载部门列表失败，请稍后重试");
            req.getRequestDispatcher("/dept.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        sysUser user = (sysUser) session.getAttribute("currentUser");
        // 总经理只能查看，不能操作
        if ("总经理".equals(session.getAttribute("currentUserRole"))) {
            session.setAttribute("message", "权限不足：总经理只能查看，不能操作部门");
            resp.sendRedirect(req.getContextPath() + "/dept");
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("delete".equals(action)) {
                String idParam = req.getParameter("deptId");
                if (idParam == null || idParam.isEmpty()) {
                    session.setAttribute("message", "删除失败：缺少部门ID");
                    resp.sendRedirect(req.getContextPath() + "/dept");
                    return;
                }
                Integer deptId = Integer.parseInt(idParam);
                String checkError = deptService.checkBeforeDelete(deptId);
                if (checkError != null) {
                    session.setAttribute("message", checkError);
                } else {
                    deptService.delete(deptId);
                    session.setAttribute("message", "部门删除成功");
                    try { logService.log(user.getUserId(), "DELETE_DEPT", req.getRemoteAddr()); } catch (SQLException ignored) {}
                }
                resp.sendRedirect(req.getContextPath() + "/dept");
                return;
            }

            // 保存（新增或更新）
            String deptIdParam = req.getParameter("deptId");
            String deptName = req.getParameter("deptName");
            String remark = req.getParameter("remark");

            if (deptName == null || deptName.trim().isEmpty()) {
                session.setAttribute("message", "保存失败：部门名称不能为空");
                resp.sendRedirect(req.getContextPath() + "/dept");
                return;
            }

            sysDept dept = new sysDept();
            dept.setDeptName(deptName.trim());
            dept.setRemark(remark != null ? remark.trim() : "");

            if (deptIdParam != null && !deptIdParam.isEmpty()) {
                // 更新
                dept.setDeptId(Integer.parseInt(deptIdParam));
                deptService.update(dept);
                session.setAttribute("message", "部门更新成功");
                try { logService.log(user.getUserId(), "UPDATE_DEPT", req.getRemoteAddr()); } catch (SQLException ignored) {}
            } else {
                // 新增
                deptService.add(dept);
                session.setAttribute("message", "部门新增成功");
                try { logService.log(user.getUserId(), "ADD_DEPT", req.getRemoteAddr()); } catch (SQLException ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/dept");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "操作失败，请稍后重试");
            resp.sendRedirect(req.getContextPath() + "/dept");
        }
    }

    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 1;
        try { return Integer.parseInt(value.trim()); } catch (NumberFormatException e) { return 1; }
    }

    private <T> PageResult<T> paginate(List<T> fullList, int pageNo, int pageSize) {
        int total = fullList.size();
        int from = (pageNo - 1) * pageSize;
        if (from >= total) from = 0;
        int to = Math.min(from + pageSize, total);
        return new PageResult<>(fullList.subList(from, to), pageNo, pageSize, total);
    }
}
