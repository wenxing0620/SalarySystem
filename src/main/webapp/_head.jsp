<%@ page pageEncoding="UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
/* ===========================================================
   薪资管理系统  v2.0 — 沉稳金融企业风
   深色侧栏 · 明亮内容区 · 克制配色 · 微质感
   =========================================================== */

/* ----- 变量 ----- */
:root {
  --c-bg:       #f1f5f9;
  --c-surface:  #ffffff;
  --c-border:   #e2e8f0;
  --c-text:     #1e293b;
  --c-muted:    #64748b;
  --c-faint:    #94a3b8;

  --c-primary:       #4338ca;
  --c-primary-hover: #3730a3;
  --c-primary-light: #e0e7ff;
  --c-primary-soft:  #eef2ff;

  --c-success:       #059669;
  --c-success-light: #d1fae5;
  --c-danger:        #e11d48;
  --c-danger-light:  #ffe4e6;
  --c-warning:       #d97706;
  --c-warning-light: #fef3c7;
  --c-info:          #0284c7;
  --c-info-light:    #e0f2fe;

  --sidebar-w: 232px;
  --topbar-h:  56px;
  --radius:    10px;
  --radius-sm: 6px;
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.04);
  --shadow-md: 0 4px 14px rgba(0,0,0,0.06);
  --shadow-lg: 0 16px 48px rgba(0,0,0,0.12);
  --transition: 0.18s ease;
}

/* ----- Reset & Base ----- */
*, *::before, *::after { margin:0; padding:0; box-sizing:border-box; }
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
               'Microsoft YaHei', 'Helvetica Neue', sans-serif;
  background: var(--c-bg);
  color: var(--c-text);
  font-size: 14px;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}
a { color: var(--c-primary); text-decoration: none; transition: color var(--transition); }
a:hover { color: var(--c-primary-hover); }

