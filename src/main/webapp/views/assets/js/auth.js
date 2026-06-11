/**
 * ============================================================
 * FICHIER : views/assets/js/auth.js
 * RÔLE    : Scripts des pages d'authentification LearnPath
 *           - Validation en temps réel des champs
 *           - Indicateur de force du mot de passe avec animation
 *           - Vérification correspondance des mots de passe
 *           - Toggle affichage mot de passe
 *           - Validation à la soumission
 *           - Gestion du loader du bouton
 *           - Empêche la double soumission
 *           - Validation email (format)
 *           - Feedback visuel immédiat avec animations
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
        initTogglePassword();
        initFormSubmission();
        initInputAnimations();
        initEmailValidation();
        initPseudoValidation();
    });

    /* ==========================================================
     SECTION 2 : RÉFÉRENCES AUX ÉLÉMENTS DU DOM
     ========================================================== */
    function getElements() {
        return {
            form: document.getElementById('formInscription'),
            pseudo: document.getElementById('pseudo'),
            email: document.getElementById('email'),
            motDePasse: document.getElementById('motDePasse'),
            confirmation: document.getElementById('confirmation'),
            btnSubmit: document.getElementById('btnInscription'),
            btnLoader: document.getElementById('btnLoader'),
            strengthBar: document.getElementById('strengthBar'),
            strengthText: document.getElementById('strengthText'),
            messageCorrespondance: document.getElementById('messageCorrespondance'),
            pseudoStatus: document.getElementById('pseudoStatus'),
            emailStatus: document.getElementById('emailStatus'),
            pseudoError: document.getElementById('pseudoError'),
            emailError: document.getElementById('emailError'),
            acceptConditions: document.getElementById('acceptConditions'),
            reqLength: document.getElementById('reqLength'),
            reqUppercase: document.getElementById('reqUppercase'),
            reqLowercase: document.getElementById('reqLowercase'),
            reqNumber: document.getElementById('reqNumber'),
            passwordRequirements: document.getElementById('passwordRequirements'),
            passwordStrengthContainer: document.getElementById('passwordStrengthContainer')
        };
    }

    /* ==========================================================
     SECTION 3 : VALIDATION EN TEMPS RÉEL DU PSEUDO
     ========================================================== */
    function initPseudoValidation() {
        const els = getElements();
        if (!els.pseudo) return;

        els.pseudo.addEventListener('input', function() {
            validatePseudoField(els);
        });

        els.pseudo.addEventListener('blur', function() {
            validatePseudoField(els);
        });
    }

    function validatePseudoField(els) {
        const value = els.pseudo.value.trim();
        const statusEl = els.pseudoStatus;
        const errorEl = els.pseudoError;
        const input = els.pseudo;

        resetInputStatus(input, statusEl, errorEl);

        if (value.length === 0) return;

        if (value.length < 3) {
            setInputError(input, statusEl, errorEl, 'Le pseudo doit contenir au moins 3 caractères.');
            return;
        }

        if (value.length > 50) {
            setInputError(input, statusEl, errorEl, 'Le pseudo ne doit pas dépasser 50 caractères.');
            return;
        }

        if (!/^[a-zA-Z0-9_\-À-ÿ\s]+$/.test(value)) {
            setInputError(input, statusEl, errorEl, 'Le pseudo contient des caractères non autorisés.');
            return;
        }

        setInputValid(input, statusEl);
    }

    /* ==========================================================
     SECTION 4 : VALIDATION EN TEMPS RÉEL DE L'EMAIL
     ========================================================== */
    function initEmailValidation() {
        const els = getElements();
        if (!els.email) return;

        els.email.addEventListener('input', function() {
            validateEmailField(els);
        });

        els.email.addEventListener('blur', function() {
            validateEmailField(els);
        });
    }

    function validateEmailField(els) {
        const value = els.email.value.trim();
        const statusEl = els.emailStatus;
        const errorEl = els.emailError;
        const input = els.email;

        resetInputStatus(input, statusEl, errorEl);

        if (value.length === 0) {
            setInputError(input, statusEl, errorEl, 'L\'adresse email est obligatoire.');
            return;
        }

        const emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;

        if (!emailRegex.test(value)) {
            setInputError(input, statusEl, errorEl, 'Veuillez entrer une adresse email valide.');
            return;
        }

        setInputValid(input, statusEl);
    }

    /* ==========================================================
     SECTION 5 : INITIALISATION VALIDATION FORMULAIRE
     ========================================================== */
    function initFormValidation() {
        const els = getElements();
        if (!els.form) return;

        if (els.motDePasse) {
            els.motDePasse.addEventListener('input', function() {
                updatePasswordStrength(els);
                if (els.confirmation && els.confirmation.value.length > 0) {
                    checkPasswordMatch(els);
                }
            });
        }

        if (els.confirmation) {
            els.confirmation.addEventListener('input', function() {
                checkPasswordMatch(els);
            });
        }
    }

    /* ==========================================================
     SECTION 6 : FORCE DU MOT DE PASSE (ANIMÉE)
     ========================================================== */
    function initPasswordStrength() {
        const els = getElements();
        if (!els.motDePasse) return;

        els.motDePasse.addEventListener('focus', function() {
            if (els.passwordRequirements) {
                els.passwordRequirements.style.display = 'flex';
                els.passwordRequirements.style.opacity = '0';
                els.passwordRequirements.style.transform = 'translateY(-8px)';
                els.passwordRequirements.style.transition = 'all 0.3s ease';
                
                requestAnimationFrame(function() {
                    els.passwordRequirements.style.opacity = '1';
                    els.passwordRequirements.style.transform = 'translateY(0)';
                });
            }
            if (els.passwordStrengthContainer) {
                els.passwordStrengthContainer.style.display = 'block';
                els.passwordStrengthContainer.style.opacity = '0';
                els.passwordStrengthContainer.style.transition = 'all 0.3s ease';
                
                requestAnimationFrame(function() {
                    els.passwordStrengthContainer.style.opacity = '1';
                });
            }
        });
    }

    function updatePasswordStrength(els) {
        const motDePasse = els.motDePasse.value;
        let score = 0;
        let checksPassed = 0;

        // === CRITÈRE 1 : Longueur >= 8 ===
        if (motDePasse.length >= 8) {
            score += 25;
            checksPassed++;
            markRequirement(els.reqLength, true);
        } else {
            markRequirement(els.reqLength, false);
        }

        // === CRITÈRE 2 : Au moins une majuscule ===
        if (/[A-Z]/.test(motDePasse)) {
            score += 25;
            checksPassed++;
            markRequirement(els.reqUppercase, true);
        } else {
            markRequirement(els.reqUppercase, false);
        }

        // === CRITÈRE 3 : Au moins une minuscule ===
        if (/[a-z]/.test(motDePasse)) {
            score += 25;
            checksPassed++;
            markRequirement(els.reqLowercase, true);
        } else {
            markRequirement(els.reqLowercase, false);
        }

        // === CRITÈRE 4 : Au moins un chiffre ===
        if (/[0-9]/.test(motDePasse)) {
            score += 25;
            checksPassed++;
            markRequirement(els.reqNumber, true);
        } else {
            markRequirement(els.reqNumber, false);
        }

        // Bonus si tous les critères sont remplis
        if (checksPassed === 4) {
            score = 100;
        }

        // === MISE À JOUR DE LA BARRE DE FORCE ===
        if (els.strengthBar) {
            els.strengthBar.style.width = score + '%';
            els.strengthBar.style.transition = 'width 0.5s cubic-bezier(0.4, 0, 0.2, 1), background-color 0.5s ease, box-shadow 0.5s ease';

            // Couleurs selon le score
            let color;
            let shadow;
            if (score <= 25) {
                color = '#e74c3c';
                shadow = '0 0 8px rgba(231, 76, 60, 0.3)';
            } else if (score <= 50) {
                color = '#f39c12';
                shadow = '0 0 8px rgba(243, 156, 18, 0.3)';
            } else if (score <= 75) {
                color = '#2ecc71';
                shadow = '0 0 8px rgba(46, 204, 113, 0.3)';
            } else {
                color = '#27ae60';
                shadow = '0 0 16px rgba(39, 174, 96, 0.5)';
            }
            
            els.strengthBar.style.backgroundColor = color;
            els.strengthBar.style.boxShadow = shadow;
        }

        // === TEXTE DE FORCE ===
        if (els.strengthText) {
            let text = '';
            let color = '';
            let icon = '';

            if (motDePasse.length === 0) {
                text = '';
                els.strengthText.style.opacity = '0';
            } else {
                els.strengthText.style.opacity = '1';
                
                if (score <= 25) {
                    text = 'Faible';
                    color = '#e74c3c';
                    icon = '🔴';
                } else if (score <= 50) {
                    text = 'Moyen';
                    color = '#f39c12';
                    icon = '🟠';
                } else if (score <= 75) {
                    text = 'Bon';
                    color = '#2ecc71';
                    icon = '🟢';
                } else {
                    text = 'Excellent !';
                    color = '#27ae60';
                    icon = '✅';
                }
            }

            els.strengthText.textContent = icon + ' Mot de passe ' + text;
            els.strengthText.style.color = color;
            els.strengthText.style.transition = 'all 0.3s ease';
        }
    }

    /**
     * Marque une exigence comme remplie ou non
     * Avec animation fluide
     */
    function markRequirement(element, valid) {
        if (!element) return;

        if (valid) {
            if (!element.classList.contains('valid')) {
                element.classList.add('valid');
                // Animation de validation
                element.style.transition = 'all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                element.style.transform = 'scale(1.05)';
                setTimeout(function() {
                    element.style.transform = 'scale(1)';
                }, 300);
                
                // Changer l'icône
                const icon = element.querySelector('i');
                if (icon) {
                    icon.className = 'fas fa-check-circle';
                    icon.style.color = '#27ae60';
                }
            }
        } else {
            if (element.classList.contains('valid')) {
                element.classList.remove('valid');
                // Animation de retour
                element.style.transition = 'all 0.3s ease';
                
                // Remettre l'icône
                const icon = element.querySelector('i');
                if (icon) {
                    icon.className = 'fas fa-circle';
                    icon.style.color = '';
                }
            }
        }
    }

    /* ==========================================================
     SECTION 7 : VÉRIFICATION CORRESPONDANCE MOTS DE PASSE
     ========================================================== */
    function initPasswordMatch() {
        const els = getElements();
        if (!els.confirmation) return;

        els.confirmation.addEventListener('input', function() {
            checkPasswordMatch(els);
        });

        els.motDePasse.addEventListener('input', function() {
            if (els.confirmation.value.length > 0) {
                checkPasswordMatch(els);
            }
        });
    }

    function checkPasswordMatch(els) {
        const motDePasse = els.motDePasse.value;
        const confirmation = els.confirmation.value;
        const messageEl = els.messageCorrespondance;

        if (!messageEl) return;

        if (confirmation.length === 0) {
            messageEl.innerHTML = '';
            messageEl.className = 'form-match';
            messageEl.style.opacity = '0';
            resetInputStatus(els.confirmation, null, null);
            return;
        }

        messageEl.style.opacity = '1';
        messageEl.style.transition = 'all 0.3s ease';

        if (motDePasse === confirmation) {
            messageEl.innerHTML = '<i class="fas fa-check-circle"></i> Les mots de passe correspondent';
            messageEl.className = 'form-match match-success';
            messageEl.style.color = '#27ae60';
            setInputValid(els.confirmation, null);
        } else {
            messageEl.innerHTML = '<i class="fas fa-times-circle"></i> Les mots de passe ne correspondent pas';
            messageEl.className = 'form-match match-error';
            messageEl.style.color = '#e74c3c';
            setInputError(els.confirmation, null, null, '');
        }
    }

    /* ==========================================================
     SECTION 8 : TOGGLE MOT DE PASSE
     ========================================================== */
    function initTogglePassword() {
        const toggleBtns = document.querySelectorAll('.toggle-password-btn');
        
        toggleBtns.forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const input = this.parentElement.querySelector('input');
                if (!input) return;

                const isPassword = input.getAttribute('type') === 'password';
                input.setAttribute('type', isPassword ? 'text' : 'password');

                const icon = this.querySelector('i');
                if (icon) {
                    icon.style.transition = 'transform 0.3s ease';
                    icon.style.transform = 'rotate(180deg)';
                    setTimeout(function() {
                        icon.classList.toggle('fa-eye', !isPassword);
                        icon.classList.toggle('fa-eye-slash', isPassword);
                        icon.style.transform = 'rotate(0deg)';
                    }, 150);
                }

                input.focus();
            });
        });
    }

    /* ==========================================================
     SECTION 9 : SOUMISSION DU FORMULAIRE
     ========================================================== */
    function initFormSubmission() {
        const els = getElements();
        if (!els.form) return;

        let isSubmitting = false;

        els.form.addEventListener('submit', function(e) {
            if (isSubmitting) {
                e.preventDefault();
                return false;
            }

            const errors = validateAllFields(els);
            
            if (errors.length > 0) {
                e.preventDefault();
                displaySubmissionErrors(els, errors);
                
                const firstError = document.querySelector('.form-group input.input-error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                }
                
                return false;
            }

            if (els.acceptConditions && !els.acceptConditions.checked) {
                e.preventDefault();
                shakeElement(els.acceptConditions.closest('.checkbox-group'));
                
                const checkboxGroup = els.acceptConditions.closest('.checkbox-group');
                let errorMsg = checkboxGroup.querySelector('.form-error');
                if (!errorMsg) {
                    errorMsg = document.createElement('span');
                    errorMsg.className = 'form-error';
                    checkboxGroup.appendChild(errorMsg);
                }
                errorMsg.textContent = 'Vous devez accepter les conditions d\'utilisation.';
                errorMsg.style.color = '#e74c3c';
                
                return false;
            }

            isSubmitting = true;
            activateLoader(els);
            
            setTimeout(function() {
                if (isSubmitting) {
                    deactivateLoader(els);
                    isSubmitting = false;
                }
            }, 10000);

            return true;
        });
    }

    /**
     * Validation complète de tous les champs avant soumission
     */
    function validateAllFields(els) {
        const errors = [];

        // Validation pseudo
        if (els.pseudo) {
            const pseudoValue = els.pseudo.value.trim();
            if (pseudoValue.length === 0) {
                errors.push({ field: 'pseudo', message: 'Le pseudo est obligatoire.' });
                setInputError(els.pseudo, els.pseudoStatus, els.pseudoError, 'Le pseudo est obligatoire.');
            } else if (pseudoValue.length < 3) {
                errors.push({ field: 'pseudo', message: 'Le pseudo doit contenir au moins 3 caractères.' });
                setInputError(els.pseudo, els.pseudoStatus, els.pseudoError, 'Minimum 3 caractères.');
            }
        }

        // Validation email (OBLIGATOIRE)
        if (els.email) {
            const emailValue = els.email.value.trim();
            if (emailValue.length === 0) {
                errors.push({ field: 'email', message: 'L\'email est obligatoire.' });
                setInputError(els.email, els.emailStatus, els.emailError, 'L\'email est obligatoire.');
            } else {
                const emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
                if (!emailRegex.test(emailValue)) {
                    errors.push({ field: 'email', message: 'Adresse email invalide.' });
                    setInputError(els.email, els.emailStatus, els.emailError, 'Adresse email invalide.');
                }
            }
        }

        // Validation mot de passe
        if (els.motDePasse) {
            const mdpValue = els.motDePasse.value;
            if (mdpValue.length === 0) {
                errors.push({ field: 'motDePasse', message: 'Le mot de passe est obligatoire.' });
                setInputError(els.motDePasse, null, null, '');
            } else if (mdpValue.length < 8) {
                errors.push({ field: 'motDePasse', message: 'Minimum 8 caractères.' });
                setInputError(els.motDePasse, null, null, '');
            } else if (!/[A-Z]/.test(mdpValue)) {
                errors.push({ field: 'motDePasse', message: 'Il faut une majuscule.' });
                setInputError(els.motDePasse, null, null, '');
            } else if (!/[a-z]/.test(mdpValue)) {
                errors.push({ field: 'motDePasse', message: 'Il faut une minuscule.' });
                setInputError(els.motDePasse, null, null, '');
            } else if (!/[0-9]/.test(mdpValue)) {
                errors.push({ field: 'motDePasse', message: 'Il faut un chiffre.' });
                setInputError(els.motDePasse, null, null, '');
            }
        }

        // Validation confirmation
        if (els.confirmation) {
            const confirmValue = els.confirmation.value;
            const mdpValue = els.motDePasse ? els.motDePasse.value : '';
            
            if (confirmValue.length === 0) {
                errors.push({ field: 'confirmation', message: 'La confirmation est obligatoire.' });
                setInputError(els.confirmation, null, null, '');
            } else if (confirmValue !== mdpValue) {
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

    function displaySubmissionErrors(els, errors) {
        console.log('Erreurs de validation :', errors);
    }

    /* ==========================================================
     SECTION 10 : GESTION DU LOADER
     ========================================================== */
    function activateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.add('loading');
            els.btnSubmit.disabled = true;
        }
    }

    function deactivateLoader(els) {
        if (els.btnSubmit) {
            els.btnSubmit.classList.remove('loading');
            els.btnSubmit.disabled = false;
        }
    }

    /* ==========================================================
     SECTION 11 : UTILITAIRES VISUELS
     ========================================================== */

    function setInputValid(input, statusEl) {
        if (!input) return;
        input.classList.remove('input-error');
        input.classList.add('input-valid');
        
        if (statusEl) {
            statusEl.innerHTML = '<i class="fas fa-check-circle" style="color: #27ae60;"></i>';
            statusEl.className = 'input-status valid';
            statusEl.style.opacity = '1';
            statusEl.style.transition = 'opacity 0.3s ease';
        }

        const errorEl = input.closest('.form-group')?.querySelector('.form-error');
        if (errorEl) {
            errorEl.textContent = '';
        }
    }

    function setInputError(input, statusEl, errorEl, message) {
        if (!input) return;
        input.classList.remove('input-valid');
        input.classList.add('input-error');
        
        if (statusEl) {
            statusEl.innerHTML = '<i class="fas fa-exclamation-circle" style="color: #e74c3c;"></i>';
            statusEl.className = 'input-status error';
            statusEl.style.opacity = '1';
        }

        if (errorEl && message) {
            errorEl.textContent = message;
            errorEl.style.color = '#e74c3c';
        }
    }

    function resetInputStatus(input, statusEl, errorEl) {
        if (!input) return;
        input.classList.remove('input-valid', 'input-error');
        
        if (statusEl) {
            statusEl.innerHTML = '';
            statusEl.className = 'input-status';
            statusEl.style.opacity = '0';
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

    /* ==========================================================
     SECTION 12 : ANIMATIONS DES INPUTS
     ========================================================== */
    function initInputAnimations() {
        const inputs = document.querySelectorAll('.form-group input[type="text"], .form-group input[type="email"], .form-group input[type="password"]');
        
        inputs.forEach(function(input) {
            input.addEventListener('focus', function() {
                const wrapper = this.closest('.input-wrapper');
                if (wrapper) {
                    wrapper.style.transition = 'all 0.3s ease';
                    wrapper.style.borderColor = '#667eea';
                    wrapper.style.boxShadow = '0 0 0 3px rgba(102, 126, 234, 0.1)';
                }
            });

            input.addEventListener('blur', function() {
                const wrapper = this.closest('.input-wrapper');
                if (wrapper) {
                    wrapper.style.transition = 'all 0.3s ease';
                    wrapper.style.borderColor = '#e8eaef';
                    wrapper.style.boxShadow = 'none';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 13 : STYLES D'ANIMATION INJECTÉS
     ========================================================== */
    const animationStyles = document.createElement('style');
    animationStyles.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-6px); }
            20%, 40%, 60%, 80% { transform: translateX(6px); }
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-8px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        @keyframes checkBounce {
            0% { transform: scale(0); }
            50% { transform: scale(1.3); }
            100% { transform: scale(1); }
        }
        
        .requirement.valid i {
            animation: checkBounce 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
        }
        
        .requirement i {
            transition: all 0.3s ease;
        }
        
        .strength-bar {
            transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1), 
                        background-color 0.5s ease, 
                        box-shadow 0.5s ease !important;
        }
        
        .password-strength-text {
            transition: all 0.3s ease;
        }
        
        .input-valid {
            border-color: #27ae60 !important;
            box-shadow: 0 0 0 3px rgba(39, 174, 96, 0.1) !important;
        }
        
        .input-error {
            border-color: #e74c3c !important;
            box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1) !important;
        }
    `;
    document.head.appendChild(animationStyles);

    /* ==========================================================
     SECTION 14 : NAVIGATION CLAVIER
     ========================================================== */
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && e.target.tagName === 'INPUT') {
            const form = e.target.closest('form');
            if (form && form.id === 'formInscription') {
                const inputs = form.querySelectorAll('input:not([type="checkbox"])');
                const lastInput = inputs[inputs.length - 1];
                
                if (e.target !== lastInput) {
                    e.preventDefault();
                    const currentIndex = Array.from(inputs).indexOf(e.target);
                    if (currentIndex < inputs.length - 1) {
                        inputs[currentIndex + 1].focus();
                    }
                }
            }
        }
    });

})();