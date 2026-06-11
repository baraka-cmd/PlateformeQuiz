// ============================================================
// FICHIER : views/assets/js/paiement.js
// RÔLE    : Script de la page de paiement LearnPath
//           - Validation du numero de telephone en temps reel
//           - Formatage automatique (+243 ou 0)
//           - Gestion du formulaire de paiement
//           - Animation du bouton pendant le traitement
//           - Protection anti-double-clic
//           - Feedback visuel
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var PaiementConfig = {
    indicatifPays: '+243',
    longueurMin: 10,
    longueurMax: 14,
    regexTelephone: /^(\+243|0)[0-9]{9}$/,
    regexChiffres: /[^0-9+]/g
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initPaiementForm();
    initTelephoneInput();
    initPrixCards();
    initFaqToggle();
    console.log('Paiement - Page initialisee');
});

// ============================================================
// INITIALISATION DU FORMULAIRE
// ============================================================
function initPaiementForm() {
    var form = document.getElementById('formPaiement');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        // Valider le telephone
        var telephoneInput = document.getElementById('telephone');
        var telephone = telephoneInput ? telephoneInput.value.trim() : '';
        var telephoneError = document.getElementById('telephoneError');

        // Si telephone invalide, bloquer la soumission
        if (!validerTelephone(telephone)) {
            e.preventDefault();
            if (telephoneError) {
                telephoneError.textContent = 'Format invalide. Exemple : +243984270438 ou 0984270438';
                telephoneError.style.display = 'block';
            }
            if (telephoneInput) {
                telephoneInput.focus();
                telephoneInput.classList.add('input-error');
                telephoneInput.style.borderColor = '#e74c3c';
                telephoneInput.style.animation = 'shake 0.5s ease';
            }
            return false;
        }

        // Telephone valide : normaliser
        telephone = normaliserTelephone(telephone);
        if (telephoneInput) {
            telephoneInput.value = telephone;
            telephoneInput.classList.remove('input-error');
            telephoneInput.classList.add('input-valid');
            telephoneInput.style.borderColor = '#27ae60';
            telephoneInput.style.animation = '';
        }

        if (telephoneError) {
            telephoneError.style.display = 'none';
        }

        // Desactiver le bouton et afficher le loader
        var btnPayer = document.getElementById('btnPayer');
        if (btnPayer) {
            btnPayer.disabled = true;
            var btnText = btnPayer.querySelector('span');
            var btnLoader = btnPayer.querySelector('.btn-loader');

            if (btnText) btnText.textContent = 'Paiement en cours...';
            if (btnLoader) btnLoader.style.display = 'inline-block';
            btnPayer.style.opacity = '0.8';
            btnPayer.style.cursor = 'wait';
        }

        // Laisser le formulaire se soumettre normalement
        // Ne pas appeler e.preventDefault() ici
        return true;
    });
}

// ============================================================
// INITIALISATION DU CHAMP TELEPHONE
// ============================================================
function initTelephoneInput() {
    var telephoneInput = document.getElementById('telephone');
    if (!telephoneInput) return;

    // Formatage automatique pendant la saisie
    telephoneInput.addEventListener('input', function() {
        var valeur = this.value;

        // Supprimer les caracteres non autorises
        var nettoye = valeur.replace(PaiementConfig.regexChiffres, '');

        // Limiter la longueur
        if (nettoye.length > PaiementConfig.longueurMax) {
            nettoye = nettoye.substring(0, PaiementConfig.longueurMax);
        }

        this.value = nettoye;

        // Valider en temps reel
        var erreur = document.getElementById('telephoneError');
        var estValide = PaiementConfig.regexTelephone.test(nettoye);

        if (nettoye.length >= PaiementConfig.longueurMin && estValide) {
            this.classList.add('input-valid');
            this.classList.remove('input-error');
            this.style.borderColor = '#27ae60';
            if (erreur) erreur.style.display = 'none';
        } else if (nettoye.length > 0 && !estValide && nettoye.length >= 3) {
            this.classList.add('input-error');
            this.classList.remove('input-valid');
            this.style.borderColor = '#e74c3c';
        } else {
            this.classList.remove('input-valid', 'input-error');
            this.style.borderColor = '#e8eaef';
        }
    });

    // Formatage au focus
    telephoneInput.addEventListener('focus', function() {
        this.parentElement.style.boxShadow = '0 0 0 4px rgba(102, 126, 234, 0.08)';
    });

    telephoneInput.addEventListener('blur', function() {
        this.parentElement.style.boxShadow = 'none';

        // Valider a la perte de focus
        var valeur = this.value.trim();
        if (valeur.length > 0) {
            var estValide = PaiementConfig.regexTelephone.test(valeur);
            if (!estValide) {
                this.classList.add('input-error');
                this.style.borderColor = '#e74c3c';
                var erreur = document.getElementById('telephoneError');
                if (erreur) {
                    erreur.textContent = 'Format invalide. Exemple : +243984270438';
                    erreur.style.display = 'block';
                }
            }
        }
    });

    // Empecher les caracteres non numeriques au clavier
    telephoneInput.addEventListener('keypress', function(e) {
        var char = String.fromCharCode(e.which || e.keyCode);
        // Autoriser : chiffres, +, backspace, delete, fleches, tab
        if (!/[0-9+]/.test(char) &&
            e.keyCode !== 8 && e.keyCode !== 46 &&
            e.keyCode !== 37 && e.keyCode !== 39 &&
            e.keyCode !== 9) {
            e.preventDefault();
        }

        // Empecher + apres le premier caractere
        if (char === '+' && this.value.indexOf('+') !== -1) {
            e.preventDefault();
        }

        // Empecher + si pas au debut
        if (char === '+' && this.selectionStart > 0) {
            e.preventDefault();
        }
    });

    // Ajouter le style pour l'animation shake
    if (!document.getElementById('paiement-shake-style')) {
        var shakeStyle = document.createElement('style');
        shakeStyle.id = 'paiement-shake-style';
        shakeStyle.textContent =
            '@keyframes shake {' +
            '  0%, 100% { transform: translateX(0); }' +
            '  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }' +
            '  20%, 40%, 60%, 80% { transform: translateX(5px); }' +
            '}' +
            '.input-valid { border-color: #27ae60 !important; background: #f0fff4 !important; }' +
            '.input-error { border-color: #e74c3c !important; background: #fff5f5 !important; }';
        document.head.appendChild(shakeStyle);
    }
}

