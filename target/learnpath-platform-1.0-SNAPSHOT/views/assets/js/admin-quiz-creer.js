// ============================================================
// FICHIER : views/assets/js/admin-quiz-creer.js
// RÔLE    : Scripts des pages de création de quiz
//           - Mise à jour de l'aperçu du titre
//           - Validation des formulaires
//           - Compteur de caractères
//           - Confirmation de suppression
//           - Confirmation de réinitialisation
//           - Animation des étapes de progression
// ============================================================

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initTitrePreview();
        initCharCounters();
        initFormValidation();
        initDeleteConfirmation();
        initReinitialiserConfirmation();
        initProgressAnimation();
        console.log('Admin Quiz Creer - Pages initialisees');
    });

    /* ==========================================================
     SECTION 2 : APERÇU DU TITRE EN TEMPS RÉEL
     ========================================================== */
    function initTitrePreview() {
        var themeInput = document.getElementById('theme');
        var ordreInput = document.getElementById('ordrePartie');
        var preview = document.getElementById('previewTitre');

        if (!themeInput || !ordreInput || !preview) return;

        function updatePreview() {
            var theme = themeInput.value.trim() || 'Theme';
            var ordre = ordreInput.value || 'X';
            preview.textContent = theme + ' - Partie ' + ordre;

            // Animation de mise à jour
            preview.style.transform = 'scale(1.05)';
            preview.style.transition = 'transform 0.2s ease';
            setTimeout(function() {
                preview.style.transform = 'scale(1)';
            }, 200);
        }

        themeInput.addEventListener('input', updatePreview);
        ordreInput.addEventListener('input', updatePreview);
        updatePreview();
    }

    /* ==========================================================
     SECTION 3 : COMPTEURS DE CARACTÈRES
     ========================================================== */
    function initCharCounters() {
        var textareas = document.querySelectorAll('textarea');

        textareas.forEach(function(textarea) {
            // Chercher un compteur existant ou en créer un
            var counter = textarea.parentElement.querySelector('.char-counter');
            if (!counter) {
                counter = document.createElement('span');
                counter.className = 'char-counter';
                counter.style.cssText = 
                    'display:block;text-align:right;font-size:11px;color:#8b8fa3;margin-top:4px;';
                textarea.parentElement.appendChild(counter);
            }

            function updateCounter() {
                var length = textarea.value.length;
                counter.textContent = length + ' caractere' + (length > 1 ? 's' : '');

                if (length > 500) {
                    counter.style.color = '#e74c3c';
                } else if (length > 300) {
                    counter.style.color = '#f39c12';
                } else {
                    counter.style.color = '#8b8fa3';
                }
            }

            textarea.addEventListener('input', updateCounter);
            updateCounter();
        });
    }

    /* ==========================================================
     SECTION 4 : VALIDATION DES FORMULAIRES
     ========================================================== */
    function initFormValidation() {
        // Formulaire d'informations du quiz
        var formInfos = document.getElementById('formInfos');
        if (formInfos) {
            formInfos.addEventListener('submit', function(e) {
                var theme = document.getElementById('theme');
                var niveau = document.getElementById('niveau');
                var ordre = document.getElementById('ordrePartie');

                if (!theme || !theme.value.trim()) {
                    e.preventDefault();
                    theme.focus();
                    theme.style.borderColor = '#e74c3c';
                    alert('Veuillez entrer un theme pour le quiz.');
                    return false;
                }

                if (!niveau || !niveau.value) {
                    e.preventDefault();
                    niveau.focus();
                    niveau.style.borderColor = '#e74c3c';
                    alert('Veuillez selectionner un niveau.');
                    return false;
                }

                if (!ordre || !ordre.value || parseInt(ordre.value) < 1) {
                    e.preventDefault();
                    ordre.focus();
                    ordre.style.borderColor = '#e74c3c';
                    alert('Veuillez entrer un numero de partie valide (1-20).');
                    return false;
                }

                return true;
            });
        }

        // Formulaire d'ajout de question
        var formAjout = document.getElementById('formAjoutQuestion');
        if (formAjout) {
            formAjout.addEventListener('submit', function(e) {
                var texte = document.getElementById('texteQuestion');
                var prop1 = document.getElementById('proposition1');
                var prop2 = document.getElementById('proposition2');
                var bonneReponse = document.querySelector('input[name="bonneReponse"]:checked');

                if (!texte || !texte.value.trim()) {
                    e.preventDefault();
                    texte.focus();
                    texte.style.borderColor = '#e74c3c';
                    alert('Veuillez entrer le texte de la question.');
                    return false;
                }

                if (!prop1 || !prop1.value.trim()) {
                    e.preventDefault();
                    prop1.focus();
                    prop1.style.borderColor = '#e74c3c';
                    alert('Veuillez entrer la proposition A.');
                    return false;
                }

                if (!prop2 || !prop2.value.trim()) {
                    e.preventDefault();
                    prop2.focus();
                    prop2.style.borderColor = '#e74c3c';
                    alert('Veuillez entrer la proposition B.');
                    return false;
                }

                if (!bonneReponse) {
                    e.preventDefault();
                    alert('Veuillez selectionner la bonne reponse (A, B, C ou D).');
                    return false;
                }

                return true;
            });
        }
    }

    /* ==========================================================
     SECTION 5 : CONFIRMATION DE SUPPRESSION
     ========================================================== */
    function initDeleteConfirmation() {
        var deleteForms = document.querySelectorAll('.question-item-action');

        deleteForms.forEach(function(form) {
            form.addEventListener('submit', function(e) {
                var questionItem = this.closest('.question-item');
                var questionTexte = '';
                if (questionItem) {
                    var texteElem = questionItem.querySelector('.question-item-texte');
                    if (texteElem) {
                        questionTexte = texteElem.textContent.trim();
                    }
                }

                var message = 'Supprimer cette question ?\n\n';
                if (questionTexte) {
                    message += '"' + questionTexte + '"';
                }

                if (!confirm(message)) {
                    e.preventDefault();
                }
            });
        });
    }

    /* ==========================================================
     SECTION 6 : CONFIRMATION DE RÉINITIALISATION
     ========================================================== */
    function initReinitialiserConfirmation() {
        var formReinit = document.querySelector('.form-reinitialiser');
        if (!formReinit) return;

        formReinit.addEventListener('submit', function(e) {
            var message = 'Reinitialiser la creation du quiz ?\n\n' +
                         'Toutes les informations et questions seront perdues.\n' +
                         'Cette action est irreversible.';

            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    }

    /* ==========================================================
     SECTION 7 : ANIMATION DE LA PROGRESSION
     ========================================================== */
    function initProgressAnimation() {
        var steps = document.querySelectorAll('.progress-step');

        if (steps.length === 0) return;

        // Observer pour déclencher l'animation
        if ('IntersectionObserver' in window) {
            var observer = new IntersectionObserver(function(entries) {
                entries.forEach(function(entry) {
                    if (entry.isIntersecting) {
                        var stepNumber = entry.target.querySelector('.step-number');
                        if (stepNumber && entry.target.classList.contains('active')) {
                            stepNumber.style.transform = 'scale(0.8)';
                            stepNumber.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                            setTimeout(function() {
                                stepNumber.style.transform = 'scale(1)';
                            }, 100);
                        }
                    }
                });
            }, { threshold: 0.5 });

            steps.forEach(function(step) {
                observer.observe(step);
            });
        }
    }

    /* ==========================================================
     SECTION 8 : ANIMATION DES BOUTONS RADIO (BONNE RÉPONSE)
     ========================================================== */
    (function initRadioAnimation() {
        var radioOptions = document.querySelectorAll('.bonne-reponse-option input[type="radio"]');

        radioOptions.forEach(function(radio) {
            radio.addEventListener('change', function() {
                var lettre = this.parentElement.querySelector('.option-lettre');
                if (lettre && this.checked) {
                    lettre.style.transform = 'scale(1.2)';
                    lettre.style.transition = 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    setTimeout(function() {
                        lettre.style.transform = 'scale(1.1)';
                    }, 300);
                }
            });
        });
    })();

    /* ==========================================================
     SECTION 9 : DÉTECTION DU MODE PLEIN ÉCRAN (F11)
     ========================================================== */
    (function initFullscreenDetection() {
        document.addEventListener('fullscreenchange', function() {
            var body = document.body;
            if (document.fullscreenElement) {
                body.classList.add('fullscreen-mode');
            } else {
                body.classList.remove('fullscreen-mode');
            }
        });
    })();

    /* ==========================================================
     SECTION 10 : STYLE POUR LE MODE PLEIN ÉCRAN
     ========================================================== */
    (function injectFullscreenStyle() {
        var style = document.createElement('style');
        style.textContent = 
            '.fullscreen-mode .admin-ajouter-question-container,' +
            '.fullscreen-mode .admin-quiz-creer-container {' +
            '  max-width: 100%;' +
            '  padding: 20px 40px;' +
            '}';
        document.head.appendChild(style);
    })();

})();