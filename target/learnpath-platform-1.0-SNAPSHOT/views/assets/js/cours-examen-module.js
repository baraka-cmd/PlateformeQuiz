// ============================================================
// FICHIER : views/assets/js/cours-examen-module.js
// RÔLE    : Script de la page d'examen d'un module de cours
//           - Navigation entre les questions
//           - Gestion des reponses (radio buttons)
//           - Barre de progression dynamique
//           - Indicateurs de questions (points numerotes)
//           - Statut de question (En attente / Repondu)
//           - Modal de confirmation avant de terminer
//           - Resume dans la modale
//           - Raccourcis clavier
//           - Protection contre la fermeture accidentelle
//           - Sauvegarde automatique dans le localStorage
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : VARIABLES GLOBALES
     ========================================================== */
    var NOMBRE_QUESTIONS = window.NOMBRE_QUESTIONS || 20;
    var CONTEXT_PATH = window.location.pathname.split('/')[1] || '';
    var STORAGE_KEY = 'LearnPath_examen_module_reponses';
    var STORAGE_TIMESTAMP_KEY = 'LearnPath_examen_module_timestamp';

    var reponsesUtilisateur = new Array(NOMBRE_QUESTIONS).fill(null);
    var currentQuestion = 0;
    var examenSoumis = false;

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        chargerReponsesSauvegardees();
        restaurerReponsesExistantes();
        updateButtonsState();
        updateActiveIndicator();
        updateAnsweredCount();
        updateProgressFill();
        initKeyboardShortcuts();
        initBeforeUnloadProtection();
        initModalCloseEvents();
        console.log('Examen Module - Initialise avec ' + NOMBRE_QUESTIONS + ' questions');
    });

    /* ==========================================================
     SECTION 3 : GESTION DES REPONSES
     ========================================================== */

    /**
     * Appelee quand l'utilisateur selectionne une reponse
     * @param {number} index - Index de la question (0-based)
     * @param {number} valeur - Valeur de la reponse (1, 2, 3 ou 4)
     */
    window.onReponseChange = function(index, valeur) {
        if (examenSoumis) return;

        reponsesUtilisateur[index] = valeur;
        updateIndicator(index);
        updateQuestionStatut(index, true);
        updateAnsweredCount();
        updateProgressFill();
        sauvegarderReponses();
    };

    /**
     * Restaure les reponses deja selectionnees (au chargement ou retour)
     */
    function restaurerReponsesExistantes() {
        for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
            var radios = document.getElementsByName('reponse_' + i);
            if (!radios || radios.length === 0) continue;

            for (var j = 0; j < radios.length; j++) {
                if (radios[j].checked) {
                    reponsesUtilisateur[i] = parseInt(radios[j].value, 10);
                    updateIndicator(i);
                    updateQuestionStatut(i, true);
                    break;
                }
            }
        }
    }

    /* ==========================================================
     SECTION 4 : INDICATEURS VISUELS
     ========================================================== */

    /**
     * Met a jour l'indicateur de navigation (point numerote)
     * @param {number} index - Index de la question
     */
    function updateIndicator(index) {
        var indicator = document.getElementById('indicator' + index);
        if (!indicator) return;

        if (reponsesUtilisateur[index] !== null) {
            indicator.classList.add('answered');
        } else {
            indicator.classList.remove('answered');
        }
    }

    /**
     * Met a jour le statut texte de la question (En attente / Repondu)
     * @param {number} index - Index de la question
     * @param {boolean} estRepondu - True si la question a une reponse
     */
    function updateQuestionStatut(index, estRepondu) {
        var statut = document.getElementById('questionStatut' + index);
        if (!statut) return;

        if (estRepondu) {
            statut.innerHTML = '<i class="fas fa-check-circle"></i> Repondu';
            statut.style.color = '#27ae60';
        } else {
            statut.innerHTML = '<i class="fas fa-circle"></i> En attente';
            statut.style.color = '#8b8fa3';
        }
    }

    /**
     * Met a jour le compteur de questions repondues
     */
    function updateAnsweredCount() {
        var count = 0;
        for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
            if (reponsesUtilisateur[i] !== null) count++;
        }

        var answeredSpan = document.getElementById('answeredCount');
        if (answeredSpan) {
            answeredSpan.innerText = count;
        }
    }

    /**
     * Met a jour la barre de progression
     */
    function updateProgressFill() {
        var count = 0;
        for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
            if (reponsesUtilisateur[i] !== null) count++;
        }

        var percent = (count / NOMBRE_QUESTIONS) * 100;
        var fill = document.getElementById('progressFill');
        if (fill) {
            fill.style.width = percent + '%';
        }
    }

    /**
     * Met a jour l'indicateur actif (question courante)
     */
    function updateActiveIndicator() {
        for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
            var indicator = document.getElementById('indicator' + i);
            if (!indicator) continue;

            if (i === currentQuestion) {
                indicator.classList.add('active');
            } else {
                indicator.classList.remove('active');
            }
        }
    }

    /* ==========================================================
     SECTION 5 : NAVIGATION
     ========================================================== */

    /**
     * Navigue vers la question precedente ou suivante
     * @param {number} direction - -1 pour precedent, 1 pour suivant
     */
    window.naviguerQuestion = function(direction) {
        var newIndex = currentQuestion + direction;

        if (newIndex >= 0 && newIndex < NOMBRE_QUESTIONS) {
            // Masquer la question courante
            var currentCard = document.getElementById('questionCard' + currentQuestion);
            if (currentCard) {
                currentCard.style.display = 'none';
            }

            // Afficher la nouvelle question
            currentQuestion = newIndex;
            var newCard = document.getElementById('questionCard' + currentQuestion);
            if (newCard) {
                newCard.style.display = 'block';
            }

            // Mettre a jour les compteurs
            var displayElement = document.getElementById('currentQuestionDisplay');
            if (displayElement) {
                displayElement.innerText = (currentQuestion + 1);
            }

            var navCurrent = document.getElementById('navCurrent');
            if (navCurrent) {
                navCurrent.innerText = (currentQuestion + 1);
            }

            updateActiveIndicator();
            updateButtonsState();

            // Scroll vers le haut de la question
            if (newCard) {
                newCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        }
    };

    /**
     * Navigue directement vers une question specifique
     * @param {number} index - Index de la question cible
     */
    window.naviguerVersQuestion = function(index) {
        if (index < 0 || index >= NOMBRE_QUESTIONS) return;
        if (index === currentQuestion) return;

        // Masquer la question courante
        var currentCard = document.getElementById('questionCard' + currentQuestion);
        if (currentCard) {
            currentCard.style.display = 'none';
        }

        // Afficher la question cible
        currentQuestion = index;
        var newCard = document.getElementById('questionCard' + currentQuestion);
        if (newCard) {
            newCard.style.display = 'block';
        }

        // Mettre a jour les compteurs
        var displayElement = document.getElementById('currentQuestionDisplay');
        if (displayElement) {
            displayElement.innerText = (currentQuestion + 1);
        }

        var navCurrent = document.getElementById('navCurrent');
        if (navCurrent) {
            navCurrent.innerText = (currentQuestion + 1);
        }

        updateActiveIndicator();
        updateButtonsState();

        // Scroll vers le haut de la question
        if (newCard) {
            newCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };

    /**
     * Active/desactive les boutons de navigation selon la position
     */
    function updateButtonsState() {
        var btnPrecedent = document.getElementById('btnPrecedent');
        var btnSuivant = document.getElementById('btnSuivant');

        if (btnPrecedent) {
            var estPremiere = currentQuestion === 0;
            btnPrecedent.style.opacity = estPremiere ? '0.4' : '1';
            btnPrecedent.style.pointerEvents = estPremiere ? 'none' : 'auto';
            btnPrecedent.disabled = estPremiere;
        }

        if (btnSuivant) {
            var estDerniere = currentQuestion === NOMBRE_QUESTIONS - 1;
            btnSuivant.style.display = estDerniere ? 'none' : 'inline-flex';
        }
    }

    /* ==========================================================
     SECTION 6 : MODAL DE CONFIRMATION
     ========================================================== */

    /**
     * Ouvre la modale de confirmation avant de terminer
     */
    window.ouvrirModalConfirmation = function() {
        var nonRepondues = 0;
        var repondues = 0;

        for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
            if (reponsesUtilisateur[i] === null) {
                nonRepondues++;
            } else {
                repondues++;
            }
        }

        // Avertissement questions sans reponse
        var warningDiv = document.getElementById('questionsNonRepondues');
        var nbSpan = document.getElementById('nbNonRepondues');

        if (nonRepondues > 0 && warningDiv && nbSpan) {
            nbSpan.innerText = nonRepondues;
            warningDiv.style.display = 'flex';
        } else if (warningDiv) {
            warningDiv.style.display = 'none';
        }

        // Resume dans la modale
        var modalResume = document.getElementById('modalResume');
        var modalRepondues = document.getElementById('modalRepondues');
        var modalMiniProgress = document.getElementById('modalMiniProgressFill');

        if (modalResume && modalRepondues && modalMiniProgress) {
            modalResume.style.display = 'block';
            modalRepondues.innerText = repondues;
            modalMiniProgress.style.width = ((repondues / NOMBRE_QUESTIONS) * 100) + '%';
        }

        // Afficher la modale
        var modal = document.getElementById('modalConfirmation');
        if (modal) {
            modal.style.display = 'flex';
            document.body.style.overflow = 'hidden';
            modal.setAttribute('aria-hidden', 'false');

            // Focus sur le bouton Annuler
            var btnAnnuler = modal.querySelector('.btn-modal-annuler');
            if (btnAnnuler) {
                setTimeout(function() { btnAnnuler.focus(); }, 100);
            }
        }
    };

    /**
     * Ferme la modale de confirmation
     */
    window.fermerModalConfirmation = function() {
        var modal = document.getElementById('modalConfirmation');
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = '';
            modal.setAttribute('aria-hidden', 'true');
        }
    };

    /**
     * Soumet l'examen
     */
    window.terminerExamen = function() {
        if (examenSoumis) return;
        examenSoumis = true;

        // Desactiver le bouton et afficher le loader
        var btnConfirmer = document.querySelector('.btn-modal-confirmer');
        if (btnConfirmer) {
            btnConfirmer.disabled = true;
            btnConfirmer.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Envoi en cours...';
            btnConfirmer.style.opacity = '0.8';
        }

        // Nettoyer le localStorage
        nettoyerStockageLocal();

        // Soumettre le formulaire
        var form = document.getElementById('examenForm');
        if (form) {
            form.submit();
        }
    };
    
    
    /**
 * Ferme la notification de repassage avec animation
 */
