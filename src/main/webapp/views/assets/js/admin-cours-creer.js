// ============================================================
// FICHIER : views/assets/js/admin-cours-creer.js
// RÔLE    : Script du formulaire création cours
//           - Prévisualisation en temps réel
//           - Validation avancée
//           - Compteurs de caractères
//           - Raccourcis clavier
//           - Messages toast
// ============================================================

(function() {
    'use strict';

    // ==========================================================
    // 1. CONFIGURATION
    // ==========================================================
    var CONFIG = {
        titreMinLength: 5,
        titreMaxLength: 200,
        descMaxLength: 500,
        objectifsMaxLength: 500,
        prerequisMaxLength: 300,
        toastDuration: 4000
    };

    // ==========================================================
    // 2. ÉTAT GLOBAL
    // ==========================================================
    var state = {
        isDirty: false,
        isSubmitting: false
    };

    // ==========================================================
    // 3. INITIALISATION
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Formulaire cours - Initialisé');
    });

    function initAll() {
        initLivePreview();
        initCharacterCounters();
        initFormValidation();
        initKeyboardShortcuts();
        initBeforeUnloadWarning();
        initTooltips();
        initSelectEnhancements();
    }

    // ==========================================================
    // 4. PRÉVISUALISATION EN TEMPS RÉEL
    // ==========================================================
    function initLivePreview() {
        var titreInput = document.getElementById('titre');
        var themeSelect = document.getElementById('theme');
        var niveauSelect = document.getElementById('niveau');
        var descInput = document.getElementById('description');
        var objectifsInput = document.getElementById('objectifs');
        var prerequisInput = document.getElementById('prerequis');
        var dureeInput = document.getElementById('dureeEstimee');
        var imageInput = document.getElementById('image');

        // Titre
        if (titreInput) {
            titreInput.addEventListener('input', function() {
                var titre = this.value.trim() || 'Titre du cours';
                document.getElementById('previewTitre').textContent = titre;
                state.isDirty = true;
            });
        }

        // Thème + Niveau
        if (themeSelect) {
            themeSelect.addEventListener('change', function() {
                updatePreviewMeta();
                updatePreviewThemeColor();
                state.isDirty = true;
            });
        }

        if (niveauSelect) {
            niveauSelect.addEventListener('change', function() {
                updatePreviewMeta();
                updatePreviewThemeColor();
                state.isDirty = true;
            });
        }

        // Description
        if (descInput) {
            descInput.addEventListener('input', function() {
                var desc = this.value.trim() || 'Description du cours apparaîtra ici';
                if (desc.length > 100) desc = desc.substring(0, 100) + '...';
                document.getElementById('previewDesc').textContent = desc;
                state.isDirty = true;
            });
        }

        // Durée
        if (dureeInput) {
            dureeInput.addEventListener('input', function() {
                var duree = this.value.trim();
                var previewDuree = document.getElementById('previewDuree');
                if (previewDuree) {
                    previewDuree.innerHTML = '<i class="fas fa-clock"></i> ' + (duree || 'Durée estimée');
                }
                state.isDirty = true;
            });
        }

        // Image (mise à jour de l'icône de prévisualisation si URL fournie)
        if (imageInput) {
            imageInput.addEventListener('input', function() {
                state.isDirty = true;
            });
        }

        // Initialiser
        updatePreviewMeta();
    }

    function updatePreviewMeta() {
        var themeSelect = document.getElementById('theme');
        var niveauSelect = document.getElementById('niveau');
        var previewTheme = document.getElementById('previewTheme');

        if (!previewTheme) return;

        var themeText = themeSelect && themeSelect.options[themeSelect.selectedIndex] 
            ? themeSelect.options[themeSelect.selectedIndex].text 
            : 'Thème';
        var niveauText = niveauSelect && niveauSelect.options[niveauSelect.selectedIndex] 
            ? niveauSelect.options[niveauSelect.selectedIndex].text 
            : 'Niveau';

        previewTheme.textContent = themeText + ' • ' + niveauText;
    }

    function updatePreviewThemeColor() {
        var themeSelect = document.getElementById('theme');
        var previewIcon = document.getElementById('previewIcon');
        if (!themeSelect || !previewIcon) return;

        var selectedOption = themeSelect.options[themeSelect.selectedIndex];
        if (selectedOption && selectedOption.value) {
            // Animation subtile
            previewIcon.style.transform = 'scale(1.1)';
            setTimeout(function() {
                previewIcon.style.transform = 'scale(1)';
            }, 200);
        }
    }

    // ==========================================================
    // 5. COMPTEURS DE CARACTÈRES
    // ==========================================================
    function initCharacterCounters() {
        setupCounter('titre', 'titreCount', 'titreCounter', CONFIG.titreMaxLength);
        setupCounter('description', 'descCount', 'descCounter', CONFIG.descMaxLength);
        setupCounter('objectifs', null, null, CONFIG.objectifsMaxLength);
        setupCounter('prerequis', null, null, CONFIG.prerequisMaxLength);
    }

    function setupCounter(inputId, countId, counterId, maxLength) {
        var input = document.getElementById(inputId);
        var countEl = countId ? document.getElementById(countId) : null;
        var counterEl = counterId ? document.getElementById(counterId) : null;

        if (!input) return;

        if (countEl) countEl.textContent = input.value.length;

        input.addEventListener('input', function() {
            var length = this.value.length;
            if (countEl) countEl.textContent = length;
            if (counterEl) {
                var ratio = length / maxLength;
                counterEl.style.color = ratio > 0.9 ? '#e74c3c' : ratio > 0.7 ? '#f39c12' : '#8b8fa3';
            }
        });
    }

    // ==========================================================
    // 6. VALIDATION DU FORMULAIRE
    // ==========================================================
    function initFormValidation() {
        var form = document.getElementById('formCoursCreer');
        if (!form) return;

        form.addEventListener('submit', function(e) {
            var isValid = validateForm(true);
            if (!isValid) {
                e.preventDefault();
                scrollToFirstError();
            } else {
                state.isSubmitting = true;
                var submitBtn = document.getElementById('btnCreerCours');
                if (submitBtn) {
                    submitBtn.disabled = true;
                    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Création en cours...</span>';
                }
            }
        });

        // Validation en temps réel au blur
        var titreInput = document.getElementById('titre');
        if (titreInput) {
            titreInput.addEventListener('blur', function() { validateTitre(false); });
            titreInput.addEventListener('input', function() {
                if (this.classList.contains('acc-input-error')) validateTitre(false);
            });
        }
    }

    function validateForm(showErrors) {
        var isValid = true;
        if (!validateTheme(showErrors)) isValid = false;
        if (!validateNiveau(showErrors)) isValid = false;
        if (!validateTitre(showErrors)) isValid = false;
        return isValid;
    }

    function validateTheme(showErrors) {
        var select = document.getElementById('theme');
        var error = document.getElementById('themeError');
        var valid = select && select.value !== '';

        if (!valid && showErrors) {
            if (select) select.classList.add('acc-input-error');
            if (error) error.textContent = 'Veuillez sélectionner un thème.';
        } else {
            if (select) select.classList.remove('acc-input-error');
            if (error) error.textContent = '';
        }
        return valid;
    }

    function validateNiveau(showErrors) {
        var select = document.getElementById('niveau');
        var valid = select && select.value !== '';

        if (!valid && showErrors) {
            if (select) select.classList.add('acc-input-error');
        } else {
            if (select) select.classList.remove('acc-input-error');
        }
        return valid;
    }

    function validateTitre(showErrors) {
        var input = document.getElementById('titre');
        var error = document.getElementById('titreError');
        if (!input) return true;

        var valeur = input.value.trim();
        var valid = true;

        if (!valeur) {
            valid = false;
            if (showErrors) {
                input.classList.add('acc-input-error');
                if (error) error.textContent = 'Le titre du cours est obligatoire.';
            }
        } else if (valeur.length < CONFIG.titreMinLength) {
            valid = false;
            if (showErrors) {
                input.classList.add('acc-input-error');
                if (error) error.textContent = 'Le titre doit contenir au moins ' + CONFIG.titreMinLength + ' caractères.';
            }
        } else if (valeur.length > CONFIG.titreMaxLength) {
            valid = false;
            if (showErrors) {
                input.classList.add('acc-input-error');
                if (error) error.textContent = 'Le titre ne peut pas dépasser ' + CONFIG.titreMaxLength + ' caractères.';
            }
        } else {
            input.classList.remove('acc-input-error');
            if (error) error.textContent = '';
        }

        return valid;
    }

    function scrollToFirstError() {
        var firstError = document.querySelector('.acc-input-error');
        if (firstError) {
            firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            firstError.focus();
        }
    }

    // ==========================================================
    // 7. AMÉLIORATION DES SELECT
    // ==========================================================
    function initSelectEnhancements() {
        var selects = document.querySelectorAll('.acc-select');
        selects.forEach(function(select) {
            // Ajouter une classe si une valeur est sélectionnée
            select.addEventListener('change', function() {
                if (this.value) {
                    this.classList.add('has-value');
                } else {
                    this.classList.remove('has-value');
                }
            });

            // Initialiser
            if (select.value) select.classList.add('has-value');
        });
    }

    // ==========================================================
    // 8. RACCOURCIS CLAVIER
    // ==========================================================
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ctrl+S : Sauvegarder
            if ((e.ctrlKey || e.metaKey) && e.key === 's') {
                e.preventDefault();
                var form = document.getElementById('formCoursCreer');
                if (form && validateForm(true)) {
                    form.submit();
                }
            }

            // Échap : Retour à la liste
            if (e.key === 'Escape' && !document.activeElement.closest('input, textarea, select')) {
                var cancelBtn = document.querySelector('.acc-btn-secondary');
                if (cancelBtn) window.location.href = cancelBtn.getAttribute('href');
            }
        });
    }

    // ==========================================================
    // 9. AVERTISSEMENT AVANT DE QUITTER
    // ==========================================================
    function initBeforeUnloadWarning() {
        window.addEventListener('beforeunload', function(e) {
            if (state.isDirty && !state.isSubmitting) {
                var message = 'Vous avez des modifications non sauvegardées. Voulez-vous vraiment quitter ?';
                e.returnValue = message;
                return message;
            }
        });
    }

    // ==========================================================
    // 10. TOOLTIPS
    // ==========================================================
    function initTooltips() {
        var tooltipElements = document.querySelectorAll('[title]');
        var currentTooltip = null;

        tooltipElements.forEach(function(element) {
            var title = element.getAttribute('title');
            if (!title) return;

            element.addEventListener('mouseenter', function(e) {
                showTooltip(element, title);
            });

            element.addEventListener('mouseleave', function() {
                hideTooltip();
            });

            element.addEventListener('click', function() {
                hideTooltip();
            });
        });

        function showTooltip(element, text) {
            hideTooltip();

            var tooltip = document.createElement('div');
            tooltip.className = 'acc-custom-tooltip';
            tooltip.textContent = text;
            tooltip.setAttribute('role', 'tooltip');
            tooltip.style.cssText = [
                'position: fixed;',
                'z-index: 9999;',
                'background: #1a1a2e;',
                'color: white;',
                'padding: 7px 14px;',
                'border-radius: 8px;',
                'font-size: 11px;',
                'font-weight: 500;',
                'font-family: "Poppins", sans-serif;',
                'white-space: nowrap;',
                'pointer-events: none;',
                'box-shadow: 0 6px 20px rgba(0,0,0,0.25);',
                'opacity: 0;',
                'transform: translateY(4px);',
                'transition: opacity 0.2s ease, transform 0.2s ease;'
            ].join(' ');

            document.body.appendChild(tooltip);

            var rect = element.getBoundingClientRect();
            var tooltipRect = tooltip.getBoundingClientRect();
            var top = rect.top - tooltipRect.height - 8;
            var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

            if (left < 12) left = 12;
            if (left + tooltipRect.width > window.innerWidth - 12) {
                left = window.innerWidth - tooltipRect.width - 12;
            }
            if (top < 12) top = rect.bottom + 8;

            tooltip.style.top = top + 'px';
            tooltip.style.left = left + 'px';

            requestAnimationFrame(function() {
                tooltip.style.opacity = '1';
                tooltip.style.transform = 'translateY(0)';
            });

            currentTooltip = tooltip;
        }

        function hideTooltip() {
            if (currentTooltip) {
                currentTooltip.style.opacity = '0';
                currentTooltip.style.transform = 'translateY(4px)';
                var ref = currentTooltip;
                setTimeout(function() {
                    if (ref.parentNode) ref.parentNode.removeChild(ref);
                }, 200);
                currentTooltip = null;
            }
        }

        window.addEventListener('scroll', function() { hideTooltip(); }, { passive: true });
    }

    // ==========================================================
    // 11. NOTIFICATION TOAST
    // ==========================================================
    function showToast(message, type) {
        type = type || 'info';

        var icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            info: 'fa-info-circle',
            warning: 'fa-exclamation-triangle'
        };

        var colors = {
            success: '#27ae60',
            error: '#e74c3c',
            info: '#667eea',
            warning: '#f39c12'
        };

        var existingToasts = document.querySelectorAll('.acc-toast');
        existingToasts.forEach(function(t) { t.remove(); });

        var toast = document.createElement('div');
        toast.className = 'acc-toast';
        toast.setAttribute('role', 'alert');
        toast.style.cssText = [
            'position: fixed;',
            'bottom: 30px;',
            'right: 30px;',
            'z-index: 9999;',
            'background: white;',
            'padding: 14px 20px;',
            'border-radius: 12px;',
            'box-shadow: 0 10px 40px rgba(0,0,0,0.15);',
            'display: flex;',
            'align-items: center;',
            'gap: 10px;',
            'font-size: 13px;',
            'font-weight: 500;',
            'font-family: "Poppins", sans-serif;',
            'max-width: 400px;',
            'animation: accToastIn 0.4s ease;',
            'border-left: 4px solid ' + (colors[type] || '#667eea') + ';'
        ].join(' ');

        toast.innerHTML = [
            '<i class="fas ' + (icons[type] || 'fa-info-circle') + '"',
            'style="font-size: 18px; color: ' + (colors[type] || '#667eea') + '; flex-shrink: 0;"></i>',
            '<span style="flex: 1; color: #1a1a2e;">' + message + '</span>'
        ].join('');

        document.body.appendChild(toast);

        setTimeout(function() {
            if (toast.parentNode) {
                toast.style.transition = 'all 0.3s ease';
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(40px)';
                setTimeout(function() {
                    if (toast.parentNode) toast.parentNode.removeChild(toast);
                }, 300);
            }
        }, CONFIG.toastDuration);
    }

    // Injecter l'animation du toast
    if (!document.getElementById('acc-toast-style')) {
        var style = document.createElement('style');
        style.id = 'acc-toast-style';
        style.textContent = '@keyframes accToastIn { from { opacity: 0; transform: translateX(50px); } to { opacity: 1; transform: translateX(0); } }';
        document.head.appendChild(style);
    }

    // ==========================================================
    // 12. UTILITAIRES
    // ==========================================================
    function log(message) {
        if (window.console && console.log) {
            console.log('%c[Admin Cours Créer] %c' + message,
                'color: #667eea; font-weight: 700;',
                'color: #555;');
        }
    }

})();