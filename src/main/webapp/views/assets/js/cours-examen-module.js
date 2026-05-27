// ============================================================
// FICHIER : src/main/webapp/views/assets/js/cours-examen-module.js
// RÔLE    : Gestion de l'examen d'un module de cours
//           Navigation entre questions, indicateurs,
//           modal de confirmation, soumission du formulaire
// ============================================================

'use strict';

// ===== VARIABLES =====
var questionCourante = 0;
var TOTAL_QUESTIONS = 0;
var reponsesSelectionnees = [];

// ===== INITIALISATION =====
document.addEventListener('DOMContentLoaded', function() {
    TOTAL_QUESTIONS = document.querySelectorAll('.question-card').length;
    reponsesSelectionnees = new Array(TOTAL_QUESTIONS).fill(0);
    
    mettreAJourAffichage();
    chargerReponsesExistantes();
    initialiserEcouteurs();
});

// ============================================================
// NAVIGATION ENTRE LES QUESTIONS
// ============================================================

function naviguerQuestion(direction) {
    var nouvelleQuestion = questionCourante + direction;
    
    if (nouvelleQuestion >= 0 && nouvelleQuestion < TOTAL_QUESTIONS) {
        document.getElementById('questionCard' + questionCourante).style.display = 'none';
        questionCourante = nouvelleQuestion;
        document.getElementById('questionCard' + questionCourante).style.display = 'block';
        
        window.scrollTo({ top: 0, behavior: 'smooth' });
        mettreAJourAffichage();
    }
}

function naviguerVersQuestion(index) {
    if (index >= 0 && index < TOTAL_QUESTIONS) {
        document.getElementById('questionCard' + questionCourante).style.display = 'none';
        questionCourante = index;
        document.getElementById('questionCard' + questionCourante).style.display = 'block';
        
        window.scrollTo({ top: 0, behavior: 'smooth' });
        mettreAJourAffichage();
    }
}

// ============================================================
// GESTION DES RÉPONSES
// ============================================================

function onReponseChange(index) {
    var radios = document.getElementsByName('reponse_' + index);
    var reponse = 0;
    
    for (var i = 0; i < radios.length; i++) {
        if (radios[i].checked) {
            reponse = parseInt(radios[i].value);
            break;
        }
    }
    
    reponsesSelectionnees[index] = reponse;
    mettreAJourIndicateur(index, reponse > 0);
    mettreAJourCompteurReponses();
}

function chargerReponsesExistantes() {
    for (var i = 0; i < TOTAL_QUESTIONS; i++) {
        var radios = document.getElementsByName('reponse_' + i);
        for (var j = 0; j < radios.length; j++) {
            if (radios[j].checked) {
                reponsesSelectionnees[i] = parseInt(radios[j].value);
                mettreAJourIndicateur(i, true);
                break;
            }
        }
    }
    mettreAJourCompteurReponses();
}

// ============================================================
// MISE À JOUR DE L'AFFICHAGE
// ============================================================

function mettreAJourAffichage() {
    var currentDisplay = document.getElementById('currentQuestionDisplay');
    if (currentDisplay) {
        currentDisplay.textContent = questionCourante + 1;
    }

    var navCurrent = document.getElementById('navCurrent');
    if (navCurrent) {
        navCurrent.textContent = questionCourante + 1;
    }
    
    var btnPrecedent = document.getElementById('btnPrecedent');
    var btnSuivant = document.getElementById('btnSuivant');
    var btnTerminer = document.getElementById('btnTerminer');
    
    if (btnPrecedent) {
        if (questionCourante === 0) {
            btnPrecedent.style.display = 'none';
        } else {
            btnPrecedent.style.display = 'inline-flex';
        }
    }
    
    if (btnSuivant && btnTerminer) {
        if (questionCourante === TOTAL_QUESTIONS - 1) {
            btnSuivant.style.display = 'none';
            btnTerminer.style.display = 'inline-flex';
        } else {
            btnSuivant.style.display = 'inline-flex';
            btnTerminer.style.display = 'none';
        }
    }
    
    var indicators = document.querySelectorAll('.indicator-dot');
    for (var i = 0; i < indicators.length; i++) {
        if (i === questionCourante) {
            indicators[i].classList.add('active');
        } else {
            indicators[i].classList.remove('active');
        }
    }
}

