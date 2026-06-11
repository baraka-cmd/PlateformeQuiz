// ============================================================
// FICHIER : src/main/webapp/views/assets/js/cours-theme.js
// RÔLE    : Gestion de la page d'un thème spécifique
//           Animations, progression, recommandations,
//           sauvegarde des préférences, interactions
// ============================================================

'use strict';

// ============================================================
// INITIALISATION AU CHARGEMENT
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initAnimations();
    initProgressionBars();
    initCardHoverEffects();
    initKeyboardShortcuts();
    initSaveLastVisited();
    initTooltips();
    initScrollReveal();
});

// ============================================================
// ANIMATIONS D'ENTRÉE
// ============================================================
function initAnimations() {
    // Animation du thème banner
    const banner = document.querySelector('.theme-banner');
    if (banner) {
        banner.style.opacity = '0';
        banner.style.transform = 'translateY(-20px)';
        setTimeout(() => {
            banner.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            banner.style.opacity = '1';
            banner.style.transform = 'translateY(0)';
        }, 100);
    }
    
    // Animation des cartes niveaux
    const cards = document.querySelectorAll('.niveau-card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        setTimeout(() => {
            card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 150 + (index * 80));
    });
    
    // Animation de la section recommandation
    const recommandation = document.querySelector('.recommandation-section');
    if (recommandation) {
        recommandation.style.opacity = '0';
        setTimeout(() => {
            recommandation.style.transition = 'opacity 0.5s ease';
            recommandation.style.opacity = '1';
        }, 500);
    }
}

// ============================================================
// BARRES DE PROGRESSION
// ============================================================
function initProgressionBars() {
    const progressFills = document.querySelectorAll('.banner-progress-fill, .niveau-progress-fill');
    
    progressFills.forEach(fill => {
        const targetWidth = fill.style.width;
        if (targetWidth && targetWidth !== '0%') {
            fill.style.width = '0%';
            setTimeout(() => {
                fill.style.transition = 'width 0.8s cubic-bezier(0.4, 0, 0.2, 1)';
                fill.style.width = targetWidth;
            }, 200);
        }
    });
    
    // Animation des stats numériques
    const statValues = document.querySelectorAll('.banner-stat-item strong');
    statValues.forEach(stat => {
        const finalValue = parseInt(stat.innerText);
        if (finalValue > 0 && !isNaN(finalValue)) {
            let current = 0;
            const increment = Math.ceil(finalValue / 30);
            const timer = setInterval(() => {
                current += increment;
                if (current >= finalValue) {
                    stat.innerText = finalValue;
                    clearInterval(timer);
                } else {
                    stat.innerText = current;
                }
            }, 25);
        }
    });
}

// ============================================================
// EFFETS DE SURVOL DES CARTES
// ============================================================
function initCardHoverEffects() {
    const cards = document.querySelectorAll('.niveau-card');
    
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            const icon = this.querySelector('.niveau-icon');
            if (icon) {
                icon.style.transform = 'scale(1.05)';
                icon.style.transition = 'transform 0.2s ease';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            const icon = this.querySelector('.niveau-icon');
            if (icon) {
                icon.style.transform = 'scale(1)';
            }
        });
    });
    
    // Effet de pulsation pour le bouton continuer
    const continueBtn = document.querySelector('.continue-btn');
    if (continueBtn) {
        setInterval(() => {
            continueBtn.style.transform = 'scale(1.02)';
            setTimeout(() => {
                continueBtn.style.transform = 'scale(1)';
            }, 300);
        }, 5000);
    }
}

