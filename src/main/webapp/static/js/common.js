/* ============================================
   薪资管理系统 — 全局共享 JavaScript
   ============================================ */

/**
 * 打开弹窗
 * @param {string} modalId - modal-overlay 元素的 ID
 */
function openModal(modalId) {
    var overlay = document.getElementById(modalId);
    if (overlay) overlay.classList.add('show');
}

/**
 * 关闭弹窗
 * @param {string} modalId - modal-overlay 元素的 ID
 */
function closeModal(modalId) {
    var overlay = document.getElementById(modalId);
    if (overlay) overlay.classList.remove('show');
}

/**
 * 动态创建表单并提交（用于删除等操作）
 * @param {string} action - 表单 action URL
 * @param {Object} params - 要提交的键值对
 */
function submitForm(action, params) {
    var form = document.createElement('form');
    form.method = 'post';
    form.action = action;
    for (var key in params) {
        if (params.hasOwnProperty(key)) {
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = key;
            input.value = params[key];
            form.appendChild(input);
        }
    }
    document.body.appendChild(form);
    form.submit();
}

/**
 * 点击遮罩层关闭弹窗
 */
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('show');
    }
});

/**
 * 在弹窗打开时阻止 body 滚动
 */
var _modalObserver = new MutationObserver(function() {
    var anyOpen = document.querySelector('.modal-overlay.show');
    document.body.style.overflow = anyOpen ? 'hidden' : '';
});
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.modal-overlay').forEach(function(ov) {
        _modalObserver.observe(ov, { attributes: true, attributeFilter: ['class'] });
    });
});
