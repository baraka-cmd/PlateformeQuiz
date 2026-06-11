/**
 * ============================================================
 * FICHIER : views/assets/js/changer-mot-de-passe.js
 * RÔLE    : Scripts de la page "Changer mon mot de passe"
 *           - Validation en temps réel des champs
 *           - Indicateur de force du mot de passe
 *           - Vérification correspondance mots de passe
 *           - Toggle affichage mot de passe (3 boutons)
 *           - Validation à la soumission
 *           - Gestion du loader du bouton
 *           - Empêche la double soumission
 *           - Feedback visuel immédiat
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initFormValidation();
        initPasswordStrength();
        initPasswordMatch();
        initTogglePasswords();
        initFormSubmission();
    });

    /* ==========================================================
     SECTION 2 : RÉFÉRENCES AUX ÉLÉMENTS DU DOM
     ========================================================== */
    function getElements() {
        return {
            form: document.getElementById('formChangerMdp'),
            ancienMotDePasse: document.getElementById('ancienMotDePasse'),
            nouveauMotDePasse: document.getElementById('nouveauMotDePasse'),
            confirmation: document.getElementById('confirmation'),
            btnSubmit: document.getElementById('btnChangerMdp'),
            btnLoader: document.getElementById('btnLoader'),
            ancienError: document.getElementById('ancienError'),
            strengthBar: document.getElementById('strengthBar'),
            strengthText: document.getElementById('strengthText'),
            messageCorrespondance: document.getElementById('messageCorrespondance'),
            // Exigences
            reqLength: document.getElementById('reqLength'),
            reqUppercase: document.getElementById('reqUppercase'),
            reqLowercase: document.getElementById('reqLowercase'),
            reqNumber: document.getElementById('reqNumber'),
            reqSpecial: document.getElementById('reqSpecial'),
            passwordRequirements: document.getElementById('passwordRequirements'),
            passwordStrengthContainer: document.getElementById('passwordStrengthContainer'),
            // Toggle boutons
            toggleAncien: document.getElementById('toggleAncien'),
            toggleNouveau: document.getElementById('toggleNouveau'),
            toggleConfirm: document.getElementById('toggleConfirm')
        };
    }

    /* ==========================================================
     SECTION 3 : VALIDATION EN TEMPS RÉEL
     ========================================================== */
    function initFormValidation() {
        var els = getElements();
        if (!els.form) return;

        // Validation de l'ancien mot de passe
        if (els.ancienMotDePasse) {
            els.ancienMotDePasse.addEventListener('input', function() {
                validateAncienField(els);
            });
            els.ancienMotDePasse.addEventListener('blur', function() {
                validateAncienField(els);
            });
        }

        // Validation du nouveau mot de passe
        if (els.nouveauMotDePasse) {
            els.nouveauMotDePasse.addEventListener('input', function() {
                updatePasswordStrength(els);
                if (els.confirmation && els.confirmation.value.length > 0) {
                    checkPasswordMatch(els);
                }
            });
        }

        // Validation de la confirmation
        if (els.confirmation) {
            els.confirmation.addEventListener('input', function() {
                checkPasswordMatch(els);
            });
        }
    }

    /**
     * Validation de l'ancien mot de passe
     */
    function validateAncienField(els) {
        var value = els.ancienMotDePasse.value;
        var errorEl = els.ancienError;
        var input = els.ancienMotDePasse;

        resetInputStatus(input, null, errorEl);

        if (value.length === 0) return;

        if (value.length < 6) {
            setInputError(input, null, errorEl, 'Mot de passe trop court (minimum 6 caractères).');
            return;
        }

        setInputValid(input, null);
    }

    /* ==========================================================
     SECTION 4 : FORCE DU MOT DE PASSE
     ========================================================== */
    function initPasswordStrength() {
        var els = getElements();
        if (!els.nouveauMotDePasse) return;

        els.nouveauMotDePasse.addEventListener('focus', function() {
            if (els.passwordRequirements) {
                els.passwordRequirements.style.display = 'flex';
            }
            if (els.passwordStrengthContainer) {
                els.passwordStrengthContainer.style.display = 'block';
            }
        });
    }

    function updatePasswordStrength(els) {
        var motDePasse = els.nouveauMotDePasse.value;
        var score = 0;

        // Critère 1 : Longueur >= 8
        if (motDePasse.length >= 8) {
            score += 20;
            markRequirement(els.reqLength, true);
        } else {
            markRequirement(els.reqLength, false);
        }

        // Critère 2 : Longueur >= 12
        if (motDePasse.length >= 12) {
            score += 10;
        }

        // Critère 3 : Au moins une majuscule
        if (/[A-Z]/.test(motDePasse)) {
            score += 20;
            markRequirement(els.reqUppercase, true);
        } else {
            markRequirement(els.reqUppercase, false);
        }

        // Critère 4 : Au moins une minuscule
        if (/[a-z]/.test(motDePasse)) {
            score += 20;
            markRequirement(els.reqLowercase, true);
        } else {
            markRequirement(els.reqLowercase, false);
        }

        // Critère 5 : Au moins un chiffre
        if (/[0-9]/.test(motDePasse)) {
            score += 15;
            markRequirement(els.reqNumber, true);
        } else {
            markRequirement(els.reqNumber, false);
        }

        // Critère 6 : Au moins un caractère spécial
        if (/[!@#$%^&*(),.?":{}|<>_\-+=\[\]\\;:'/`~]/.test(motDePasse)) {
            score += 15;
            markRequirement(els.reqSpecial, true);
        } else {
            markRequirement(els.reqSpecial, false);
        }

        // Mise à jour de la barre de force
        if (els.strengthBar) {
            els.strengthBar.style.width = score + '%';

            if (score <= 25) {
                els.strengthBar.style.backgroundColor = '#e74c3c';
            } else if (score <= 50) {
                els.strengthBar.style.backgroundColor = '#f39c12';
            } else if (score <= 75) {
                els.strengthBar.style.backgroundColor = '#2ecc71';
            } else {
                els.strengthBar.style.backgroundColor = '#27ae60';
            }
        }

        // Texte de force
        if (els.strengthText) {
            var text = '';
            var color = '';

            if (motDePasse.length === 0) {
                text = '';
            } else if (score <= 25) {
                text = '🔴 Très faible';
                color = '#e74c3c';
            } else if (score <= 50) {
                text = '🟠 Faible';
                color = '#f39c12';
            } else if (score <= 75) {
                text = '🟢 Bon';
                color = '#2ecc71';
            } else {
                text = '🟢 Très fort !';
                color = '#27ae60';
            }

            els.strengthText.textContent = text;
            els.strengthText.style.color = color;
        }
    }

    function markRequirement(element, valid) {
        if (!element) return;
        if (valid) {
            element.classList.add('valid');
            element.classList.remove('invalid');
        } else {
            element.classList.remove('valid');
            element.classList.add('invalid');
        }
    }

    /* ==========================================================
     SECTION 5 : VÉRIFICATION CORRESPONDANCE
     ========================================================== */
    function initPasswordMatch() {
        var els = getElements();
        if (!els.confirmation) return;

        els.confirmation.addEventListener('input', function() {
            checkPasswordMatch(els);
        });

        els.nouveauMotDePasse.addEventListener('input', function() {
            if (els.confirmation.value.length > 0) {
                checkPasswordMatch(els);
            }
        });
    }

    function checkPasswordMatch(els) {
        var nouveau = els.nouveauMotDePasse.value;
        var confirmation = els.confirmation.value;
        var messageEl = els.messageCorrespondance;

        if (!messageEl) return;

        if (confirmation.length === 0) {
            messageEl.textContent = '';
            messageEl.className = 'form-match';
            resetInputStatus(els.confirmation, null, null);
            return;
        }

        if (nouveau === confirmation) {
            messageEl.innerHTML = '<i class="fas fa-check-circle"></i> Les mots de passe correspondent';
            messageEl.className = 'form-match match-success';
            setInputValid(els.confirmation, null);
        } else {
            messageEl.innerHTML = '<i class="fas fa-times-circle"></i> Les mots de passe ne correspondent pas';
            messageEl.className = 'form-match match-error';
            setInputError(els.confirmation, null, null, '');
        }
    }

    /* ==========================================================
     SECTION 6 : TOGGLE MOTS DE PASSE (CORRIGÉ)
     ========================================================== */
    function initTogglePasswords() {
        var els = getElements();

        setupToggle(els.toggleAncien, els.ancienMotDePasse);
        setupToggle(els.toggleNouveau, els.nouveauMotDePasse);
        setupToggle(els.toggleConfirm, els.confirmation);
    }

    function setupToggle(btn, input) {
        if (!btn || !input) return;

        btn.addEventListener('click', function(e) {
            e.preventDefault();
            
            var isPassword = input.getAttribute('type') === 'password';
            
            // Sauvegarder les classes CSS actuelles
            var currentClasses = input.className;
            
            // Changer le type
            input.setAttribute('type', isPassword ? 'text' : 'password');
            
            // Restaurer les classes CSS (le changement de type peut les réinitialiser)
            input.className = currentClasses;

            // Changer l'icône
            var icon = this.querySelector('i');
            if (icon) {
                icon.classList.toggle('fa-eye', !isPassword);
                icon.classList.toggle('fa-eye-slash', isPassword);
            }

            // Animation subtile du bouton
            this.style.transform = 'scale(0.9)';
            var self = this;
            setTimeout(function() {
                self.style.transform = 'scale(1)';
            }, 150);

            // Remettre le focus (sans toucher à la valeur pour éviter la perte de focus)
            input.focus();
        });

        btn.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.click();
            }
        });
    }

    /* ==========================================================
     SECTION 7 : SOUMISSION DU FORMULAIRE
     ========================================================== */
    function initFormSubmission() {
        var els = getElements();
        if (!els.form) return;

        var isSubmitting = false;

        els.form.addEventListener('submit', function(e) {
            if (isSubmitting) {
                e.preventDefault();
                return false;
            }

            var errors = validateAllFields(els);
            
            if (errors.length > 0) {
                e.preventDefault();
                
                var firstError = document.querySelector('.form-group input.input-error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                }
                
                return false;
            }

            isSubmitting = true;
            activateLoader(els);
            
            setTimeout(function() {
                if (isSubmitting) {
                    deactivateLoader(els);
                    isSubmitting = false;
                    showToast('La modification prend plus de temps que prévu. Veuillez patienter...', 'warning', 5000);
                }
            }, 10000);

            return true;
        });
    }

    function validateAllFields(els) {
        var errors = [];

        // Validation ancien mot de passe
        if (els.ancienMotDePasse) {
            var ancienValue = els.ancienMotDePasse.value;
            if (ancienValue.length === 0) {
                errors.push({ field: 'ancienMotDePasse', message: 'Veuillez entrer votre mot de passe actuel.' });
                setInputError(els.ancienMotDePasse, null, els.ancienError, 'Veuillez entrer votre mot de passe actuel.');
            } else if (ancienValue.length < 6) {
                errors.push({ field: 'ancienMotDePasse', message: 'Mot de passe actuel trop court.' });
                setInputError(els.ancienMotDePasse, null, els.ancienError, 'Mot de passe trop court (minimum 6 caractères).');
            }
        }

        // Validation nouveau mot de passe
        if (els.nouveauMotDePasse) {
            var nouveauValue = els.nouveauMotDePasse.value;
            if (nouveauValue.length === 0) {
                errors.push({ field: 'nouveauMotDePasse', message: 'Veuillez entrer un nouveau mot de passe.' });
                setInputError(els.nouveauMotDePasse, null, null, 'Veuillez entrer un nouveau mot de passe.');
            } else if (nouveauValue.length < 8) {
                errors.push({ field: 'nouveauMotDePasse', message: 'Le mot de passe doit contenir au moins 8 caractères.' });
                setInputError(els.nouveauMotDePasse, null, null, 'Minimum 8 caractères requis.');
            } else {
                if (!/[A-Z]/.test(nouveauValue)) {
                    errors.push({ field: 'nouveauMotDePasse', message: 'Le mot de passe doit contenir une majuscule.' });
                }
                if (!/[a-z]/.test(nouveauValue)) {
                    errors.push({ field: 'nouveauMotDePasse', message: 'Le mot de passe doit contenir une minuscule.' });
                }
                if (!/[0-9]/.test(nouveauValue)) {
                    errors.push({ field: 'nouveauMotDePasse', message: 'Le mot de passe doit contenir un chiffre.' });
                }
                if (!/[!@#$%^&*(),.?":{}|<>_\-+=\[\]\\;:'/`~]/.test(nouveauValue)) {
                    errors.push({ field: 'nouveauMotDePasse', message: 'Le mot de passe doit contenir un caractère spécial.' });
                }
            }
        }

        // Validation confirmation
        if (els.confirmation) {
            var confirmValue = els.confirmation.value;
            var nouveauValue = els.nouveauMotDePasse ? els.nouveauMotDePasse.value : '';
            
            if (confirmValue.length === 0) {
                errors.push({ field: 'confirmation', message: 'Veuillez confirmer votre nouveau mot de passe.' });
                setInputError(els.confirmation, null, null, 'Veuillez confirmer votre nouveau mot de passe.');
            } else if (confirmValue !== nouveauValue) {
                errors.push({ field: 'confirmation', message: 'Les mots de passe ne correspondent pas.' });
                setInputError(els.confirmation, null, null, '');
                if (els.messageCorrespondance) {
                    els.messageCorrespondance.innerHTML = '<i class="fas fa-times-circle"></i> Les mots de passe ne correspondent pas';
                    els.messageCorrespondance.className = 'form-match match-error';
                }
            }
        }

        return errors;
    }

    /* ==========================================================
     SECTION 8 : GESTION DU LOADER
     ========================================================== */
    function activateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.add('loading');
            els.btnSubmit.disabled = true;
        }
        if (els.ancienMotDePasse) els.ancienMotDePasse.readOnly = true;
        if (els.nouveauMotDePasse) els.nouveauMotDePasse.readOnly = true;
        if (els.confirmation) els.confirmation.readOnly = true;
    }

    function deactivateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.remove('loading');
            els.btnSubmit.disabled = false;
        }
        if (els.ancienMotDePasse) els.ancienMotDePasse.readOnly = false;
        if (els.nouveauMotDePasse) els.nouveauMotDePasse.readOnly = false;
        if (els.confirmation) els.confirmation.readOnly = false;
    }

    /* ==========================================================
     SECTION 9 : UTILITAIRES VISUELS
     ========================================================== */

    function setInputValid(input, statusEl) {
        if (!input) return;
        input.classList.remove('input-error');
        input.classList.add('input-valid');
        
        var errorEl = input.closest('.form-group')?.querySelector('.form-error');
        if (errorEl) {
            errorEl.textContent = '';
        }
    }

    function setInputError(input, statusEl, errorEl, message) {
        if (!input) return;
        input.classList.remove('input-valid');
        input.classList.add('input-error');

        if (errorEl && message) {
            errorEl.textContent = message;
        }
    }

    function resetInputStatus(input, statusEl, errorEl) {
        if (!input) return;
        input.classList.remove('input-valid', 'input-error');

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

})();