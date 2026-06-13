package com.salarysystem.security;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.*;

/**
 * 角色权限过滤器 —— 根据用户角色限制 URL 访问。
 *
 * 角色通过 sys_role 表中的角色名称识别（不依赖自增ID）：
 *   系统管理员   全部功能
 *   人事管理员   员工管理、家属管理
 *   财务管理员   薪资管理、专项附加扣除
 *   总经理       可查看全部页面（写操作在 servlet 层拦截）
 *   审计管理员   仅审计日志
 */
@WebFilter("/*")
public class RoleAuthFilter implements Filter {

    // ── 无需登录即可访问的路径 ──
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/login", "/login.jsp", "/logout", "/error.jsp", "/"
    );

    // ── 人事管理员可访问 ──
    private static final Set<String> HR_PATHS = Set.of(
        "/emp-list", "/emp-add", "/emp-edit", "/emp-delete", "/emp-view",
        "/emp-list.jsp", "/emp-add.jsp", "/emp-edit.jsp", "/emp-view.jsp",
        "/family", "/family.jsp",
        "/dept", "/dept.jsp"
    );

    // ── 财务管理员可访问 ──
    private static final Set<String> FINANCE_PATHS = Set.of(
        "/salary-list", "/salary", "/salary-calculate-tax",
        "/salary-import-excel", "/salary-export-excel",
        "/salary-list.jsp",
        "/deduction", "/deduction.jsp"
    );

    // ── 审计管理员可访问 ──
    private static final Set<String> AUDIT_PATHS = Set.of(
        "/audit-log", "/audit-log.jsp"
    );

    // ── 所有已登录用户可访问 ──
    private static final Set<String> COMMON_PATHS = Set.of(
        "/dashboard", "/dashboard.jsp",
        "/change-password", "/change-password.jsp"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 1. 公开路径直接放行
        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. 放行静态资源（css/js/图片等）
        if (path.startsWith("/static/") || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. 检查登录状态
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String roleName = (String) session.getAttribute("currentUserRole");

        // 检查密码过期标记 → 强制跳转修改密码页
        if (Boolean.TRUE.equals(session.getAttribute("pendingPasswordChange"))) {
            if ("/change-password".equals(path) || "/change-password.jsp".equals(path)
                    || "/logout".equals(path) || "/login.jsp".equals(path)) {
                chain.doFilter(request, response);
            } else {
                resp.sendRedirect(req.getContextPath() + "/change-password");
            }
            return;
        }

        // 4. 系统管理员拥有全部权限
        if ("系统管理员".equals(roleName)) {
            chain.doFilter(request, response);
            return;
        }

        // 5. 公共页面（dashboard 等）所有已登录用户可访问
        if (COMMON_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 6. 按角色检查权限
        boolean allowed = isAllowed(roleName, path);
        if (allowed) {
            chain.doFilter(request, response);
        } else {
            // 无权限 → 返回 403 页面
            req.setAttribute("error", "权限不足：您的角色【" + (roleName != null ? roleName : "未知") + "】无权访问【" + path + "】");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    /**
     * 根据角色名称判断是否有权访问指定路径
     */
    private boolean isAllowed(String roleName, String path) {
        if (roleName == null) return false;

        switch (roleName) {
            case "人事管理员":
                return HR_PATHS.contains(path) || COMMON_PATHS.contains(path);
            case "财务管理员":
                return FINANCE_PATHS.contains(path) || COMMON_PATHS.contains(path);
            case "总经理":
                return HR_PATHS.contains(path) || FINANCE_PATHS.contains(path) || COMMON_PATHS.contains(path);
            case "审计管理员":
                return AUDIT_PATHS.contains(path) || COMMON_PATHS.contains(path);
            default:
                return false;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}
    @Override
    public void destroy() {}
}