window.fermerNotificationRepassage = function() {
    var notification = document.querySelector('.repassage-notification');
    if (!notification) return;

    notification.style.transition = 'all 0.3s ease';
    notification.style.opacity = '0';
    notification.style.transform = 'translateY(-15px)';
    notification.style.maxHeight = notification.offsetHeight + 'px';

    setTimeout(function() {
        notification.style.maxHeight = '0';
        notification.style.padding = '0';
        notification.style.margin = '0';
        notification.style.borderWidth = '0';
        notification.style.overflow = 'hidden';
    }, 200);

    setTimeout(function() {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 500);
};

    /**
     * Initialise les evenements de fermeture de la modale
     */
    function initModalCloseEvents() {
        var modal = document.getElementById('modalConfirmation');
        if (!modal) return;

        // Fermer en cliquant sur l'overlay
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                fermerModalConfirmation();
            }
        });

        // Fermer avec la touche Escape
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && modal.style.display === 'flex') {
                fermerModalConfirmation();
            }
        });
    }

    /* ==========================================================
     SECTION 7 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ignorer si la modale est ouverte
            var modal = document.getElementById('modalConfirmation');
            if (modal && modal.style.display === 'flex') return;

            // Ignorer si on est dans un champ de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.isContentEditable) {
                return;
            }

            switch (e.key) {
                case 'ArrowLeft':
                    if (!e.ctrlKey && !e.altKey) {
                        e.preventDefault();
                        naviguerQuestion(-1);
                    }
                    break;

                case 'ArrowRight':
                    if (!e.ctrlKey && !e.altKey) {
                        e.preventDefault();
                        naviguerQuestion(1);
                    }
                    break;

                case 'a':
                case 'A':
                    if (!e.ctrlKey && !e.altKey) {
                        selectionnerReponse(currentQuestion, 1);
                    }
                    break;

                case 'b':
                case 'B':
                    if (!e.ctrlKey && !e.altKey) {
                        selectionnerReponse(currentQuestion, 2);
                    }
                    break;

                case 'c':
                case 'C':
                    if (!e.ctrlKey && !e.altKey) {
                        selectionnerReponse(currentQuestion, 3);
                    }
                    break;

                case 'd':
                case 'D':
                    if (!e.ctrlKey && !e.altKey) {
                        selectionnerReponse(currentQuestion, 4);
                    }
                    break;

                case 'Enter':
                    if (e.ctrlKey) {
                        e.preventDefault();
                        ouvrirModalConfirmation();
                    }
                    break;
            }
        });
    }

    /**
     * Selectionne une reponse par son numero (raccourci clavier A, B, C, D)
     * @param {number} questionIndex - Index de la question
     * @param {number} reponseNumero - Numero de la reponse (1=A, 2=B, 3=C, 4=D)
     */
    function selectionnerReponse(questionIndex, reponseNumero) {
        var radios = document.getElementsByName('reponse_' + questionIndex);
        if (!radios || radios.length === 0) return;

        for (var i = 0; i < radios.length; i++) {
            if (parseInt(radios[i].value, 10) === reponseNumero) {
                radios[i].checked = true;
                onReponseChange(questionIndex, reponseNumero);
                break;
            }
        }
    }

    /* ==========================================================
     SECTION 8 : SAUVEGARDE AUTOMATIQUE (LOCALSTORAGE)
     ========================================================== */

    /**
     * Sauvegarde les reponses dans le localStorage
     */
    function sauvegarderReponses() {
        try {
            var data = {
                reponses: reponsesUtilisateur,
                timestamp: Date.now(),
                questions: NOMBRE_QUESTIONS
            };
            localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
            localStorage.setItem(STORAGE_TIMESTAMP_KEY, Date.now().toString());
        } catch (e) {
            // localStorage plein ou non disponible
            console.warn('Impossible de sauvegarder les reponses dans le localStorage');
        }
    }

    /**
     * Charge les reponses sauvegardees depuis le localStorage
     */
    function chargerReponsesSauvegardees() {
        try {
            var dataJSON = localStorage.getItem(STORAGE_KEY);
            if (!dataJSON) return;

            var data = JSON.parse(dataJSON);

            // Verifier que le nombre de questions correspond
            if (data.questions !== NOMBRE_QUESTIONS) {
                nettoyerStockageLocal();
                return;
            }

            // Verifier que la sauvegarde n'est pas trop vieille (plus de 2 heures)
            var timestamp = parseInt(localStorage.getItem(STORAGE_TIMESTAMP_KEY) || '0', 10);
            var deuxHeures = 2 * 60 * 60 * 1000;
            if (Date.now() - timestamp > deuxHeures) {
                nettoyerStockageLocal();
                return;
            }

            // Restaurer les reponses
            if (data.reponses && Array.isArray(data.reponses)) {
                for (var i = 0; i < Math.min(data.reponses.length, NOMBRE_QUESTIONS); i++) {
                    if (data.reponses[i] !== null && data.reponses[i] !== undefined) {
                        reponsesUtilisateur[i] = data.reponses[i];

                        // Cocher le radio correspondant
                        var radios = document.getElementsByName('reponse_' + i);
                        if (radios) {
                            for (var j = 0; j < radios.length; j++) {
                                if (parseInt(radios[j].value, 10) === data.reponses[i]) {
                                    radios[j].checked = true;
                                    updateIndicator(i);
                                    updateQuestionStatut(i, true);
                                    break;
                                }
                            }
                        }
                    }
                }
                console.log('Reponses restaurees depuis la sauvegarde locale');
            }
        } catch (e) {
            console.warn('Erreur lors du chargement des reponses sauvegardees');
            nettoyerStockageLocal();
        }
    }

    /**
     * Nettoie le localStorage (apres soumission ou donnees invalides)
     */
    function nettoyerStockageLocal() {
        try {
            localStorage.removeItem(STORAGE_KEY);
            localStorage.removeItem(STORAGE_TIMESTAMP_KEY);
        } catch (e) {
            // Ignorer
        }
    }

    /* ==========================================================
     SECTION 9 : PROTECTION CONTRE LA FERMETURE ACCIDENTELLE
     ========================================================== */
    function initBeforeUnloadProtection() {
        window.addEventListener('beforeunload', function(e) {
            if (examenSoumis) return;

            // Verifier si au moins une reponse a ete donnee
            var aDesReponses = false;
            for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
                if (reponsesUtilisateur[i] !== null) {
                    aDesReponses = true;
                    break;
                }
            }

            if (aDesReponses) {
                var message = 'Vous avez des reponses non soumises. Voulez-vous vraiment quitter ?';
                e.returnValue = message;
                return message;
            }
        });
    }

})();