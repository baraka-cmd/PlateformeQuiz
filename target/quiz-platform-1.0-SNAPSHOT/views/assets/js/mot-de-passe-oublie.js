/**
 * ============================================================
 * FICHIER : views/assets/js/mot-de-passe-oublie.js
 * RÔLE    : Scripts de la page "Mot de passe oublié"
 *           - Validation en temps réel de l'email
 *           - Validation à la soumission
 *           - Gestion du loader du bouton
 *           - Empêche la double soumission
 *           - Feedback visuel immédiat
 *           - Navigation clavier optimisée
 *           - Animation des inputs
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initFormValidation();
        initFormSubmission();
        initInputAnimations();
        initAutoFocus();
    });

    /* ==========================================================
     SECTION 2 : RÉFÉRENCES AUX ÉLÉMENTS DU DOM
     ========================================================== */
    function getElements() {
        return {
            form: document.getElementById('formMdpOublie'),
            email: document.getElementById('email'),
            btnSubmit: document.getElementById('btnResetMdp'),
            btnLoader: document.getElementById('btnLoader'),
            emailStatus: document.getElementById('emailStatus'),
            emailError: document.getElementById('emailError')
        };
    }

    /* ==========================================================
     SECTION 3 : VALIDATION EN TEMPS RÉEL
     ========================================================== */
    function initFormValidation() {
        var els = getElements();
        if (!els.form) return;

        // Validation de l'email en temps réel
        if (els.email) {
            els.email.addEventListener('input', function() {
                validateEmailField(els);
            });

            els.email.addEventListener('blur', function() {
                validateEmailField(els);
            });
        }
    }

    /**
     * Validation du champ email
     */
    function validateEmailField(els) {
        var value = els.email.value.trim();
        var statusEl = els.emailStatus;
        var errorEl = els.emailError;
        var input = els.email;

        // Réinitialiser
        resetInputStatus(input, statusEl, errorEl);

        if (value.length === 0) {
            // Champ vide, ne rien afficher si pas encore soumis
            return;
        }

        // Validation du format email
        var emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(value)) {
            setInputError(input, statusEl, errorEl, 'Veuillez entrer une adresse email valide.');
            return;
        }

        // Email valide
        setInputValid(input, statusEl, 'Email valide');
    }

    /* ==========================================================
     SECTION 4 : SOUMISSION DU FORMULAIRE
     ========================================================== */
    function initFormSubmission() {
        var els = getElements();
        if (!els.form) return;

        var isSubmitting = false;

        els.form.addEventListener('submit', function(e) {
            // Empêcher la double soumission
            if (isSubmitting) {
                e.preventDefault();
                return false;
            }

            // Validation finale
            var errors = validateAllFields(els);
            
            if (errors.length > 0) {
                e.preventDefault();
                
                // Afficher les erreurs
                displaySubmissionErrors(els, errors);
                
                // Scroller vers l'erreur
                var firstError = document.querySelector('.form-group input.input-error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                }
                
                return false;
            }

            // Tout est OK
            isSubmitting = true;
            
            // Activer le loader
            activateLoader(els);
            
            // Timeout de sécurité (15 secondes pour l'envoi d'email)
            setTimeout(function() {
                if (isSubmitting) {
                    deactivateLoader(els);
                    isSubmitting = false;
                    
                    // Afficher un message si la requête prend trop de temps
                    showToast('L\'envoi prend plus de temps que prévu. Veuillez patienter...', 'warning', 5000);
                }
            }, 15000);

            return true;
        });
    }

    /**
     * Validation complète avant soumission
     */
    function validateAllFields(els) {
        var errors = [];

        // Validation email
        if (els.email) {
            var emailValue = els.email.value.trim();
            if (emailValue.length === 0) {
                errors.push({ field: 'email', message: 'Veuillez entrer votre adresse email.' });
                setInputError(els.email, els.emailStatus, els.emailError, 'Veuillez entrer votre adresse email.');
            } else {
                var emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
                if (!emailRegex.test(emailValue)) {
                    errors.push({ field: 'email', message: 'Adresse email invalide.' });
                    setInputError(els.email, els.emailStatus, els.emailError, 'Veuillez entrer une adresse email valide.');
                }
            }
        }

        return errors;
    }

    function displaySubmissionErrors(els, errors) {
        if (errors.length > 0) {
            console.warn('Erreurs de validation mot de passe oublié :', errors);
        }
    }

    /* ==========================================================
     SECTION 5 : GESTION DU LOADER
     ========================================================== */
    function activateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.add('loading');
            els.btnSubmit.disabled = true;
        }
        if (els.email) els.email.readOnly = true;
    }

    function deactivateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.remove('loading');
            els.btnSubmit.disabled = false;
        }
        if (els.email) els.email.readOnly = false;
    }

    /* ==========================================================
     SECTION 6 : FOCUS AUTOMATIQUE
     ========================================================== */
    function initAutoFocus() {
        var els = getElements();
        // Le focus est déjà sur l'email grâce à l'attribut autofocus
        // On s'assure que le curseur est à la fin du texte si pré-rempli
        if (els.email && els.email.value.length > 0) {
            var val = els.email.value;
            els.email.value = '';
            els.email.value = val;
        }
    }

    /* ==========================================================
     SECTION 7 : UTILITAIRES VISUELS
     ========================================================== */

    function setInputValid(input, statusEl, message) {
        if (!input) return;
        input.classList.remove('input-error');
        input.classList.add('input-valid');
        
        if (statusEl) {
            statusEl.innerHTML = '<i class="fas fa-check-circle"></i>';
            statusEl.className = 'input-status valid';
            statusEl.setAttribute('title', message || 'Champ valide');
        }

        var errorEl = input.closest('.form-group')?.querySelector('.form-error');
        if (errorEl) {
            errorEl.textContent = '';
        }
    }

    function setInputError(input, statusEl, errorEl, message) {
        if (!input) return;
        input.classList.remove('input-valid');
        input.classList.add('input-error');
        
        if (statusEl) {
            statusEl.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
            statusEl.className = 'input-status error';
            statusEl.setAttribute('title', message || 'Champ invalide');
        }

        if (errorEl && message) {
            errorEl.textContent = message;
        }
    }

    function resetInputStatus(input, statusEl, errorEl) {
        if (!input) return;
        input.classList.remove('input-valid', 'input-error');
        
        if (statusEl) {
            statusEl.innerHTML = '';
            statusEl.className = 'input-status';
            statusEl.removeAttribute('title');
        }

        if (errorEl) {
            errorEl.textContent = '';
        }
    }

    function showToast(message, type, duration) {
        if (window.showToast) {
            window.showToast(message, type, duration);
            return;
        }

        var toast = document.createElement('div');
        toast.style.cssText = 
            'position: fixed; bottom: 30px; right: 30px; padding: 14px 22px; border-radius: 12px; ' +
            'color: white; font-weight: 500; font-size: 14px; z-index: 99999; ' +
            'animation: slideDown 0.4s ease; max-width: 400px; box-shadow: 0 8px 30px rgba(0,0,0,0.2);';
        
        var colors = {
            success: 'linear-gradient(135deg, #27ae60, #2ecc71)',
            error: 'linear-gradient(135deg, #e74c3c, #c0392b)',
            warning: 'linear-gradient(135deg, #f39c12, #e67e22)',
            info: 'linear-gradient(135deg, #667eea, #764ba2)'
        };
        
        toast.style.background = colors[type] || colors.info;
        toast.textContent = message;
        
        document.body.appendChild(toast);
        
        setTimeout(function() {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100px)';
            toast.style.transition = 'all 0.4s ease';
            setTimeout(function() { toast.remove(); }, 400);
        }, duration || 4000);
    }

    /* ==========================================================
     SECTION 8 : ANIMATIONS DES INPUTS
     ========================================================== */
    function initInputAnimations() {
        var inputs = document.querySelectorAll('.form-group input[type="email"]');
        
        inputs.forEach(function(input) {
            // Effet de focus amélioré
            input.addEventListener('focus', function() {
                var wrapper = this.closest('.input-wrapper');
                if (wrapper) {
                    wrapper.style.transform = 'scale(1.01)';
                    wrapper.style.transition = 'transform 0.3s ease';
                }
            });

            input.addEventListener('blur', function() {
                var wrapper = this.closest('.input-wrapper');
                if (wrapper) {
                    wrapper.style.transform = 'scale(1)';
                }
            });

            // Détection de la touche Échap pour défocus
            input.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    this.blur();
                }
            });
        });
    }

    /* ==========================================================
     SECTION 9 : NAVIGATION CLAVIER
     ========================================================== */
    document.addEventListener('keydown', function(e) {
        // Si Enter est pressé sur l'input email, soumettre le formulaire
        if (e.key === 'Enter' && e.target.id === 'email') {
            var form = document.getElementById('formMdpOublie');
            if (form) {
                e.preventDefault();
                form.dispatchEvent(new Event('submit', { cancelable: true }));
            }
        }
    });

})();