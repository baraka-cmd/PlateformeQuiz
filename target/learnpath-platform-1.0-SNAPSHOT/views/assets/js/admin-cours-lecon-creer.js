// ============================================================
// FICHIER : views/assets/js/admin-cours-lecon-creer.js
// RÔLE    : Script du formulaire création/modification leçon
//           - Switch éditeur/aperçu
//           - Insertion de balises HTML
//           - Prévisualisation en temps réel
//           - Validation
//           - Raccourcis clavier
//           - Messages toast
// ============================================================

(function() {
    'use strict';

    var CONFIG = {
        toastDuration: 4000
    };

    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Formulaire leçon - Initialisé');
    });

    function initAll() {
        initKeyboardShortcuts();
        initAutoPreview();
    }

    // ===== SWITCH ÉDITER / APERÇU =====
    window.switchTab = function(tab) {
        document.querySelectorAll('.acle-tab').forEach(function(t) { t.classList.remove('active'); });
        var tabBtn = document.querySelector('.acle-tab[data-tab="' + tab + '"]');
        if (tabBtn) tabBtn.classList.add('active');

        if (tab === 'editor') {
            var editorArea = document.getElementById('editorArea');
            var editorToolbar = document.getElementById('editorToolbar');
            var previewArea = document.getElementById('previewArea');
            if (editorArea) editorArea.style.display = 'block';
            if (editorToolbar) editorToolbar.style.display = 'flex';
            if (previewArea) previewArea.style.display = 'none';
        } else {
            var editorArea2 = document.getElementById('editorArea');
            var editorToolbar2 = document.getElementById('editorToolbar');
            var previewArea2 = document.getElementById('previewArea');
            var previewContent = document.getElementById('previewContent');
            var contenuTexte = document.getElementById('contenuTexte');
            if (editorArea2) editorArea2.style.display = 'none';
            if (editorToolbar2) editorToolbar2.style.display = 'none';
            if (previewArea2) previewArea2.style.display = 'block';
            if (previewContent && contenuTexte) {
                previewContent.innerHTML = contenuTexte.value || "<p style='color: #8b8fa3; font-style: italic;'>L'aperçu du contenu apparaîtra ici...</p>";
            }
        }
    };

    // ===== INSERTION DE BALISES HTML =====
    window.insertTag = function(tag) {
        var textarea = document.getElementById('contenuTexte');
        if (!textarea) return;

        var start = textarea.selectionStart;
        var end = textarea.selectionEnd;
        var selectedText = textarea.value.substring(start, end);
        var replacement = '';

        switch (tag) {
            case 'strong': replacement = '<strong>' + (selectedText || 'texte en gras') + '</strong>'; break;
            case 'em': replacement = '<em>' + (selectedText || 'texte en italique') + '</em>'; break;
            case 'h3': replacement = '\n<h3>' + (selectedText || 'Titre de section') + '</h3>\n'; break;
            case 'p': replacement = '\n<p>' + (selectedText || 'Nouveau paragraphe') + '</p>\n'; break;
            case 'ul': replacement = '\n<ul>\n  <li>' + (selectedText || 'Élément 1') + '</li>\n  <li>Élément 2</li>\n</ul>\n'; break;
            case 'ol': replacement = '\n<ol>\n  <li>' + (selectedText || 'Élément 1') + '</li>\n  <li>Élément 2</li>\n</ol>\n'; break;
            case 'li': replacement = '  <li>' + (selectedText || 'Élément') + '</li>\n'; break;
            case 'pre': replacement = '\n<pre><code>' + (selectedText || '// Votre code ici') + '</code></pre>\n'; break;
            case 'a': replacement = '<a href="' + (selectedText || '#') + '">' + (selectedText || 'Texte du lien') + '</a>'; break;
            default: replacement = selectedText;
        }

        textarea.value = textarea.value.substring(0, start) + replacement + textarea.value.substring(end);
        textarea.focus();
        var newPos = start + replacement.length;
        textarea.selectionStart = newPos;
        textarea.selectionEnd = newPos;
    };

    // ===== PRÉVISUALISATION AUTO =====
    function initAutoPreview() {
        var ordreInput = document.getElementById('ordre');
        var previewOrdre = document.getElementById('previewOrdre');
        var radios = document.querySelectorAll('input[name="acces"]');
        var previewAcces = document.getElementById('previewAcces');

        if (ordreInput && previewOrdre) {
            ordreInput.addEventListener('input', function() {
                previewOrdre.textContent = this.value || '—';
            });
        }

        if (radios.length && previewAcces) {
            radios.forEach(function(radio) {
                radio.addEventListener('change', function() {
                    previewAcces.textContent = this.value === 'gratuit' ? 'Gratuit' : 'Premium';
                    document.querySelectorAll('.acle-acces-option').forEach(function(opt) { opt.classList.remove('active'); });
                    var parent = this.closest('.acle-acces-option');
                    if (parent) parent.classList.add('active');
                });
            });
        }
    }

    // ===== RACCOURCIS CLAVIER =====
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ctrl+S : Sauvegarder
            if ((e.ctrlKey || e.metaKey) && e.key === 's') {
                e.preventDefault();
                var form = document.getElementById('formLecon');
                if (form) {
                    var titre = document.getElementById('titre');
                    if (titre && titre.value.trim().length >= 2) {
                        form.submit();
                    } else {
                        showToast('Veuillez remplir le titre avant d\'enregistrer.', 'warning');
                    }
                }
            }

            // Échap : Annuler
            if (e.key === 'Escape' && !document.activeElement.closest('input, textarea, select')) {
                var cancelBtn = document.querySelector('.acle-btn-secondary');
                if (cancelBtn) window.location.href = cancelBtn.getAttribute('href');
            }
        });
    }

    // ===== TOAST =====
    function showToast(message, type) {
        type = type || 'info';
        var icons = { success: 'fa-check-circle', error: 'fa-exclamation-circle', info: 'fa-info-circle', warning: 'fa-exclamation-triangle' };
        var colors = { success: '#27ae60', error: '#e74c3c', info: '#667eea', warning: '#f39c12' };

        var existing = document.querySelectorAll('.acle-toast');
        existing.forEach(function(t) { t.remove(); });

        var toast = document.createElement('div');
        toast.className = 'acle-toast';
        toast.style.cssText = [
            'position: fixed; bottom: 30px; right: 30px; z-index: 9999; background: white; padding: 14px 20px;',
            'border-radius: 12px; box-shadow: 0 10px 40px rgba(0,0,0,0.15); display: flex; align-items: center;',
            'gap: 10px; font-size: 13px; font-weight: 500; font-family: "Poppins", sans-serif; max-width: 400px;',
            'animation: acleToastIn 0.4s ease; border-left: 4px solid ' + (colors[type] || '#667eea') + ';'
        ].join(' ');

        toast.innerHTML = '<i class="fas ' + (icons[type] || 'fa-info-circle') + '" style="font-size: 18px; color: ' + (colors[type] || '#667eea') + '; flex-shrink: 0;"></i><span style="flex: 1; color: #1a1a2e;">' + message + '</span>';
        document.body.appendChild(toast);

        setTimeout(function() {
            if (toast.parentNode) {
                toast.style.transition = 'all 0.3s ease'; toast.style.opacity = '0'; toast.style.transform = 'translateX(40px)';
                setTimeout(function() { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 300);
            }
        }, CONFIG.toastDuration);
    }

    // Injecter animation toast
    if (!document.getElementById('acle-toast-style')) {
        var style = document.createElement('style');
        style.id = 'acle-toast-style';
        style.textContent = '@keyframes acleToastIn { from { opacity: 0; transform: translateX(50px); } to { opacity: 1; transform: translateX(0); } }';
        document.head.appendChild(style);
    }

    function log(msg) {
        if (window.console) console.log('%c[Admin Leçon] %c' + msg, 'color: #667eea; font-weight: 700;', 'color: #555;');
    }

})();