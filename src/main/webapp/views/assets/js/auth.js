/**
 * ============================================================
 * FICHIER : views/assets/js/auth.js
 * RÔLE    : Scripts des pages d'authentification AmijayQuiz
 *           - Validation en temps réel des champs
 *           - Indicateur de force du mot de passe
 *           - Vérification correspondance des mots de passe
 *           - Toggle affichage mot de passe
 *           - Validation à la soumission
 *           - Gestion du loader du bouton
 *           - Empêche la double soumission
 *           - Validation email (format)
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
        initTogglePassword();
        initFormSubmission();
        initInputAnimations();
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
            // Exigences mot de passe
            reqLength: document.getElementById('reqLength'),
            reqUppercase: document.getElementById('reqUppercase'),
            reqNumber: document.getElementById('reqNumber'),
            passwordRequirements: document.getElementById('passwordRequirements'),
            passwordStrengthContainer: document.getElementById('passwordStrengthContainer')
        };
    }

    /* ==========================================================
     SECTION 3 : VALIDATION EN TEMPS RÉEL DES CHAMPS
     ========================================================== */
    function initFormValidation() {
        const els = getElements();
        if (!els.form) return;

        // Validation du pseudo en temps réel
        if (els.pseudo) {
            els.pseudo.addEventListener('input', function() {
                validatePseudo(els);
            });

            els.pseudo.addEventListener('blur', function() {
                validatePseudo(els);
            });
        }

        // Validation de l'email en temps réel
        if (els.email) {
            els.email.addEventListener('input', function() {
                validateEmail(els);
            });

            els.email.addEventListener('blur', function() {
                validateEmail(els);
            });
        }

        // Validation du mot de passe en temps réel
        if (els.motDePasse) {
            els.motDePasse.addEventListener('input', function() {
                updatePasswordStrength(els);
                if (els.confirmation && els.confirmation.value.length > 0) {
                    checkPasswordMatch(els);
                }
            });
        }

        // Validation de la confirmation en temps réel
        if (els.confirmation) {
            els.confirmation.addEventListener('input', function() {
                checkPasswordMatch(els);
            });
        }
    }

    /**
     * Validation du pseudo
     */
    function validatePseudo(els) {
        const value = els.pseudo.value.trim();
        const statusEl = els.pseudoStatus;
        const errorEl = els.pseudoError;
        const input = els.pseudo;

        // Réinitialiser
        resetInputStatus(input, statusEl, errorEl);

        if (value.length === 0) {
            // Champ vide (ne rien afficher si pas encore touché)
            return;
        }

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

        // Pseudo valide
        setInputValid(input, statusEl);
    }

    /**
     * Validation de l'email (si renseigné)
     */
    function validateEmail(els) {
        const value = els.email.value.trim();
        const statusEl = els.emailStatus;
        const errorEl = els.emailError;
        const input = els.email;

        // Réinitialiser
        resetInputStatus(input, statusEl, errorEl);

        if (value.length === 0) {
            // Email optionnel, ne rien afficher
            return;
        }

        // Regex email standard
        const emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;

        if (!emailRegex.test(value)) {
            setInputError(input, statusEl, errorEl, 'Veuillez entrer une adresse email valide.');
            return;
        }

        // Email valide
        setInputValid(input, statusEl);
    }

    /* ==========================================================
     SECTION 4 : FORCE DU MOT DE PASSE
     ========================================================== */
    function initPasswordStrength() {
        const els = getElements();
        if (!els.motDePasse) return;

        // Afficher les exigences quand l'utilisateur focus le champ
        els.motDePasse.addEventListener('focus', function() {
            if (els.passwordRequirements) {
                els.passwordRequirements.style.display = 'flex';
                els.passwordRequirements.style.animation = 'fadeIn 0.3s ease';
            }
            if (els.passwordStrengthContainer) {
                els.passwordStrengthContainer.style.display = 'block';
            }
        });
    }

    function updatePasswordStrength(els) {
        const motDePasse = els.motDePasse.value;
        let score = 0;

        // Critère 1 : Longueur >= 6
        if (motDePasse.length >= 6) {
            score += 30;
            markRequirement(els.reqLength, true);
        } else {
            markRequirement(els.reqLength, false);
        }

        // Critère 2 : Longueur >= 8
        if (motDePasse.length >= 8) {
            score += 15;
        }

        // Critère 3 : Au moins une majuscule
        if (/[A-Z]/.test(motDePasse)) {
            score += 25;
            markRequirement(els.reqUppercase, true);
        } else {
            markRequirement(els.reqUppercase, false);
        }

        // Critère 4 : Au moins un chiffre ou caractère spécial
        if (/[0-9!@#$%^&*(),.?":{}|<>_\-+=\[\]\\;:'/`~]/.test(motDePasse)) {
            score += 30;
            markRequirement(els.reqNumber, true);
        } else {
            markRequirement(els.reqNumber, false);
        }

        // Mise à jour de la barre de force
        if (els.strengthBar) {
            els.strengthBar.style.width = score + '%';

            // Couleurs selon le score
            if (score <= 30) {
                els.strengthBar.style.backgroundColor = '#e74c3c';
            } else if (score <= 55) {
                els.strengthBar.style.backgroundColor = '#f39c12';
            } else if (score <= 75) {
                els.strengthBar.style.backgroundColor = '#2ecc71';
            } else {
                els.strengthBar.style.backgroundColor = '#27ae60';
            }
        }

        // Texte de force
        if (els.strengthText) {
            let text = '';
            let color = '';

            if (motDePasse.length === 0) {
                text = '';
            } else if (score <= 30) {
                text = '🔴 Mot de passe faible';
                color = '#e74c3c';
            } else if (score <= 55) {
                text = '🟠 Mot de passe moyen';
                color = '#f39c12';
            } else if (score <= 75) {
                text = '🟢 Mot de passe bon';
                color = '#2ecc71';
            } else {
                text = '🟢 Mot de passe fort !';
                color = '#27ae60';
            }

            els.strengthText.textContent = text;
            els.strengthText.style.color = color;
        }

        // Animation de la barre
        if (els.strengthBar && els.strengthBar.style.width === '100%') {
            els.strengthBar.style.transition = 'all 0.3s ease';
            setTimeout(function() {
                els.strengthBar.style.boxShadow = '0 0 10px rgba(39, 174, 96, 0.4)';
            }, 300);
        } else if (els.strengthBar) {
            els.strengthBar.style.boxShadow = 'none';
        }
    }

    function markRequirement(element, valid) {
        if (!element) return;
        if (valid) {
            element.classList.add('valid');
        } else {
            element.classList.remove('valid');
        }
    }

    /* ==========================================================
     SECTION 5 : VÉRIFICATION CORRESPONDANCE MOTS DE PASSE
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
            messageEl.textContent = '';
            messageEl.className = 'form-match';
            resetInputStatus(els.confirmation, null, null);
            return;
        }

        if (motDePasse === confirmation) {
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
     SECTION 6 : TOGGLE MOT DE PASSE
     ========================================================== */
    function initTogglePassword() {
        // La fonction globale togglePassword est déjà définie dans main.js
        // On s'assure que les boutons sont bien câblés
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
                    icon.classList.toggle('fa-eye', !isPassword);
                    icon.classList.toggle('fa-eye-slash', isPassword);
                }

                // Remettre le focus sur l'input
                input.focus();
            });
        });
    }

    /* ==========================================================
     SECTION 7 : SOUMISSION DU FORMULAIRE
     ========================================================== */
    function initFormSubmission() {
        const els = getElements();
        if (!els.form) return;

        let isSubmitting = false;

        els.form.addEventListener('submit', function(e) {
            // Empêcher la double soumission
            if (isSubmitting) {
                e.preventDefault();
                return false;
            }

            // Validation finale
            const errors = validateAllFields(els);
            
            if (errors.length > 0) {
                e.preventDefault();
                
                // Afficher les erreurs
                displaySubmissionErrors(els, errors);
                
                // Scroller vers la première erreur
                const firstError = document.querySelector('.form-group input.input-error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                }
                
                return false;
            }

            // Vérifier les conditions
            if (els.acceptConditions && !els.acceptConditions.checked) {
                e.preventDefault();
                shakeElement(els.acceptConditions.closest('.checkbox-group'));
                
                // Afficher un message
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

            // Tout est OK, activer le loader
            isSubmitting = true;
            activateLoader(els);
            
            // Désactiver le loader après 10 secondes (timeout de sécurité)
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
                setInputError(els.pseudo, els.pseudoStatus, els.pseudoError, 'Le pseudo doit contenir au moins 3 caractères.');
            } else if (!/^[a-zA-Z0-9_\-À-ÿ\s]+$/.test(pseudoValue)) {
                errors.push({ field: 'pseudo', message: 'Le pseudo contient des caractères non autorisés.' });
                setInputError(els.pseudo, els.pseudoStatus, els.pseudoError, 'Caractères non autorisés.');
            }
        }

        // Validation email (si renseigné)
        if (els.email && els.email.value.trim().length > 0) {
            const emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
            if (!emailRegex.test(els.email.value.trim())) {
                errors.push({ field: 'email', message: 'Adresse email invalide.' });
                setInputError(els.email, els.emailStatus, els.emailError, 'Adresse email invalide.');
            }
        }

        // Validation mot de passe
        if (els.motDePasse) {
            const mdpValue = els.motDePasse.value;
            if (mdpValue.length === 0) {
                errors.push({ field: 'motDePasse', message: 'Le mot de passe est obligatoire.' });
                setInputError(els.motDePasse, null, null, '');
            } else if (mdpValue.length < 6) {
                errors.push({ field: 'motDePasse', message: 'Le mot de passe doit contenir au moins 6 caractères.' });
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
        // Les erreurs sont déjà affichées par setInputError dans validateAllFields
        // On peut ajouter un message général en haut si besoin
        console.log('Erreurs de validation :', errors);
    }

    /* ==========================================================
     SECTION 8 : GESTION DU LOADER
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
     SECTION 9 : UTILITAIRES VISUELS
     ========================================================== */

    /**
     * Marque un input comme valide
     */
    function setInputValid(input, statusEl) {
        if (!input) return;
        input.classList.remove('input-error');
        input.classList.add('input-valid');
        
        if (statusEl) {
            statusEl.innerHTML = '<i class="fas fa-check-circle"></i>';
            statusEl.className = 'input-status valid';
        }

        // Cacher le message d'erreur associé
        const errorEl = input.closest('.form-group')?.querySelector('.form-error');
        if (errorEl) {
            errorEl.textContent = '';
        }
    }

    /**
     * Marque un input comme invalide
     */
    function setInputError(input, statusEl, errorEl, message) {
        if (!input) return;
        input.classList.remove('input-valid');
        input.classList.add('input-error');
        
        if (statusEl) {
            statusEl.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
            statusEl.className = 'input-status error';
        }

        if (errorEl && message) {
            errorEl.textContent = message;
        }
    }

    /**
     * Réinitialise le statut d'un input
     */
    function resetInputStatus(input, statusEl, errorEl) {
        if (!input) return;
        input.classList.remove('input-valid', 'input-error');
        
        if (statusEl) {
            statusEl.innerHTML = '';
            statusEl.className = 'input-status';
        }

        if (errorEl) {
            errorEl.textContent = '';
        }
    }

    /**
     * Animation de secousse pour les champs invalides
     */
    function shakeElement(element) {
        if (!element) return;
        
        element.style.animation = 'none';
        element.offsetHeight; // Force reflow
        element.style.animation = 'shake 0.5s ease';
        
        element.addEventListener('animationend', function handler() {
            element.removeEventListener('animationend', handler);
            element.style.animation = '';
        });
    }

    /* ==========================================================
     SECTION 10 : ANIMATIONS DES INPUTS
     ========================================================== */
    function initInputAnimations() {
        const inputs = document.querySelectorAll('.form-group input[type="text"], .form-group input[type="email"], .form-group input[type="password"]');
        
        inputs.forEach(function(input) {
            // Animation du label quand l'input est focus
            input.addEventListener('focus', function() {
                const label = this.closest('.form-group')?.querySelector('label');
                if (label) {
                    label.style.transition = 'all 0.3s ease';
                }
            });

            input.addEventListener('blur', function() {
                const label = this.closest('.form-group')?.querySelector('label');
                if (label) {
                    label.style.transition = 'all 0.3s ease';
                }
            });
        });
    }

    /* ==========================================================
     SECTION 11 : ANIMATION SHAKE (ajoutée au DOM)
     ========================================================== */
    const shakeStyle = document.createElement('style');
    shakeStyle.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-6px); }
            20%, 40%, 60%, 80% { transform: translateX(6px); }
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-5px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(shakeStyle);

    /* ==========================================================
     SECTION 12 : EMPÊCHER LA SOUMISSION AVEC ENTRÉE
     ========================================================== */
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && e.target.tagName === 'INPUT') {
            const form = e.target.closest('form');
            if (form && form.id === 'formInscription') {
                // Vérifier si on est sur le dernier champ
                const inputs = form.querySelectorAll('input:not([type="checkbox"])');
                const lastInput = inputs[inputs.length - 1];
                
                if (e.target === lastInput) {
                    // Sur le dernier champ, soumettre
                    // La validation se fera dans le submit event
                } else {
                    // Passer au champ suivant
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