/* ----- Navbar ----- */
.navbar {
  height: var(--topbar-h);
  background: #fff;
  border-bottom: 1px solid var(--c-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  position: fixed; top:0; left:0; right:0; z-index:100;
  box-shadow: var(--shadow-sm);
}
.navbar h1 {
  font-size: 17px;
  font-weight: 700;
  color: var(--c-text);
  letter-spacing: -0.3px;
}
.navbar a, .navbar span { color: var(--c-muted); font-size:13px; text-decoration:none; }
.navbar a:hover { color: var(--c-text); text-decoration:none; }
.navbar-right { display:flex; gap:18px; align-items:center; }
.navbar-right .user-chip {
  display:flex; align-items:center; gap:8px;
  background: var(--c-bg); padding:5px 12px 5px 5px;
  border-radius: 999px; font-size:13px; color: var(--c-text);
}
.navbar-right .avatar {
  width:30px; height:30px; border-radius:50%;
  background: var(--c-primary); color:#fff;
  display:flex; align-items:center; justify-content:center;
  font-size:12px; font-weight:700;
}

/* ----- Layout ----- */
.layout { display:flex; min-height:calc(100vh - var(--topbar-h)); padding-top:var(--topbar-h); }

/* ----- Sidebar ----- */
.sidebar {
  width: var(--sidebar-w); flex-shrink:0;
  background: #1e293b;
  position: fixed; top:var(--topbar-h); left:0; bottom:0;
  padding: 18px 0;
  overflow-y: auto;
  z-index: 90;
}
.sidebar-menu { list-style:none; }
.sidebar-menu a {
  display:flex; align-items:center; gap:10px;
  padding: 10px 16px; margin: 2px 10px;
  border-radius: var(--radius-sm);
  color: #94a3b8; font-size:13px; text-decoration:none;
  border-left: 3px solid transparent;
  transition: all var(--transition);
}
.sidebar-menu a:hover {
  background: rgba(255,255,255,0.06);
  color: #fff;
  border-left-color: #64748b;
}
.sidebar-menu a.active {
  background: rgba(99,102,241,0.15);
  color: #fff;
  border-left-color: #818cf8;
  font-weight:600;
}

/* sidebar sections */
.sidebar-section { padding: 0 4px; }
.sidebar-label {
  font-size:10px; text-transform:uppercase; letter-spacing:1.2px;
  color: #64748b; padding:14px 16px 4px; font-weight:700;
}
.sidebar-divider { height:1px; background:#334155; margin:10px 14px; }

/* ----- Main Content ----- */
.main-content { flex:1; padding:28px 30px; overflow-x:auto; margin-left:var(--sidebar-w); }

/* ----- Page Header ----- */
.header-section { display:flex; justify-content:space-between; align-items:center; margin-bottom:22px; }
.header-section h2 { font-size:21px; font-weight:700; color:var(--c-text); letter-spacing:-0.4px; }

/* ----- Buttons ----- */
.btn {
  display:inline-flex; align-items:center; gap:5px;
  padding:7px 15px; border:1px solid transparent; border-radius:var(--radius-sm);
  cursor:pointer; font-size:13px; font-weight:500; font-family:inherit;
  line-height:1.5; text-decoration:none; white-space:nowrap;
  transition: all var(--transition); letter-spacing:0.1px;
}
.btn:hover { transform:translateY(-1px); box-shadow:var(--shadow-md); text-decoration:none; }
.btn:active { transform:translateY(0); }

.btn-primary   { background:var(--c-primary); color:#fff; }
.btn-primary:hover { background:var(--c-primary-hover); color:#fff; }
.btn-success   { background:var(--c-success); color:#fff; }
.btn-success:hover { background:#047857; color:#fff; }
.btn-danger    { background:var(--c-danger); color:#fff; }
.btn-danger:hover { background:#be123c; color:#fff; }
.btn-warning   { background:var(--c-warning); color:#fff; }
.btn-warning:hover { background:#b45309; color:#fff; }
.btn-info      { background:var(--c-info); color:#fff; }
.btn-info:hover { background:#0369a1; color:#fff; }
.btn-secondary { background:#fff; color:var(--c-muted); border-color:var(--c-border); }
.btn-secondary:hover { background:var(--c-bg); color:var(--c-text); border-color:#cbd5e1; }
.btn-sm  { padding:4px 10px; font-size:12px; border-radius:4px; }
.btn-lg  { padding:10px 22px; font-size:15px; }
.full-width { width:100%; justify-content:center; }

/* ----- Alerts ----- */
.alert {
  padding:12px 16px; margin-bottom:16px; border-radius:var(--radius-sm);
  font-size:13px; display:flex; align-items:center; gap:8px;
  border-left:4px solid; animation: alertIn 0.25s ease;
}
@keyframes alertIn { from{opacity:0;transform:translateY(-6px);} to{opacity:1;transform:translateY(0);} }
.alert-success { background:var(--c-success-light); color:#064e3b; border-left-color:var(--c-success); }
.alert-error   { background:var(--c-danger-light);  color:#881337; border-left-color:var(--c-danger); }

/* ----- Dashboard ----- */
.dashboard-grid {
  display:grid;
  grid-template-columns:repeat(auto-fit,minmax(210px,1fr));
  gap:16px; margin-bottom:26px;
}
.card {
  background:#fff; border-radius:var(--radius); padding:20px 22px;
  box-shadow:var(--shadow-sm); border:1px solid var(--c-border);
  transition: box-shadow var(--transition);
  position:relative; overflow:hidden;
}
.card::before {
  content:''; position:absolute; left:0; top:14px; bottom:14px;
  width:4px; border-radius:0 3px 3px 0;
}
.card:nth-child(1)::before { background:var(--c-primary); }
.card:nth-child(2)::before { background:var(--c-success); }
.card:nth-child(3)::before { background:var(--c-warning); }
.card:nth-child(4)::before { background:var(--c-info); }
.card:hover { box-shadow:var(--shadow-md); }
.card h3 { font-size:11px; color:var(--c-muted); text-transform:uppercase; letter-spacing:0.6px; font-weight:600; margin-bottom:6px; }
.card .number { font-size:30px; font-weight:700; color:var(--c-text); letter-spacing:-1px; }

/* ----- Panel / Table Container ----- */
.table-container {
  background:#fff; border-radius:var(--radius);
  box-shadow:var(--shadow-sm); border:1px solid var(--c-border);
  overflow:hidden;
}
.table-container h3 {
  padding:15px 20px; border-bottom:1px solid var(--c-border);
  font-size:14px; font-weight:600; color:var(--c-text);
}

/* ----- Tables ----- */
table { width:100%; border-collapse:collapse; }
table thead th {
  background:var(--c-bg); padding:10px 14px; text-align:left;
  font-size:11px; font-weight:600; color:var(--c-muted);
  text-transform:uppercase; letter-spacing:0.5px;
  border-bottom:2px solid var(--c-border); white-space:nowrap;
}
table tbody td {
  padding:11px 14px; border-bottom:1px solid #f1f5f9;
  font-size:13px; color:var(--c-text);
}
table tbody tr:hover { background:var(--c-primary-soft); }
table tbody tr:last-child td { border-bottom:none; }

/* number alignment in tables */
th.number, td.number { text-align:right; font-variant-numeric:tabular-nums; }

/* ----- Info Table (emp-view) ----- */
.info-table { width:100%; }
.info-table td { padding:10px 14px; font-size:13px; }
.info-table td:first-child { font-weight:600; color:var(--c-muted); width:120px; white-space:nowrap; }
.info-text { font-size:13px; color:var(--c-muted); margin-top:4px; }

/* ----- Forms ----- */
.form-group { display:flex; flex-direction:column; gap:4px; margin-bottom:13px; }
.form-group label {
  font-size:11px; font-weight:600; color:var(--c-muted);
  text-transform:uppercase; letter-spacing:0.4px;
}
.form-group input, .form-group select, .form-group textarea {
  padding:9px 12px; border:1px solid var(--c-border); border-radius:var(--radius-sm);
  font-size:14px; font-family:inherit; color:var(--c-text); background:#fff;
  width:100%; transition: border-color var(--transition), box-shadow var(--transition);
}
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  outline:none; border-color:var(--c-primary);
  box-shadow:0 0 0 3px rgba(99,102,241,0.1);
}
.form-group input::placeholder, .form-group textarea::placeholder { color:var(--c-faint); }
.form-row { display:flex; gap:12px; align-items:flex-end; flex-wrap:wrap; }
.form-row .form-group { flex:1; min-width:130px; margin-bottom:0; }
.form-card {
  background:#fff; padding:22px; border-radius:var(--radius);
  box-shadow:var(--shadow-sm); border:1px solid var(--c-border); margin-bottom:18px;
}
.form-card h3 { margin-bottom:16px; font-size:15px; color:var(--c-text); }
.help-text { font-size:11px; color:var(--c-faint); margin-top:2px; }
.readonly {
  background:var(--c-bg); padding:10px 14px; border-radius:var(--radius-sm);
  min-height:36px; line-height:1.6; color:var(--c-text); border:1px solid var(--c-border);
}
.form-buttons { display:flex; gap:10px; margin-top:20px; justify-content:flex-end; }
.modal-actions { display:flex; gap:10px; margin-top:20px; justify-content:flex-end; }

/* ----- Filter Bar ----- */
.filter-box { display:flex; gap:10px; margin-bottom:18px; flex-wrap:wrap; align-items:center; }
.filter-box input, .filter-box select {
  padding:8px 12px; border:1px solid var(--c-border); border-radius:var(--radius-sm);
  font-size:13px; font-family:inherit; color:var(--c-text); background:#fff;
  transition: border-color var(--transition);
}
.filter-box input:focus, .filter-box select:focus { outline:none; border-color:var(--c-primary); }
.filter-toolbar { display:flex; gap:8px; margin-bottom:18px; flex-wrap:wrap; align-items:center; }

/* ----- Modal ----- */
.modal-overlay {
  display:none; position:fixed; inset:0;
  background:rgba(15,23,42,0.5); backdrop-filter:blur(3px);
  z-index:1000; justify-content:center; align-items:center;
}
.modal-overlay.show { display:flex; }
.modal {
  background:#fff; padding:26px 28px; border-radius:var(--radius);
  width:520px; max-width:92vw; max-height:85vh; overflow-y:auto;
  box-shadow:var(--shadow-lg);
  animation: modalIn 0.22s ease;
}
@keyframes modalIn { from{opacity:0;transform:scale(0.96) translateY(-8px);} to{opacity:1;transform:scale(1) translateY(0);} }
.modal h3 { margin-bottom:18px; color:var(--c-text); font-size:16px; font-weight:700; }
.modal-wide { width:660px; }

/* ----- Pagination ----- */
.pagination { margin-top:20px; display:flex; gap:4px; justify-content:center; align-items:center; }
.pagination a, .pagination span { padding:6px 13px; border-radius:var(--radius-sm); text-decoration:none; font-size:13px; font-weight:500; }
.pagination a { background:var(--c-bg); color:var(--c-muted); transition:all var(--transition); }
.pagination a:hover { background:var(--c-primary-light); color:var(--c-primary); }
.pagination .current { background:var(--c-primary); color:#fff; font-weight:600; }
.pagination .disabled { color:var(--c-faint); pointer-events:none; }

/* ----- Badges ----- */
.badge { display:inline-block; padding:2px 10px; border-radius:999px; font-size:11px; font-weight:600; letter-spacing:0.3px; }
.badge-success { background:var(--c-success-light); color:#064e3b; }
.badge-danger  { background:var(--c-danger-light);  color:#881337; }
.badge-warning { background:var(--c-warning-light); color:#78350f; }
.badge-info    { background:var(--c-info-light);    color:#0c4a6e; }

/* ----- Utilities ----- */
.masked      { color:var(--c-faint); font-style:italic; }
.text-muted  { color:var(--c-muted); }
.text-center { text-align:center; }
.text-right  { text-align:right; }
.mt-20 { margin-top:20px; }
.mb-20 { margin-bottom:20px; }
.hmac-valid   { color:var(--c-success); font-weight:600; }
.hmac-invalid { color:var(--c-danger);  font-weight:600; }
.error-wrap   { max-width:520px; margin:60px auto; background:#fff; padding:32px; border-radius:var(--radius); box-shadow:var(--shadow-md); text-align:center; border-left:4px solid var(--c-danger); }
.skip-link    { position:absolute; left:-9999px; }

/* ----- Login & Change Password ----- */
.center-layout {
  min-height:100vh; display:flex;
  background: linear-gradient(155deg, #f8fafc 0%, #f1f5f9 40%, #e2e8f0 100%);
}
.login-card {
  width:400px; margin:auto;
  background:#fff; padding:36px 32px; border-radius:var(--radius);
  box-shadow:var(--shadow-lg); border:1px solid var(--c-border);
}
.login-header { text-align:center; margin-bottom:28px; }
.login-header h1 { font-size:26px; font-weight:800; color:var(--c-text); letter-spacing:-0.6px; margin-bottom:6px; }
.login-header p  { font-size:14px; color:var(--c-muted); }
.login-btn {
  width:100%; padding:11px; margin-top:4px;
  background:var(--c-primary); color:#fff; border:none; border-radius:var(--radius-sm);
  font-size:15px; font-weight:600; cursor:pointer; font-family:inherit;
  transition:all var(--transition); letter-spacing:0.2px;
}
.login-btn:hover { background:var(--c-primary-hover); transform:translateY(-1px); box-shadow:0 4px 14px rgba(67,56,202,0.3); }
.footer-note { text-align:center; margin-top:22px; color:var(--c-faint); font-size:12px; }

/* ----- Responsive ----- */
@media (max-width: 768px) {
  .sidebar { display:none; }
  .main-content { margin-left:0; }
}
</style>

<script>
/* ===========================================================
   薪资管理系统 — 全局 JS
   =========================================================== */
function openModal(id){ var o=document.getElementById(id); if(o)o.classList.add('show'); }
function closeModal(id){ var o=document.getElementById(id); if(o)o.classList.remove('show'); }
function submitForm(action,params){
  var f=document.createElement('form'); f.method='post'; f.action=action;
  for(var k in params){ if(params.hasOwnProperty(k)){
    var i=document.createElement('input'); i.type='hidden'; i.name=k; i.value=params[k]; f.appendChild(i);
  }}
  document.body.appendChild(f); f.submit();
}
document.addEventListener('click',function(e){
  if(e.target.classList.contains('modal-overlay')) e.target.classList.remove('show');
});
</script>