// ============================================================
// VALIDATION TELEPHONE
// ============================================================
function validerTelephone(telephone) {
    if (!telephone || telephone.trim() === '') return false;
    return PaiementConfig.regexTelephone.test(telephone.trim());
}

/**
 * Normalise un numero de telephone au format international
 * @param {string} telephone - Numero brut
 * @returns {string} Numero au format +243XXXXXXXXX
 */
function normaliserTelephone(telephone) {
    telephone = telephone.trim();

    // Si commence par 0, remplacer par +243
    if (telephone.startsWith('0')) {
        telephone = PaiementConfig.indicatifPays + telephone.substring(1);
    }

    return telephone;
}

// ============================================================
// ANIMATION DES CARTES DE PRIX
// ============================================================
function initPrixCards() {
    var prixCards = document.querySelectorAll('.prix-card');

    prixCards.forEach(function(card, index) {
        // Animation d'apparition decalee
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        setTimeout(function() {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 200 + (index * 150));

        // Effet hover
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 10px 40px rgba(0, 0, 0, 0.12)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '';
        });
    });

    // Animation du separateur "OU"
    var prixOu = document.querySelector('.prix-ou');
    if (prixOu) {
        prixOu.style.opacity = '0';
        prixOu.style.transform = 'scale(0)';
        prixOu.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

        setTimeout(function() {
            prixOu.style.opacity = '1';
            prixOu.style.transform = 'scale(1)';
        }, 400);
    }
}

// ============================================================
// FAQ TOGGLE
// ============================================================
function initFaqToggle() {
    var faqQuestions = document.querySelectorAll('.faq-item h4');

    faqQuestions.forEach(function(question) {
        question.style.cursor = 'pointer';
        question.style.transition = 'all 0.3s ease';

        question.addEventListener('click', function() {
            var parent = this.parentElement;
            var answer = parent.querySelector('p');

            if (!answer) return;

            var isOpen = answer.style.maxHeight && answer.style.maxHeight !== '0px';

            // Fermer toutes les autres
            document.querySelectorAll('.faq-item p').forEach(function(p) {
                if (p !== answer) {
                    p.style.maxHeight = '0';
                    p.style.opacity = '0';
                    p.style.marginTop = '0';
                }
            });

            // Toggle celle-ci
            if (isOpen) {
                answer.style.maxHeight = '0';
                answer.style.opacity = '0';
                answer.style.marginTop = '0';
                if (this.querySelector('i')) {
                    this.querySelector('i').style.transform = 'rotate(0deg)';
                }
            } else {
                answer.style.maxHeight = answer.scrollHeight + 'px';
                answer.style.opacity = '1';
                answer.style.marginTop = '8px';
                if (this.querySelector('i')) {
                    this.querySelector('i').style.transform = 'rotate(180deg)';
                }
            }
        });

        // Styles initiaux pour les reponses
        var answer = question.parentElement.querySelector('p');
        if (answer) {
            answer.style.transition = 'all 0.3s ease';
            answer.style.overflow = 'hidden';
            answer.style.maxHeight = '0';
            answer.style.opacity = '0';
        }
    });
}

// ============================================================
// ANIMATION DES AVANTAGES
// ============================================================
(function initAvantagesAnimation() {
    var avantages = document.querySelectorAll('.avantage-item');

    avantages.forEach(function(avantage, index) {
        avantage.style.opacity = '0';
        avantage.style.transform = 'translateX(-20px)';
        avantage.style.transition = 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)';

        setTimeout(function() {
            avantage.style.opacity = '1';
            avantage.style.transform = 'translateX(0)';
        }, 500 + (index * 120));
    });
})();

// ============================================================
// COMPTEUR DE CARACTERES (OPTIONNEL)
// ============================================================
(function initCharCounter() {
    var telephoneInput = document.getElementById('telephone');
    var counterElement = document.getElementById('telephoneCounter');

    if (!telephoneInput || !counterElement) return;

    telephoneInput.addEventListener('input', function() {
        var longueur = this.value.replace(/[^0-9]/g, '').length;
        counterElement.textContent = longueur + ' chiffres';

        if (longueur === 9) {
            counterElement.style.color = '#27ae60';
            counterElement.innerHTML = '<i class="fas fa-check-circle"></i> ' + longueur + ' chiffres';
        } else {
            counterElement.style.color = '#8b8fa3';
            counterElement.innerHTML = longueur + ' chiffres';
        }
    });
})();