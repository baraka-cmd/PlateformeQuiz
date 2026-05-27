/**
 * ============================================================
 * FICHIER : views/assets/js/cours-examen.js
 * RÔLE    : Scripts de la page d'examen final
 *           - Navigation entre questions
 *           - Gestion du timer (30 minutes)
 *           - Sauvegarde des réponses
 *           - Indicateurs de progression
 *           - Modal de confirmation
 *           - Raccourcis clavier
 *           - Protection contre fermeture accidentelle
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : VARIABLES GLOBALES
     ========================================================== */
    var currentIndex = 0;
    var totalQuestions = TOTAL_QUESTIONS || 20;
    var tempsRestant = TEMPS_RESTANT || 1800;
    var timerInterval = null;
    var examenTermine = false;

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initTimer();
        initIndicatorsClick();
        initKeyboardShortcuts();
        initModalListeners();
        initPageProtection();
        updateNavigationButtons();
        updateProgressBar();
        restoreSavedAnswers();
        initAutoSave();
    });

    /* ==========================================================
     SECTION 3 : GESTION DU TIMER
     ========================================================== */
    function initTimer() {
        var timerDisplay = document.getElementById('timerDisplay');
        var timerBox = document.getElementById('timerBox');
        if (!timerDisplay || !timerBox) return;

        // Restaurer le temps sauvegardé
        var savedTime = localStorage.getItem('examen_temps_restant');
        if (savedTime) {
            var saved = parseInt(savedTime);
            if (saved > 0 && saved <= TEMPS_RESTANT) {
                tempsRestant = saved;
            }
        }

        mettreAJourAffichageTimer();

        timerInterval = setInterval(function() {
            tempsRestant--;

            // Sauvegarder dans le localStorage
            localStorage.setItem('examen_temps_restant', tempsRestant);

            if (tempsRestant <= 0) {
                clearInterval(timerInterval);
                timerDisplay.textContent = '00:00';
                timerBox.classList.add('urgent');
                
                // Temps écoulé : soumettre automatiquement
                setTimeout(function() {
                    if (!examenTermine) {
                        terminerExamen();
                    }
                }, 1000);
                return;
            }

            mettreAJourAffichageTimer();
        }, 1000);
    }

    function mettreAJourAffichageTimer() {
        var timerDisplay = document.getElementById('timerDisplay');
        var timerBox = document.getElementById('timerBox');
        if (!timerDisplay || !timerBox) return;

        var minutes = Math.floor(tempsRestant / 60);
        var secondes = tempsRestant % 60;
        timerDisplay.textContent = 
            String(minutes).padStart(2, '0') + ':' + String(secondes).padStart(2, '0');

        timerBox.classList.remove('warning', 'urgent');
        if (tempsRestant <= 60) {
            timerBox.classList.add('urgent');
        } else if (tempsRestant <= 300) {
            timerBox.classList.add('warning');
        }
    }

    /* ==========================================================
     SECTION 4 : NAVIGATION ENTRE QUESTIONS
     ========================================================== */
    window.naviguerQuestion = function(direction) {
        var newIndex = currentIndex + direction;

        if (newIndex < 0 || newIndex >= totalQuestions) return;

        // Sauvegarder la réponse actuelle avant de naviguer
        sauvegarderReponse(currentIndex);

        // Cacher la question actuelle
        var currentSlide = document.getElementById('questionSlide' + currentIndex);
        if (currentSlide) currentSlide.style.display = 'none';

        // Afficher la nouvelle question
        var newSlide = document.getElementById('questionSlide' + newIndex);
        if (newSlide) {
            newSlide.style.display = 'block';
            newSlide.style.animation = 'questionFadeIn 0.35s ease';
        }

        currentIndex = newIndex;

        updateNavigationButtons();
        updateProgressBar();
        updateIndicators();

        // Scroll vers le haut de la question
        var questionCard = document.getElementById('questionCard');
        if (questionCard) {
            questionCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };

    function updateNavigationButtons() {
        var btnPrecedent = document.getElementById('btnPrecedent');
        var btnSuivant = document.getElementById('btnSuivant');
        var btnTerminer = document.getElementById('btnTerminer');

        if (btnPrecedent) btnPrecedent.disabled = (currentIndex === 0);
        
        if (btnSuivant && btnTerminer) {
            if (currentIndex === totalQuestions - 1) {
                btnSuivant.style.display = 'none';
                btnTerminer.style.display = 'flex';
            } else {
                btnSuivant.style.display = 'flex';
                btnTerminer.style.display = 'none';
            }
        }

        // Mettre à jour le compteur
        var navCurrent = document.getElementById('navCurrent');
        var currentQuestionSpan = document.getElementById('currentQuestion');
        if (navCurrent) navCurrent.textContent = currentIndex + 1;
        if (currentQuestionSpan) currentQuestionSpan.textContent = currentIndex + 1;
    }

    function updateProgressBar() {
        var progressFill = document.getElementById('progressFill');
        if (progressFill) {
            var pourcentage = ((currentIndex + 1) / totalQuestions) * 100;
            progressFill.style.width = pourcentage + '%';
        }
    }

    /* ==========================================================
     SECTION 5 : INDICATEURS DE QUESTIONS
     ========================================================== */
    function initIndicatorsClick() {
        var indicators = document.querySelectorAll('.indicator-dot');
        if (indicators.length === 0) return;

        indicators.forEach(function(dot, index) {
            dot.addEventListener('click', function() {
                if (index === currentIndex) return;

                // Sauvegarder la réponse actuelle
                sauvegarderReponse(currentIndex);

                // Cacher l'actuelle
                var currentSlide = document.getElementById('questionSlide' + currentIndex);
                if (currentSlide) currentSlide.style.display = 'none';

                // Afficher la nouvelle
                var newSlide = document.getElementById('questionSlide' + index);
                if (newSlide) {
                    newSlide.style.display = 'block';
                    newSlide.style.animation = 'questionFadeIn 0.35s ease';
                }

                currentIndex = index;
                updateNavigationButtons();
                updateProgressBar();
                updateIndicators();
            });
        });

        updateIndicators();
    }

    function updateIndicators() {
        var indicators = document.querySelectorAll('.indicator-dot');
        indicators.forEach(function(dot, index) {
            dot.classList.remove('active', 'repondu');
            if (index === currentIndex) {
                dot.classList.add('active');
            } else if (estRepondu(index)) {
                dot.classList.add('repondu');
            }
        });
    }

    /* ==========================================================
     SECTION 6 : SAUVEGARDE DES RÉPONSES
     ========================================================== */
    function sauvegarderReponse(index) {
        var radios = document.getElementsByName('reponse_' + index);
        var reponse = null;

        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked) {
                reponse = radios[i].value;
                break;
            }
        }

        var reponses = JSON.parse(localStorage.getItem('examen_reponses') || '{}');
        if (reponse !== null) {
            reponses[index] = reponse;
        } else {
            delete reponses[index];
        }
        localStorage.setItem('examen_reponses', JSON.stringify(reponses));
    }

    function restoreSavedAnswers() {
        var reponses = JSON.parse(localStorage.getItem('examen_reponses') || '{}');

        for (var index in reponses) {
            var radio = document.getElementById('reponse_' + index + '_' + reponses[index]);
            if (radio) {
                radio.checked = true;
            }
        }

        updateIndicators();
    }

    function estRepondu(index) {
        var radios = document.getElementsByName('reponse_' + index);
        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked) return true;
        }

        var reponses = JSON.parse(localStorage.getItem('examen_reponses') || '{}');
        return reponses[index] !== undefined;
    }

    function initAutoSave() {
        // Sauvegarder automatiquement quand une réponse est sélectionnée
        document.addEventListener('change', function(e) {
            if (e.target && e.target.classList.contains('proposition-radio')) {
                sauvegarderReponse(currentIndex);
                updateIndicators();
            }
        });
    }

    /* ==========================================================
     SECTION 7 : MODAL DE CONFIRMATION
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

        // Vérifier les questions non répondues
        var nbNonRepondues = 0;
        for (var i = 0; i < totalQuestions; i++) {
            if (!estRepondu(i)) nbNonRepondues++;
        }

        var divNonRepondues = document.getElementById('questionsNonRepondues');
        var nbSpan = document.getElementById('nbNonRepondues');

        if (nbNonRepondues > 0 && divNonRepondues && nbSpan) {
            divNonRepondues.style.display = 'block';
            nbSpan.textContent = nbNonRepondues;
        } else if (divNonRepondues) {
            divNonRepondues.style.display = 'none';
        }

        modal.classList.add('active');
        document.body.style.overflow = 'hidden';

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

    window.terminerExamen = function() {
        if (examenTermine) return;
        examenTermine = true;

        // Sauvegarder la réponse actuelle
        sauvegarderReponse(currentIndex);

        // Arrêter le timer
        if (timerInterval) clearInterval(timerInterval);

        // Nettoyer le localStorage
        localStorage.removeItem('examen_temps_restant');
        localStorage.removeItem('examen_reponses');

        // Fermer le modal
        fermerModalConfirmation();

        // Effet de transition
        document.body.style.transition = 'opacity 0.3s ease';
        document.body.style.opacity = '0.7';

        // Soumettre le formulaire
        var form = document.getElementById('examenForm');
        if (form) {
            form.submit();
        }
    };

    /* ==========================================================
     SECTION 8 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            var modalActive = document.getElementById('modalConfirmation')?.classList.contains('active');

            if (modalActive) {
                if (e.key === 'Escape') fermerModalConfirmation();
                if (e.key === 'Enter') terminerExamen();
                return;
            }

            switch (e.key) {
                case 'ArrowLeft':
                    if (e.altKey) { e.preventDefault(); naviguerQuestion(-1); }
                    break;
                case 'ArrowRight':
                    if (e.altKey) { e.preventDefault(); naviguerQuestion(1); }
                    break;
                case 'a': case 'A':
                    if (!e.ctrlKey && !e.metaKey) selectProposition(currentIndex, 1);
                    break;
                case 'b': case 'B':
                    if (!e.ctrlKey && !e.metaKey) selectProposition(currentIndex, 2);
                    break;
                case 'c': case 'C':
                    if (!e.ctrlKey && !e.metaKey) selectProposition(currentIndex, 3);
                    break;
                case 'd': case 'D':
                    if (!e.ctrlKey && !e.metaKey) selectProposition(currentIndex, 4);
                    break;
                case 'Enter':
                    if (currentIndex === totalQuestions - 1) {
                        e.preventDefault();
                        ouvrirModalConfirmation();
                    } else {
                        e.preventDefault();
                        naviguerQuestion(1);
                    }
                    break;
                case 'Escape':
                    e.preventDefault();
                    break;
            }
        });
    }

    function selectProposition(questionIndex, reponse) {
        var radio = document.getElementById('reponse_' + questionIndex + '_' + reponse);
        if (radio) {
            radio.checked = true;
            radio.dispatchEvent(new Event('change'));
        }
    }

    /* ==========================================================
     SECTION 9 : PROTECTION CONTRE FERMETURE ACCIDENTELLE
     ========================================================== */
    function initPageProtection() {
        window.addEventListener('beforeunload', function(e) {
            if (!examenTermine) {
                e.preventDefault();
                e.returnValue = 'Vous avez un examen en cours. Vos réponses seront perdues. Voulez-vous vraiment quitter ?';
                return e.returnValue;
            }
        });
    }

    /* ==========================================================
     SECTION 10 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        var timerBox = document.getElementById('timerBox');
        if (document.hidden) {
            if (timerBox) timerBox.style.animationPlayState = 'paused';
        } else {
            if (timerBox) timerBox.style.animationPlayState = 'running';
        }
    });

})();