function mettreAJourIndicateur(index, estRepondu) {
    var indicateur = document.getElementById('indicator' + index);
    if (indicateur) {
        if (estRepondu) {
            indicateur.classList.add('answered');
        } else {
            indicateur.classList.remove('answered');
        }
    }
}

function mettreAJourCompteurReponses() {
    var nbRepondues = 0;
    for (var i = 0; i < reponsesSelectionnees.length; i++) {
        if (reponsesSelectionnees[i] > 0) {
            nbRepondues++;
        }
    }
    
    var answeredCount = document.getElementById('answeredCount');
    if (answeredCount) {
        answeredCount.textContent = nbRepondues;
    }
    
    var pourcentage = (nbRepondues / TOTAL_QUESTIONS) * 100;
    var progressFill = document.getElementById('progressFill');
    if (progressFill) {
        progressFill.style.width = pourcentage + '%';
    }
}

// ============================================================
// ÉCOUTEURS D'ÉVÉNEMENTS
// ============================================================

function initialiserEcouteurs() {
    document.addEventListener('keydown', function(e) {
        if (e.key === 'ArrowLeft' && questionCourante > 0) {
            e.preventDefault();
            naviguerQuestion(-1);
        }
        if (e.key === 'ArrowRight' && questionCourante < TOTAL_QUESTIONS - 1) {
            e.preventDefault();
            naviguerQuestion(1);
        }
        if (['1', '2', '3', '4'].indexOf(e.key) !== -1) {
            var reponse = parseInt(e.key);
            var radio = document.getElementById('reponse_' + questionCourante + '_' + reponse);
            if (radio) {
                radio.checked = true;
                onReponseChange(questionCourante);
            }
        }
        if (e.key === 'Escape') {
            fermerModalConfirmation();
        }
    });
    
    var modal = document.getElementById('modalConfirmation');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                fermerModalConfirmation();
            }
        });
    }
    
    var examenForm = document.getElementById('examenForm');
    if (examenForm) {
        examenForm.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                ouvrirModalConfirmation();
            }
        });
    }
}

// ============================================================
// MODAL DE CONFIRMATION
// ============================================================

function ouvrirModalConfirmation() {
    var nbNonRepondues = 0;
    for (var i = 0; i < reponsesSelectionnees.length; i++) {
        if (reponsesSelectionnees[i] === 0) {
            nbNonRepondues++;
        }
    }
    
    var questionsNonRepondues = document.getElementById('questionsNonRepondues');
    if (nbNonRepondues > 0 && questionsNonRepondues) {
        questionsNonRepondues.style.display = 'flex';
        var nbNonReponduesSpan = document.getElementById('nbNonRepondues');
        if (nbNonReponduesSpan) {
            nbNonReponduesSpan.textContent = nbNonRepondues;
        }
    } else if (questionsNonRepondues) {
        questionsNonRepondues.style.display = 'none';
    }
    
    var modal = document.getElementById('modalConfirmation');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function fermerModalConfirmation() {
    var modal = document.getElementById('modalConfirmation');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
}

function terminerExamen() {
    var btnTerminer = document.getElementById('btnTerminer');
    if (btnTerminer) {
        btnTerminer.disabled = true;
        btnTerminer.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Correction...</span>';
    }
    
    var examenForm = document.getElementById('examenForm');
    if (examenForm) {
        examenForm.submit();
    }
}

// ============================================================
// EMPÊCHER DE QUITTER ACCIDENTELLEMENT
// ============================================================

window.addEventListener('beforeunload', function(e) {
    var nbRepondues = 0;
    for (var i = 0; i < reponsesSelectionnees.length; i++) {
        if (reponsesSelectionnees[i] > 0) {
            nbRepondues++;
        }
    }
    if (nbRepondues > 0 && nbRepondues < TOTAL_QUESTIONS) {
        e.preventDefault();
        e.returnValue = 'Vous avez des réponses non soumises. Voulez-vous vraiment quitter ?';
        return e.returnValue;
    }
});