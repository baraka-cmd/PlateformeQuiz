// ============================================================
// FICHIER : views/assets/js/paiement-succes.js
// RÔLE    : Script de la page de succès de paiement
//           - Animation des confettis
//           - Compteur de redirection automatique
//           - Animation du badge Premium
//           - Animation des éléments de succès
//           - Effets visuels festifs
// ============================================================

'use strict';

// ============================================================
// CONFIGURATION
// ============================================================
var SuccesConfig = {
    tempsAvantRedirection: 10, // secondes
    intervalRedirection: null,
    secondesRestantes: 10
};

// ============================================================
// DOCUMENT READY
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initConfettis();
    initRedirectionAuto();
    initAnimationsSucces();
    initParticlesBackground();
    console.log('✅ Paiement Succès - Page initialisée');
});

// ============================================================
// ANIMATION DES CONFETTIS
// ============================================================
function initConfettis() {
    var confettis = document.querySelectorAll('.confetti');
    
    if (confettis.length === 0) {
        // Créer des confettis dynamiques si aucun dans le HTML
        creerConfettisDynamiques();
        return;
    }

    // Animer les confettis existants
    confettis.forEach(function(confetti, index) {
        // Position aléatoire
        var randomX = Math.random() * 80 + 10; // 10% à 90%
        var randomDelay = Math.random() * 1.5;
        var randomDuration = 2 + Math.random() * 2; // 2 à 4 secondes
        
        confetti.style.left = randomX + '%';
        confetti.style.animationDelay = randomDelay + 's';
        confetti.style.animationDuration = randomDuration + 's';
        
        // Relancer l'animation périodiquement
        setInterval(function() {
            confetti.style.animation = 'none';
            confetti.offsetHeight; // Trigger reflow
            confetti.style.animation = 'pmConfetti ' + randomDuration + 's ease-out forwards';
            confetti.style.animationDelay = '0s';
        }, randomDuration * 1000 + 500);
    });
}

function creerConfettisDynamiques() {
    var container = document.querySelector('.succes-animation');
    if (!container) return;

    var emojis = ['🎉', '✨', '🎊', '⭐', '🌟', '💫', '🎯', '🏆', '💎', '🔥'];
    var couleurs = ['#f39c12', '#667eea', '#27ae60', '#e74c3c', '#4facfe', '#9b59b6'];

    for (var i = 0; i < 20; i++) {
        var confetti = document.createElement('span');
        confetti.className = 'confetti';
        confetti.textContent = emojis[Math.floor(Math.random() * emojis.length)];
        confetti.style.cssText = 
            'position: absolute; font-size: ' + (16 + Math.random() * 20) + 'px; ' +
            'left: ' + (Math.random() * 80 + 10) + '%; ' +
            'top: ' + (-10 - Math.random() * 30) + 'px; ' +
            'animation: pmConfetti ' + (2 + Math.random() * 3) + 's ease-out forwards; ' +
            'animation-delay: ' + (Math.random() * 1.5) + 's; ' +
            'opacity: 0; pointer-events: none;';
        
        container.appendChild(confetti);

        // Relancer périodiquement
        var duration = 2 + Math.random() * 3;
        setInterval(function() {
            confetti.style.animation = 'none';
            confetti.offsetHeight;
            confetti.style.left = (Math.random() * 80 + 10) + '%';
            confetti.style.top = (-10 - Math.random() * 30) + 'px';
            confetti.style.animation = 'pmConfetti ' + duration + 's ease-out forwards';
            confetti.style.animationDelay = '0s';
            confetti.textContent = emojis[Math.floor(Math.random() * emojis.length)];
        }, duration * 1000 + 800);
    }
}