// ============================================================
// RACCOURCIS CLAVIER
// ============================================================
function initKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Flèche gauche : retour aux thèmes
        if (e.key === 'ArrowLeft' && !e.ctrlKey && !e.altKey && !e.target.matches('input, textarea, select')) {
            e.preventDefault();
            const backBtn = document.querySelector('.btn-back');
            if (backBtn) {
                backBtn.click();
            }
        }
        
        // Flèche droite : premier niveau débloqué
        if (e.key === 'ArrowRight' && !e.ctrlKey && !e.altKey && !e.target.matches('input, textarea, select')) {
            e.preventDefault();
            const firstUnlocked = document.querySelector('.niveau-card.debloque .niveau-card-link');
            if (firstUnlocked) {
                firstUnlocked.click();
            }
        }
        
        // C : continuer le dernier cours
        if ((e.key === 'c' || e.key === 'C') && !e.ctrlKey && !e.target.matches('input, textarea, select')) {
            e.preventDefault();
            const continueBtn = document.querySelector('.continue-btn');
            if (continueBtn) {
                continueBtn.click();
            } else {
                showNotification('Aucun cours en cours', 'info');
            }
        }
        
        // R : recommander un niveau
        if ((e.key === 'r' || e.key === 'R') && !e.ctrlKey && !e.target.matches('input, textarea, select')) {
            e.preventDefault();
            const recommendedCard = document.querySelector('.niveau-recommended-badge')?.closest('.niveau-card');
            if (recommendedCard && recommendedCard.classList.contains('debloque')) {
                const link = recommendedCard.closest('.niveau-card-link');
                if (link) link.click();
            }
        }
        
        // Échap : retour au dashboard
        if (e.key === 'Escape') {
            const dashboardLink = document.querySelector('.breadcrumb-link:first-child');
            if (dashboardLink) {
                dashboardLink.click();
            }
        }
    });
    
    // Afficher un message d'aide
    showShortcutHint();
}

function showShortcutHint() {
    const hint = document.createElement('div');
    hint.className = 'shortcut-hint';
    hint.innerHTML = `
        <i class="fas fa-keyboard"></i>
        <span>Raccourcis : ← Retour | → Niveau suivant | C Continuer | R Recommandé</span>
    `;
    hint.style.cssText = `
        position: fixed;
        bottom: 20px;
        left: 20px;
        background: #1a1a2e;
        color: #fff;
        padding: 8px 16px;
        border-radius: 30px;
        font-size: 11px;
        z-index: 99;
        opacity: 0;
        transition: opacity 0.3s;
        pointer-events: none;
        display: flex;
        align-items: center;
        gap: 8px;
    `;
    document.body.appendChild(hint);
    
    setTimeout(() => {
        hint.style.opacity = '0.7';
        setTimeout(() => {
            hint.style.opacity = '0';
            setTimeout(() => hint.remove(), 5000);
        }, 5000);
    }, 1000);
}

// ============================================================
// SAUVEGARDE DU DERNIER VISITÉ
// ============================================================
function initSaveLastVisited() {
    const themeName = getThemeName();
    if (themeName) {
        const lastVisited = {
            theme: themeName,
            date: new Date().toISOString(),
            url: window.location.href
        };
        saveToLocalStorage('lastThemeVisited', lastVisited);
    }
    
    // Sauvegarder le niveau cliqué
    const niveauLinks = document.querySelectorAll('.niveau-card-link');
    niveauLinks.forEach(link => {
        link.addEventListener('click', function() {
            const niveauCard = this.querySelector('.niveau-card');
            const niveauName = niveauCard?.querySelector('h3')?.innerText;
            if (niveauName) {
                saveToLocalStorage('lastNiveauClicked', {
                    theme: themeName,
                    niveau: niveauName,
                    date: new Date().toISOString()
                });
            }
        });
    });
}

function getThemeName() {
    const themeTitle = document.querySelector('.theme-banner-content h1')?.innerText;
    if (themeTitle) return themeTitle;
    const breadcrumb = document.querySelector('.breadcrumb-current span:last-child')?.innerText;
    return breadcrumb || null;
}

function saveToLocalStorage(key, value) {
    try {
        localStorage.setItem(key, JSON.stringify(value));
    } catch(e) {}
}

