// ============================================================
// FICHIER : views/assets/js/admin-cours-lecons.js
// RÔLE    : Script de la page liste des leçons
//           - Messages flash auto-fermants
//           - Animations des lignes du tableau
//           - Raccourcis clavier
//           - Tooltips
// ============================================================

(function() {
    'use strict';

    var CONFIG = {
        messageTimeout: 6000,
        messageFadeDuration: 400,
        tooltipOffset: 10,
        keyboardShortcuts: true
    };

    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Admin Leçons - Interface initialisée');
    });

    function initAll() {
        initFlashMessages();
        initTableRowAnimations();
        initTooltips();
        initKeyboardShortcuts();
    }

    // Messages flash
    function initFlashMessages() {
        document.querySelectorAll('.message-alerte').forEach(function(msg, i) {
            var closeBtn = document.createElement('button');
            closeBtn.innerHTML = '<i class="fas fa-times"></i>';
            closeBtn.setAttribute('aria-label', 'Fermer');
            closeBtn.style.cssText = 'position:absolute;right:12px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;padding:6px 8px;border-radius:50%;font-size:13px;color:inherit;opacity:0.6;transition:all 0.2s ease;line-height:1;';
            closeBtn.addEventListener('mouseenter', function() { this.style.opacity = '1'; this.style.background = 'rgba(0,0,0,0.05)'; });
            closeBtn.addEventListener('mouseleave', function() { this.style.opacity = '0.6'; this.style.background = 'none'; });
            closeBtn.addEventListener('click', function(e) { e.stopPropagation(); dismiss(msg); });
            msg.style.position = 'relative'; msg.style.paddingRight = '45px'; msg.appendChild(closeBtn);

            msg.style.opacity = '0'; msg.style.transform = 'translateY(-15px)';
            setTimeout(function() {
                msg.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                msg.style.opacity = '1'; msg.style.transform = 'translateY(0)';
            }, 50 + (i * 80));

            var timeout = parseInt(msg.getAttribute('data-timeout')) || CONFIG.messageTimeout;
            if (timeout > 0) setTimeout(function() { dismiss(msg); }, timeout);
        });
    }

    function dismiss(msg) {
        if (!msg || msg.classList.contains('dismissing')) return;
        msg.classList.add('dismissing');
        msg.style.transition = 'all ' + (CONFIG.messageFadeDuration / 1000) + 's ease';
        msg.style.opacity = '0'; msg.style.transform = 'translateX(25px)';
        msg.style.maxHeight = msg.offsetHeight + 'px';
        setTimeout(function() {
            msg.style.maxHeight = '0'; msg.style.margin = '0'; msg.style.padding = '0'; msg.style.borderWidth = '0'; msg.style.overflow = 'hidden';
        }, CONFIG.messageFadeDuration * 0.6);
        setTimeout(function() { if (msg.parentNode) msg.parentNode.removeChild(msg); }, CONFIG.messageFadeDuration + 200);
    }

    // Animations
    function initTableRowAnimations() {
        var rows = document.querySelectorAll('.acl-table tbody tr');
        if (!rows.length) return;
        if ('IntersectionObserver' in window) {
            var obs = new IntersectionObserver(function(entries) {
                entries.forEach(function(e) { if (e.isIntersecting) { animate(e.target); obs.unobserve(e.target); } });
            }, { threshold: 0.1, rootMargin: '0px 0px -30px 0px' });
            rows.forEach(function(r) { r.style.opacity = '0'; r.style.transform = 'translateY(10px)'; obs.observe(r); });
        } else {
            rows.forEach(function(r, i) {
                r.style.opacity = '0'; r.style.transform = 'translateY(10px)';
                setTimeout(function() { animate(r); }, 30 * i);
            });
        }
    }

    function animate(row) {
        row.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        row.style.opacity = '1'; row.style.transform = 'translateY(0)';
    }

    // Tooltips
    function initTooltips() {
        var current = null;
        document.querySelectorAll('[title]').forEach(function(el) {
            var title = el.getAttribute('title'); if (!title) return;
            el.addEventListener('mouseenter', function() { show(el, title); });
            el.addEventListener('mouseleave', hide);
            el.addEventListener('click', hide);
        });

        function show(el, text) {
            hide();
            var tt = document.createElement('div');
            tt.textContent = text;
            tt.style.cssText = 'position:fixed;z-index:9999;background:#1a1a2e;color:white;padding:7px 14px;border-radius:8px;font-size:11px;font-weight:500;font-family:"Poppins",sans-serif;white-space:nowrap;pointer-events:none;box-shadow:0 6px 20px rgba(0,0,0,0.25);opacity:0;transform:translateY(4px);transition:opacity 0.2s ease,transform 0.2s ease;';
            document.body.appendChild(tt);
            var r = el.getBoundingClientRect(), tr = tt.getBoundingClientRect();
            var top = r.top - tr.height - CONFIG.tooltipOffset;
            var left = r.left + (r.width / 2) - (tr.width / 2);
            if (left < 12) left = 12;
            if (left + tr.width > window.innerWidth - 12) left = window.innerWidth - tr.width - 12;
            if (top < 12) top = r.bottom + CONFIG.tooltipOffset;
            tt.style.top = top + 'px'; tt.style.left = left + 'px';
            requestAnimationFrame(function() { tt.style.opacity = '1'; tt.style.transform = 'translateY(0)'; });
            current = tt;
        }

        function hide() {
            if (current) { current.style.opacity = '0'; current.style.transform = 'translateY(4px)'; var ref = current; setTimeout(function() { if (ref.parentNode) ref.parentNode.removeChild(ref); }, 200); current = null; }
        }
        window.addEventListener('scroll', hide, { passive: true });
    }

    // Raccourcis
    function initKeyboardShortcuts() {
        if (!CONFIG.keyboardShortcuts) return;
        document.addEventListener('keydown', function(e) {
            var ae = document.activeElement;
            if (ae && (ae.tagName === 'INPUT' || ae.tagName === 'TEXTAREA' || ae.tagName === 'SELECT' || ae.isContentEditable)) return;
            if (e.key === 'Escape') { var b = document.querySelector('.acl-btn-retour'); if (b) window.location.href = b.getAttribute('href'); }
        });
    }

    function log(m) { if (window.console) console.log('%c[Admin Leçons] %c' + m, 'color: #667eea; font-weight: 700;', 'color: #555;'); }

})();