// ============================================================
// REDIRECTION AUTOMATIQUE
// ============================================================
function initRedirectionAuto() {
    var compteReboursElem = document.getElementById('compteRebours');
    var redirectionDiv = document.querySelector('.succes-redirection');

    if (!compteReboursElem && !redirectionDiv) return;

    // Si l'élément n'existe pas, le créer
    if (!compteReboursElem && redirectionDiv) {
        compteReboursElem = document.createElement('strong');
        compteReboursElem.id = 'compteRebours';
        redirectionDiv.appendChild(compteReboursElem);
    }

    SuccesConfig.secondesRestantes = SuccesConfig.tempsAvantRedirection;

    if (compteReboursElem) {
        compteReboursElem.textContent = SuccesConfig.secondesRestantes;
    }

    SuccesConfig.intervalRedirection = setInterval(function() {
        SuccesConfig.secondesRestantes--;

        if (compteReboursElem) {
            compteReboursElem.textContent = SuccesConfig.secondesRestantes;
        }

        // Effet visuel sur les 3 dernières secondes
        if (SuccesConfig.secondesRestantes <= 3 && compteReboursElem) {
            compteReboursElem.style.color = '#f39c12';
            compteReboursElem.style.fontSize = '16px';
            compteReboursElem.style.animation = 'pulse 0.5s ease-in-out';
        }

        if (SuccesConfig.secondesRestantes <= 0) {
            clearInterval(SuccesConfig.intervalRedirection);

            // Animation de transition
            if (redirectionDiv) {
                redirectionDiv.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Redirection en cours...';
            }

            setTimeout(function() {
                window.location.href = SuccesConfig.contextPath || 
                    (window.location.pathname.split('/').slice(0, -2).join('/') + '/joueur/dashboard');
            }, 500);
        }
    }, 1000);

    // Permettre à l'utilisateur d'annuler la redirection en cliquant
    if (redirectionDiv) {
        redirectionDiv.style.cursor = 'pointer';
        redirectionDiv.title = 'Cliquez pour annuler la redirection automatique';
        
        redirectionDiv.addEventListener('click', function() {
            if (SuccesConfig.intervalRedirection) {
                clearInterval(SuccesConfig.intervalRedirection);
                SuccesConfig.intervalRedirection = null;
                
                redirectionDiv.innerHTML = 
                    '<i class="fas fa-hand-pointer"></i> Redirection automatique annulée. ' +
                    '<strong>Vous pouvez naviguer librement.</strong>';
                redirectionDiv.style.cursor = 'default';
                
                if (typeof LearnPath !== 'undefined' && LearnPath.toast) {
                    LearnPath.toast('Redirection automatique annulée', 'info', 2500);
                }
            }
        });
    }
}

// ============================================================
// ANIMATIONS DE SUCCÈS
// ============================================================
function initAnimationsSucces() {
    // Animation du cercle de succès
    var cercleSucces = document.querySelector('.succes-cercle');
    if (cercleSucces) {
        // Effet de brillance périodique
        setInterval(function() {
            cercleSucces.style.boxShadow = '0 0 40px rgba(39, 174, 96, 0.6)';
            setTimeout(function() {
                cercleSucces.style.boxShadow = '';
            }, 800);
        }, 2500);
    }

    // Animation du badge Premium
    var badgePremium = document.querySelector('.succes-badge-premium');
    if (badgePremium) {
        // Effet de rotation 3D subtile
        var angle = 0;
        setInterval(function() {
            angle = (angle + 3) % 360;
            badgePremium.style.transform = 'perspective(500px) rotateY(' + Math.sin(angle * Math.PI / 180) * 8 + 'deg)';
        }, 50);
    }

    // Animation des éléments débloqués
    var deblocageItems = document.querySelectorAll('.deblocage-item');
    deblocageItems.forEach(function(item, index) {
        item.style.opacity = '0';
        item.style.transform = 'translateX(-30px)';
        item.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        setTimeout(function() {
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';
        }, 600 + (index * 150));
    });

    // Animation de la coche verte
    var checkIcons = document.querySelectorAll('.deblocage-check');
    checkIcons.forEach(function(icon, index) {
        icon.style.transform = 'scale(0)';
        icon.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

        setTimeout(function() {
            icon.style.transform = 'scale(1)';
        }, 800 + (index * 150));
    });
}