// ============================================================
// NOTIFICATION
// ============================================================
function showNotification(message, type = 'success') {
    let notification = document.getElementById('themeNotification');
    if (!notification) {
        notification = document.createElement('div');
        notification.id = 'themeNotification';
        notification.style.cssText = `
            position: fixed;
            bottom: 80px;
            right: 20px;
            background: ${type === 'success' ? '#27ae60' : '#667eea'};
            color: white;
            padding: 12px 20px;
            border-radius: 12px;
            font-size: 13px;
            font-weight: 500;
            z-index: 1000;
            opacity: 0;
            transition: opacity 0.3s;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: flex;
            align-items: center;
            gap: 10px;
        `;
        document.body.appendChild(notification);
    }
    
    notification.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'info-circle'}"></i> ${message}`;
    notification.style.backgroundColor = type === 'success' ? '#27ae60' : '#667eea';
    notification.style.opacity = '1';
    
    setTimeout(() => {
        notification.style.opacity = '0';
    }, 3000);
}

// ============================================================
// TOOLTIPS
// ============================================================
function initTooltips() {
    const cards = document.querySelectorAll('.niveau-card');
    
    cards.forEach(card => {
        const niveauName = card.querySelector('h3')?.innerText;
        const progression = card.querySelector('.niveau-progress-header span:last-child')?.innerText;
        
        if (niveauName && progression) {
            card.setAttribute('data-tooltip', `${niveauName} - Progression : ${progression}`);
            
            card.addEventListener('mouseenter', showTooltip);
            card.addEventListener('mouseleave', hideTooltip);
        }
    });
}

function showTooltip(e) {
    const tooltip = document.createElement('div');
    tooltip.className = 'custom-tooltip';
    tooltip.innerHTML = e.currentTarget.getAttribute('data-tooltip');
    tooltip.style.cssText = `
        position: absolute;
        background: #1a1a2e;
        color: #fff;
        padding: 6px 12px;
        border-radius: 8px;
        font-size: 11px;
        z-index: 1000;
        pointer-events: none;
        white-space: nowrap;
        box-shadow: 0 2px 8px rgba(0,0,0,0.15);
    `;
    
    const rect = e.currentTarget.getBoundingClientRect();
    tooltip.style.top = (rect.top - 30) + 'px';
    tooltip.style.left = (rect.left + 20) + 'px';
    
    document.body.appendChild(tooltip);
    e.currentTarget._tooltip = tooltip;
}

function hideTooltip(e) {
    if (e.currentTarget._tooltip) {
        e.currentTarget._tooltip.remove();
        e.currentTarget._tooltip = null;
    }
}

// ============================================================
// SCROLL REVEAL
// ============================================================
function initScrollReveal() {
    const elements = document.querySelectorAll('.cours-populaires-grid, .recommandation-section, .continue-card');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });
    
    elements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px';
        el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        observer.observe(el);
    });
}

// ============================================================
// CONFETTIS POUR THÈME TERMINÉ
// ============================================================
function showConfettiIfCompleted() {
    const completedBadge = document.querySelector('.status-badge.completed');
    if (completedBadge) {
        const hasShown = localStorage.getItem('confettiShown_' + getThemeName());
        if (!hasShown) {
            setTimeout(() => {
                launchConfetti();
                localStorage.setItem('confettiShown_' + getThemeName(), 'true');
            }, 500);
        }
    }
}

function launchConfetti() {
    if (typeof confetti === 'function') {
        confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 } });
        setTimeout(() => {
            confetti({ particleCount: 50, spread: 100, origin: { y: 0.6, x: 0.3 } });
            confetti({ particleCount: 50, spread: 100, origin: { y: 0.6, x: 0.7 } });
        }, 200);
    } else {
        // Confetti simple sans librairie
        showNotification('🎉 Félicitations ! Thème terminé !', 'success');
    }
}

// Exposer les fonctions globales
window.showNotification = showNotification;
window.launchConfetti = launchConfetti;

// ============================================================
// STYLES DYNAMIQUES
// ============================================================
const dynamicStyles = document.createElement('style');
dynamicStyles.textContent = `
    .custom-tooltip {
        animation: tooltipFadeIn 0.2s ease;
    }
    
    @keyframes tooltipFadeIn {
        from { opacity: 0; transform: translateY(5px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    .niveau-card {
        transition: transform 0.3s ease, box-shadow 0.3s ease;
    }
    
    .shortcut-hint {
        font-family: 'Poppins', monospace;
    }
    
    @media (max-width: 768px) {
        .shortcut-hint {
            display: none;
        }
    }
`;
document.head.appendChild(dynamicStyles);