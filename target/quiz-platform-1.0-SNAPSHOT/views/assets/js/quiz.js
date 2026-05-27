/**
 * ============================================================
 * FICHIER : views/assets/js/quiz.js
 * RÔLE    : Scripts de l'interface de quiz AmijayQuiz
 *           - Gestion du chronomètre (timer) avec localStorage
 *           - Navigation entre les questions
 *           - Modal de confirmation (terminer le quiz)
 *           - Sélection des propositions
 *           - Raccourcis clavier
 *           - Protection contre la fermeture accidentelle
 *           - Sauvegarde automatique de la réponse sélectionnée
 *           - Indicateurs de progression cliquables
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initTimer();
        initPropositionsSelection();
        initIndicateursClick();
        initKeyboardShortcuts();
        initModalListeners();
        initPageProtection();
        initFocusManagement();
    });

    /* ==========================================================
     SECTION 2 : CONFIGURATION (PASSÉE PAR LE JSP)
     ========================================================== */
    var CONFIG = window.QUIZ_CONFIG || {
        tempsTotal: 180,
        questionCourante: 1,
        totalQuestions: 10,
        contextPath: '',
        quizId: 0
    };

    /* ==========================================================
     SECTION 3 : GESTION DU CHRONOMÈTRE (avec localStorage)
     ========================================================== */
    function initTimer() {
        var timerDisplay = document.getElementById('timerDisplay');
        var timerBox = document.getElementById('timerBox');
        var timerLabel = document.getElementById('timerLabel');
        
        if (!timerDisplay || !timerBox) return;

        // Clé unique pour ce quiz dans le localStorage
        var storageKey = 'quiz_timer_' + CONFIG.quizId;
        var tempsRestant;
        
        // Vérifier si un timer existe déjà dans le localStorage
        var savedData = localStorage.getItem(storageKey);
        
        if (savedData) {
            // Restaurer le timer existant
            var data = JSON.parse(savedData);
            var now = Math.floor(Date.now() / 1000);
            tempsRestant = data.endTime - now;
            
            // Si le temps est écoulé, soumettre
            if (tempsRestant <= 0) {
                localStorage.removeItem(storageKey);
                timerDisplay.textContent = '00:00';
                timerBox.classList.add('urgent');
                if (timerLabel) timerLabel.textContent = 'restant';
                setTimeout(function() {
                    soumettreQuiz('terminer');
                }, 800);
                return;
            }
        } else {
            // Nouveau timer : 3 minutes (180 secondes)
            tempsRestant = CONFIG.tempsTotal || 180;
            
            // Sauvegarder dans le localStorage
            var now = Math.floor(Date.now() / 1000);
            localStorage.setItem(storageKey, JSON.stringify({
                endTime: now + tempsRestant,
                quizId: CONFIG.quizId
            }));
        }

        // Mise à jour initiale de l'affichage
        mettreAJourAffichageTimer(tempsRestant, timerDisplay, timerBox, timerLabel);

        // Intervalle de 1 seconde
        var timerInterval = setInterval(function() {
            tempsRestant--;
            
            // Mettre à jour le localStorage
            var now = Math.floor(Date.now() / 1000);
            localStorage.setItem(storageKey, JSON.stringify({
                endTime: now + tempsRestant,
                quizId: CONFIG.quizId
            }));
            
            if (tempsRestant <= 0) {
                // Temps écoulé
                clearInterval(timerInterval);
                localStorage.removeItem(storageKey);
                timerDisplay.textContent = '00:00';
                timerBox.classList.add('urgent');
                if (timerLabel) timerLabel.textContent = 'restant';
                
                // Soumettre automatiquement le quiz
                setTimeout(function() {
                    soumettreQuiz('terminer');
                }, 800);
                
                return;
            }
            
            mettreAJourAffichageTimer(tempsRestant, timerDisplay, timerBox, timerLabel);
            
        }, 1000);

        // Stocker l'intervalle pour le nettoyage
        window._quizTimerInterval = timerInterval;
        window._quizTimerStorageKey = storageKey;
    }

    function mettreAJourAffichageTimer(tempsRestant, timerDisplay, timerBox, timerLabel) {
        // Formatage MM:SS
        var minutes = Math.floor(tempsRestant / 60);
        var secondes = tempsRestant % 60;
        timerDisplay.textContent = 
            String(minutes).padStart(2, '0') + ':' + String(secondes).padStart(2, '0');
        
        // États visuels du timer
        timerBox.classList.remove('warning', 'urgent');
        
        if (tempsRestant <= 30) {
            timerBox.classList.add('urgent');
        } else if (tempsRestant <= 60) {
            timerBox.classList.add('warning');
        }
        
        // Mettre à jour le label
        if (timerLabel) {
            if (tempsRestant <= 60) {
                timerLabel.textContent = 'restant';
            } else {
                timerLabel.textContent = 'restantes';
            }
        }
    }

    /* ==========================================================
     SECTION 4 : SÉLECTION DES PROPOSITIONS
     ========================================================== */
    function initPropositionsSelection() {
        var propositionItems = document.querySelectorAll('.proposition-item');
        if (propositionItems.length === 0) return;

        propositionItems.forEach(function(item) {
            var radio = item.querySelector('input[type="radio"]');
            if (!radio) return;

            // Quand le radio change (sélection)
            radio.addEventListener('change', function() {
                // Retirer la classe 'selected' de toutes les propositions
                propositionItems.forEach(function(pi) {
                    pi.classList.remove('selected');
                });
                
                // Ajouter la classe 'selected' à la proposition choisie
                item.classList.add('selected');
                
                // Effet sonore visuel (pulsation)
                var label = item.querySelector('.proposition-label');
                if (label) {
                    label.style.transform = 'scale(1.02)';
                    setTimeout(function() {
                        label.style.transform = 'scale(1)';
                    }, 200);
                }
            });

            // Permettre le clic sur toute la carte pour sélectionner
            var label = item.querySelector('.proposition-label');
            if (label) {
                label.addEventListener('click', function(e) {
                    // Si le radio est déjà checked, ne rien faire (éviter de décocher)
                    if (radio.checked) {
                        // Garder la sélection
                    } else {
                        radio.checked = true;
                        radio.dispatchEvent(new Event('change'));
                    }
                });
            }
        });

        // Mettre en évidence la proposition déjà sélectionnée (si on revient à une question)
        var checkedRadio = document.querySelector('.proposition-item input[type="radio"]:checked');
        if (checkedRadio) {
            var parentItem = checkedRadio.closest('.proposition-item');
            if (parentItem) {
                parentItem.classList.add('selected');
            }
        }
    }

    /* ==========================================================
     SECTION 5 : INDICATEURS DE QUESTIONS CLIQUABLES
     ========================================================== */
    function initIndicateursClick() {
        var indicateurs = document.querySelectorAll('.indicateur-dot');
        if (indicateurs.length === 0) return;

        indicateurs.forEach(function(dot, index) {
            // Rendre le point cliquable visuellement
            dot.style.cursor = 'pointer';
            dot.setAttribute('role', 'button');
            dot.setAttribute('tabindex', '0');
            dot.setAttribute('aria-label', 'Aller à la question ' + (index + 1));

            // Navigation via les points (seulement si la question est déjà visitée)
            dot.addEventListener('click', function() {
                if (index < CONFIG.questionCourante - 1) {
                    // Question précédente : naviguer en arrière
                    // Pour l'instant, on ne permet que via précédent
                }
            });
        });
    }

    /* ==========================================================
     SECTION 6 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ignorer si on est dans le modal
            var modalActive = document.getElementById('modalConfirmation')?.classList.contains('active');
            if (modalActive) {
                if (e.key === 'Escape') {
                    fermerModalConfirmation();
                }
                if (e.key === 'Enter') {
                    terminerQuiz();
                }
                return;
            }

            switch (e.key) {
                case 'ArrowLeft':
                    // Question précédente
                    if (e.altKey || e.metaKey) {
                        e.preventDefault();
                        naviguer('precedent');
                    }
                    break;
                    
                case 'ArrowRight':
                    // Question suivante
                    if (e.altKey || e.metaKey) {
                        e.preventDefault();
                        naviguer('suivant');
                    }
                    break;
                    
                case 'a':
                case 'A':
                    // Sélectionner proposition A
                    if (!e.ctrlKey && !e.metaKey && !e.altKey) {
                        selectProposition(1);
                    }
                    break;
                    
                case 'b':
                case 'B':
                    // Sélectionner proposition B
                    if (!e.ctrlKey && !e.metaKey && !e.altKey) {
                        selectProposition(2);
                    }
                    break;
                    
                case 'c':
                case 'C':
                    // Sélectionner proposition C
                    if (!e.ctrlKey && !e.metaKey && !e.altKey) {
                        selectProposition(3);
                    }
                    break;
                    
                case 'd':
                case 'D':
                    // Sélectionner proposition D
                    if (!e.ctrlKey && !e.metaKey && !e.altKey) {
                        selectProposition(4);
                    }
                    break;
                    
                case 'Enter':
                    // Si sur la dernière question, ouvrir le modal
                    var btnTerminer = document.querySelector('.btn-terminer');
                    if (btnTerminer && !btnTerminer.disabled) {
                        e.preventDefault();
                        ouvrirModalConfirmation();
                    } else {
                        // Sinon, passer à la question suivante
                        var btnSuivant = document.querySelector('.btn-suivant');
                        if (btnSuivant && !btnSuivant.disabled) {
                            e.preventDefault();
                            naviguer('suivant');
                        }
                    }
                    break;
                    
                case 'Escape':
                    // Ne rien faire, éviter la fermeture accidentelle
                    e.preventDefault();
                    break;
            }
        });
    }

    /**
     * Sélectionner une proposition par son numéro (1-4)
     */
    function selectProposition(numero) {
        var radio = document.getElementById('reponse' + numero);
        if (radio) {
            radio.checked = true;
            radio.dispatchEvent(new Event('change'));
        }
    }

    /* ==========================================================
     SECTION 7 : NAVIGATION
     ========================================================== */
    window.naviguer = function(action) {
        // Désactiver la protection beforeunload temporairement
        window._allowNavigation = true;
        
        // Soumettre le formulaire
        var formAction = document.getElementById('formAction');
        if (formAction) {
            formAction.value = action;
        }
        
        var form = document.getElementById('quizForm');
        if (form) {
            form.submit();
        }
    };

    /* ==========================================================
     SECTION 8 : MODAL DE CONFIRMATION
     ========================================================== */
    function initModalListeners() {
        var modal = document.getElementById('modalConfirmation');
        if (!modal) return;

        // Fermer en cliquant en dehors
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                fermerModalConfirmation();
            }
        });

        // Fermer avec Échap
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && modal.classList.contains('active')) {
                fermerModalConfirmation();
            }
        });
    }

    window.ouvrirModalConfirmation = function() {
        var modal = document.getElementById('modalConfirmation');
        if (!modal) return;
        
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Focus sur le bouton Annuler
        setTimeout(function() {
            var btnAnnuler = modal.querySelector('.btn-modal-annuler');
            if (btnAnnuler) btnAnnuler.focus();
        }, 300);
    };

    window.fermerModalConfirmation = function() {
        var modal = document.getElementById('modalConfirmation');
        if (!modal) return;
        
        modal.classList.remove('active');
        document.body.style.overflow = '';
    };

    window.terminerQuiz = function() {
        // Désactiver la protection
        window._allowNavigation = true;
        
        // Nettoyer le timer et le localStorage
        if (window._quizTimerInterval) {
            clearInterval(window._quizTimerInterval);
        }
        if (window._quizTimerStorageKey) {
            localStorage.removeItem(window._quizTimerStorageKey);
        }
        
        // Fermer le modal
        fermerModalConfirmation();
        
        // Soumettre
        var formAction = document.getElementById('formAction');
        if (formAction) {
            formAction.value = 'terminer';
        }
        
        var form = document.getElementById('quizForm');
        if (form) {
            // Effet de transition
            document.body.style.transition = 'opacity 0.3s ease';
            document.body.style.opacity = '0.7';
            
            form.submit();
        }
    };

    /* ==========================================================
     SECTION 9 : PROTECTION CONTRE LA FERMETURE ACCIDENTELLE
     ========================================================== */
    function initPageProtection() {
        window._allowNavigation = false;

        window.addEventListener('beforeunload', function(e) {
            if (window._allowNavigation) {
                return;
            }
            
            e.preventDefault();
            e.returnValue = 'Vous avez un quiz en cours. Vos réponses seront perdues. Voulez-vous vraiment quitter ?';
            return e.returnValue;
        });

        setTimeout(function() {
            window._allowNavigation = false;
        }, 1000);
    }

    /* ==========================================================
     SECTION 10 : GESTION DU FOCUS
     ========================================================== */
    function initFocusManagement() {
        setTimeout(function() {
            var firstLabel = document.querySelector('.proposition-label');
            if (firstLabel) {
                var labels = document.querySelectorAll('.proposition-label');
                labels.forEach(function(label) {
                    label.setAttribute('tabindex', '0');
                    
                    label.addEventListener('keydown', function(e) {
                        if (e.key === ' ' || e.key === 'Enter') {
                            e.preventDefault();
                            this.click();
                        }
                    });
                });
            }
        }, 200);
    }

    /* ==========================================================
     SECTION 11 : SOUMISSION DU FORMULAIRE
     ========================================================== */
    function soumettreQuiz(action) {
        // Nettoyer le localStorage
        if (window._quizTimerStorageKey) {
            localStorage.removeItem(window._quizTimerStorageKey);
        }
        
        var formAction = document.getElementById('formAction');
        if (formAction) {
            formAction.value = action;
        }
        
        var form = document.getElementById('quizForm');
        if (form) {
            window._allowNavigation = true;
            form.submit();
        }
    }

    /* ==========================================================
     SECTION 12 : ANIMATION DE TRANSITION ENTRE QUESTIONS
     ========================================================== */
    var form = document.getElementById('quizForm');
    if (form) {
        form.addEventListener('submit', function() {
            var questionCard = document.querySelector('.question-card');
            if (questionCard) {
                questionCard.style.transition = 'all 0.25s ease';
                questionCard.style.opacity = '0.5';
                questionCard.style.transform = 'scale(0.98)';
            }
        });
    }

    /* ==========================================================
     SECTION 13 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var timerBox = document.getElementById('timerBox');
        
        if (document.hidden) {
            if (timerBox) {
                timerBox.style.animationPlayState = 'paused';
            }
        } else {
            if (timerBox) {
                timerBox.style.animationPlayState = 'running';
            }
        }
    });

    /* ==========================================================
     SECTION 14 : NOTIFICATION DE SAUVEGARDE DE RÉPONSE
     ========================================================== */
    var propositionRadios = document.querySelectorAll('.proposition-item input[type="radio"]');
    propositionRadios.forEach(function(radio) {
        radio.addEventListener('change', function() {
            var item = this.closest('.proposition-item');
            if (item) {
                var check = item.querySelector('.proposition-check i');
                if (check) {
                    check.style.transform = 'scale(1.3)';
                    check.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    setTimeout(function() {
                        check.style.transform = 'scale(1)';
                    }, 300);
                }
            }
        });
    });

})();