// ============================================================
// PARTICULES D'ARRIÈRE-PLAN
// ============================================================
function initParticlesBackground() {
    var card = document.querySelector('.succes-card');
    if (!card) return;

    // Créer un canvas pour les particules
    var canvas = document.createElement('canvas');
    canvas.style.cssText = 
        'position: absolute; top: 0; left: 0; width: 100%; height: 100%; ' +
        'pointer-events: none; z-index: 0; opacity: 0.15;';
    card.style.position = card.style.position || 'relative';
    card.insertBefore(canvas, card.firstChild);

    var ctx = canvas.getContext('2d');
    var particles = [];
    var maxParticles = 25;

    // Redimensionner le canvas
    function resizeCanvas() {
        canvas.width = card.offsetWidth;
        canvas.height = card.offsetHeight;
    }
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);

    // Créer les particules
    for (var i = 0; i < maxParticles; i++) {
        particles.push({
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            radius: 1 + Math.random() * 2,
            speedX: -0.3 + Math.random() * 0.6,
            speedY: -0.3 + Math.random() * 0.6,
            opacity: 0.3 + Math.random() * 0.7
        });
    }

    // Animer les particules
    function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        particles.forEach(function(p) {
            p.x += p.speedX;
            p.y += p.speedY;

            // Rebondir sur les bords
            if (p.x < 0 || p.x > canvas.width) p.speedX *= -1;
            if (p.y < 0 || p.y > canvas.height) p.speedY *= -1;

            // Maintenir dans les limites
            p.x = Math.max(0, Math.min(canvas.width, p.x));
            p.y = Math.max(0, Math.min(canvas.height, p.y));

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(39, 174, 96, ' + p.opacity + ')';
            ctx.fill();
        });

        // Lignes entre particules proches
        for (var i = 0; i < particles.length; i++) {
            for (var j = i + 1; j < particles.length; j++) {
                var dx = particles[i].x - particles[j].x;
                var dy = particles[i].y - particles[j].y;
                var distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 80) {
                    ctx.beginPath();
                    ctx.moveTo(particles[i].x, particles[i].y);
                    ctx.lineTo(particles[j].x, particles[j].y);
                    ctx.strokeStyle = 'rgba(39, 174, 96, ' + (0.1 * (1 - distance / 80)) + ')';
                    ctx.lineWidth = 0.5;
                    ctx.stroke();
                }
            }
        }

        requestAnimationFrame(animate);
    }

    animate();
}

// ============================================================
// NOTIFICATION DE BIENVENUE PREMIUM
// ============================================================
(function notificationBienvenue() {
    // Afficher une notification toast après un court délai
    setTimeout(function() {
        if (typeof LearnPath !== 'undefined' && LearnPath.toast) {
            LearnPath.toast('🎉 Bienvenue dans le monde Premium ! Tous les niveaux sont débloqués.', 
                'success', 6000);
        }
    }, 1500);
})();

// ============================================================
// EMPÊCHER LA REDIRECTION SI L'UTILISATEUR INTERAGIT
// ============================================================
var interactionUtilisateur = false;

document.addEventListener('click', function(e) {
    // Ne pas compter le clic sur la redirection elle-même
    if (!e.target.closest('.succes-redirection')) {
        interactionUtilisateur = true;
    }
});

document.addEventListener('scroll', function() {
    interactionUtilisateur = true;
});

// Prolonger le délai si l'utilisateur est actif
if (SuccesConfig.intervalRedirection && interactionUtilisateur) {
    clearInterval(SuccesConfig.intervalRedirection);
    SuccesConfig.secondesRestantes = 15; // Rajouter du temps
}