/**
 * ============================================================
 * FICHIER : views/assets/js/connexion.js
 * RÔLE    : Scripts de la page de connexion LearnPath
 *           - Validation en temps réel des champs (email + mot de passe)
 *           - Toggle affichage mot de passe
 *           - Gestion "Se souvenir de moi" (email sauvegardé)
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
        initTogglePassword();
        initFormSubmission();
        initRememberMe();
        initInputAnimations();
        initAutoFocus();
    });

    /* ==========================================================
     SECTION 2 : RÉFÉRENCES AUX ÉLÉMENTS DU DOM
     ========================================================== */
    function getElements() {
        return {
            form: document.getElementById('formConnexion'),
            email: document.getElementById('email'),
            motDePasse: document.getElementById('motDePasse'),
            btnSubmit: document.getElementById('btnConnexion'),
            btnLoader: document.getElementById('btnLoader'),
            emailStatus: document.getElementById('emailStatus'),
            emailError: document.getElementById('emailError'),
            motDePasseError: document.getElementById('motDePasseError'),
            rememberMe: document.getElementById('rememberMe'),
            togglePasswordBtn: document.getElementById('togglePassword')
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

        // Validation du mot de passe en temps réel
        if (els.motDePasse) {
            els.motDePasse.addEventListener('input', function() {
                validateMotDePasseField(els);
            });

            els.motDePasse.addEventListener('blur', function() {
                validateMotDePasseField(els);
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

    /**
     * Validation du champ mot de passe
     */
    function validateMotDePasseField(els) {
        var value = els.motDePasse.value;
        var errorEl = els.motDePasseError;
        var input = els.motDePasse;

        // Réinitialiser
        resetInputStatus(input, null, errorEl);

        if (value.length === 0) {
            return;
        }

        if (value.length < 6) {
            setInputError(input, null, errorEl, 'Mot de passe trop court (minimum 6 caractères).');
            return;
        }

        // Mot de passe valide
        setInputValid(input, null, 'Mot de passe saisi');
    }

    /* ==========================================================
     SECTION 4 : TOGGLE MOT DE PASSE
     ========================================================== */
    function initTogglePassword() {
        var els = getElements();
        if (!els.togglePasswordBtn || !els.motDePasse) return;

        els.togglePasswordBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            var input = els.motDePasse;
            var isPassword = input.getAttribute('type') === 'password';
            
            // Basculer le type
            input.setAttribute('type', isPassword ? 'text' : 'password');

            // Changer l'icône
            var icon = this.querySelector('i');
            if (icon) {
                icon.classList.toggle('fa-eye', !isPassword);
                icon.classList.toggle('fa-eye-slash', isPassword);
            }

            // Animation subtile du bouton
            this.style.transform = 'scale(0.9)';
            setTimeout(function() {
                els.togglePasswordBtn.style.transform = 'scale(1)';
            }, 150);

            // Remettre le focus sur l'input
            input.focus();
            
            // Remettre le curseur à la fin
            var val = input.value;
            input.value = '';
            input.value = val;
        });

        // Support du clavier pour le toggle
        els.togglePasswordBtn.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.click();
            }
        });
    }

    /* ==========================================================
     SECTION 5 : GESTION "SE SOUVENIR DE MOI"
     ========================================================== */
    function initRememberMe() {
        var els = getElements();
        if (!els.rememberMe) return;

        // Restaurer l'état précédent si l'email était sauvegardé
        var savedEmail = localStorage.getItem('LearnPath_remember_email');
        if (savedEmail && els.email) {
            els.email.value = savedEmail;
            els.rememberMe.checked = true;
            
            // Valider le champ pré-rempli
            if (savedEmail.length > 0) {
                validateEmailField(getElements());
            }
        }

        // Animation de la checkbox
        els.rememberMe.addEventListener('change', function() {
            var checkboxCustom = this.nextElementSibling;
            if (checkboxCustom && checkboxCustom.classList.contains('checkbox-custom')) {
                if (this.checked) {
                    checkboxCustom.style.transform = 'scale(1.2)';
                    setTimeout(function() {
                        checkboxCustom.style.transform = 'scale(1)';
                    }, 200);
                }
            }
        });
    }

    /**
     * Sauvegarder ou supprimer l'email dans le localStorage
     */
    function handleRememberMe(els) {
        if (els.rememberMe && els.rememberMe.checked && els.email) {
            var email = els.email.value.trim();
            if (email.length > 0) {
                localStorage.setItem('LearnPath_remember_email', email);
            }
        } else {
            localStorage.removeItem('LearnPath_remember_email');
        }
    }

    /* ==========================================================
     SECTION 6 : SOUMISSION DU FORMULAIRE
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
                
                // Scroller vers la première erreur
                var firstError = document.querySelector('.form-group input.input-error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                }
                
                return false;
            }

            // Tout est OK
            isSubmitting = true;
            
            // Gérer "Se souvenir de moi"
            handleRememberMe(els);
            
            // Activer le loader (NE PAS désactiver les inputs)
            activateLoader(els);
            
            // Timeout de sécurité (10 secondes)
            setTimeout(function() {
                if (isSubmitting) {
                    deactivateLoader(els);
                    isSubmitting = false;
                    
                    // Afficher un message si la requête prend trop de temps
                    showToast('La connexion prend plus de temps que prévu. Veuillez patienter...', 'warning', 5000);
                }
            }, 10000);

            return true;
        });
    }

    /**
     * Validation complète de tous les champs avant soumission
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

        // Validation mot de passe
        if (els.motDePasse) {
            var mdpValue = els.motDePasse.value;
            if (mdpValue.length === 0) {
                errors.push({ field: 'motDePasse', message: 'Veuillez entrer votre mot de passe.' });
                setInputError(els.motDePasse, null, els.motDePasseError, 'Veuillez entrer votre mot de passe.');
            } else if (mdpValue.length < 6) {
                errors.push({ field: 'motDePasse', message: 'Mot de passe trop court.' });
                setInputError(els.motDePasse, null, els.motDePasseError, 'Mot de passe trop court (minimum 6 caractères).');
            }
        }

        return errors;
    }

    function displaySubmissionErrors(els, errors) {
        if (errors.length > 0) {
            console.warn('Erreurs de validation connexion :', errors);
        }
    }

    /* ==========================================================
     SECTION 7 : GESTION DU LOADER (sans désactiver les inputs)
     ========================================================== */
    function activateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.add('loading');
            els.btnSubmit.disabled = true;
        }
        
        // Désactiver uniquement le bouton toggle (pas les inputs !)
        if (els.togglePasswordBtn) els.togglePasswordBtn.disabled = true;
    }

    function deactivateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.remove('loading');
            els.btnSubmit.disabled = false;
        }
        
        // Réactiver le bouton toggle
        if (els.togglePasswordBtn) els.togglePasswordBtn.disabled = false;
    }

    /* ==========================================================
     SECTION 8 : FOCUS AUTOMATIQUE
     ========================================================== */
    function initAutoFocus() {
        var els = getElements();
        
        // Si l'email est déjà rempli (remember me), focus sur le mot de passe
        if (els.email && els.motDePasse) {
            if (els.email.value.trim().length > 0) {
                els.motDePasse.focus();
            }
        }
    }

    /* ==========================================================
     SECTION 9 : UTILITAIRES VISUELS
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

    function shakeElement(element) {
        if (!element) return;
        
        element.style.animation = 'none';
        element.offsetHeight;
        element.style.animation = 'shake 0.5s ease';
        
        element.addEventListener('animationend', function handler() {
            element.removeEventListener('animationend', handler);
            element.style.animation = '';
        });
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
     SECTION 10 : ANIMATIONS DES INPUTS
     ========================================================== */
    function initInputAnimations() {
        var inputs = document.querySelectorAll('.form-group input[type="email"], .form-group input[type="password"]');
        
        inputs.forEach(function(input) {
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

            input.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    this.blur();
                }
            });
        });
    }

    /* ==========================================================
     SECTION 11 : NAVIGATION CLAVIER
     ========================================================== */
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && e.target.tagName === 'INPUT') {
            var form = e.target.closest('form');
            if (form && form.id === 'formConnexion') {
                var inputs = form.querySelectorAll('input:not([type="checkbox"]):not([type="hidden"])');
                var currentIndex = Array.from(inputs).indexOf(e.target);
                
                if (currentIndex === inputs.length - 1) {
                    // Dernier champ : laisser le submit se déclencher
                } else if (currentIndex < inputs.length - 1) {
                    e.preventDefault();
                    inputs[currentIndex + 1].focus();
                }
            }
        }
    });

    /* ==========================================================
     SECTION 12 : PROTECTION CONTRE LA FERMETURE ACCIDENTELLE
     ========================================================== */
    var els = getElements();
    if (els.form) {
        var formHasData = false;

        if (els.email) {
            els.email.addEventListener('input', function() {
                formHasData = this.value.trim().length > 0 || 
                             (els.motDePasse && els.motDePasse.value.length > 0);
            });
        }

        if (els.motDePasse) {
            els.motDePasse.addEventListener('input', function() {
                formHasData = this.value.length > 0 || 
                             (els.email && els.email.value.trim().length > 0);
            });
        }

        els.form.addEventListener('submit', function() {
            formHasData = false;
        });

        window.addEventListener('beforeunload', function(e) {
            if (formHasData) {
                e.preventDefault();
                e.returnValue = 'Vous avez des données non sauvegardées. Voulez-vous vraiment quitter ?';
                return e.returnValue;
            }
        });
    }

})();