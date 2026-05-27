// ============================================================
// FICHIER : src/main/webapp/views/assets/js/cours-niveau.js
// RÔLE    : Gestion de la page niveau d'un thème de cours
//           Filtres, tri, animations, progression
//           Design senior avec expérience utilisateur avancée
// ============================================================

'use strict';

// ============================================================
// INITIALISATION AU CHARGEMENT
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    initFiltersAndSort();
    initAnimations();
    initProgressAnimations();
    initKeyboardShortcuts();
    initTooltips();
    initFavoriteSystem();
    initRecentlyViewed();
});

// ============================================================
// VARIABLES GLOBALES
// ============================================================
let currentFilter = 'all';
let currentSort = 'default';

// ============================================================
// FILTRES ET TRI DES COURS
// ============================================================
function initFiltersAndSort() {
    const filterBtns = document.querySelectorAll('.filter-btn');
    const sortSelect = document.getElementById('sortSelect');
    const coursCards = Array.from(document.querySelectorAll('.cours-card'));
    
    if (!coursCards.length) return;
    
    function filterAndSort() {
        let filteredCards = coursCards.filter(card => {
            if (currentFilter === 'all') return true;
            return card.dataset.status === currentFilter;
        });
        
        if (currentSort !== 'default') {
            filteredCards.sort((a, b) => {
                switch(currentSort) {
                    case 'title-asc':
                        return (a.dataset.title || '').localeCompare(b.dataset.title || '');
                    case 'title-desc':
                        return (b.dataset.title || '').localeCompare(a.dataset.title || '');
                    case 'progress-asc':
                        return (parseInt(a.dataset.progress) || 0) - (parseInt(b.dataset.progress) || 0);
                    case 'progress-desc':
                        return (parseInt(b.dataset.progress) || 0) - (parseInt(a.dataset.progress) || 0);
                    default:
                        return 0;
                }
            });
        }
        
        // Animation de disparition
        coursCards.forEach(card => {
            card.style.animation = 'fadeOut 0.2s ease forwards';
            setTimeout(() => {
                card.style.display = 'none';
            }, 150);
        });
        
        setTimeout(() => {
            filteredCards.forEach((card, index) => {
                card.style.display = 'block';
                card.style.animation = `fadeInUp 0.3s ease ${index * 0.05}s forwards`;
            });
            
            updateEmptyState(filteredCards.length);
            updateCountDisplay(filteredCards.length);
        }, 200);
    }
    
    function updateEmptyState(count) {
        let emptyMsg = document.getElementById('filterEmptyMessage');
        if (count === 0) {
            if (!emptyMsg) {
                emptyMsg = document.createElement('div');
                emptyMsg.id = 'filterEmptyMessage';
                emptyMsg.className = 'empty-state';
                emptyMsg.innerHTML = `
                    <i class="fas fa-filter"></i>
                    <p>Aucun cours ne correspond à ce filtre.</p>
                    <button class="btn-reset-filters" onclick="resetFilters()">
                        <i class="fas fa-undo"></i> Réinitialiser les filtres
                    </button>
                `;
                const coursGrid = document.getElementById('coursGrid');
                if (coursGrid) coursGrid.appendChild(emptyMsg);
            } else {
                emptyMsg.style.display = 'flex';
            }
        } else if (emptyMsg) {
            emptyMsg.style.display = 'none';
        }
    }
    
    function updateCountDisplay(count) {
        let countDisplay = document.getElementById('coursCountDisplay');
        if (!countDisplay) {
            const filterGroup = document.querySelector('.filter-group');
            if (filterGroup) {
                countDisplay = document.createElement('span');
                countDisplay.id = 'coursCountDisplay';
                countDisplay.className = 'cours-count';
                filterGroup.parentElement?.appendChild(countDisplay);
            }
        }
        if (countDisplay) {
            countDisplay.innerHTML = `<i class="fas fa-book"></i> ${count} cours`;
        }
    }
    
    // Événements des filtres
    filterBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            filterBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            
            // Animation du clic
            this.style.transform = 'scale(0.95)';
            setTimeout(() => { this.style.transform = ''; }, 150);
            
            filterAndSort();
            saveFilterPreference(currentFilter);
        });
    });
    
    // Événement du tri
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            currentSort = this.value;
            filterAndSort();
            saveSortPreference(currentSort);
        });
    }
    
    // Charger les préférences sauvegardées
    loadFilterPreference();
    loadSortPreference();
}

// ============================================================
// SAUVEGARDE DES PRÉFÉRENCES
// ============================================================
function saveFilterPreference(filter) {
    try {
        localStorage.setItem('coursNiveauFilter', filter);
    } catch(e) {}
}

function loadFilterPreference() {
    try {
        const savedFilter = localStorage.getItem('coursNiveauFilter');
        if (savedFilter && savedFilter !== currentFilter) {
            const btn = document.querySelector(`.filter-btn[data-filter="${savedFilter}"]`);
            if (btn) {
                btn.click();
            }
        }
    } catch(e) {}
}

function saveSortPreference(sort) {
    try {
        localStorage.setItem('coursNiveauSort', sort);
    } catch(e) {}
}

function loadSortPreference() {
    try {
        const savedSort = localStorage.getItem('coursNiveauSort');
        if (savedSort && savedSort !== currentSort) {
            const sortSelect = document.getElementById('sortSelect');
            if (sortSelect) {
                sortSelect.value = savedSort;
                currentSort = savedSort;
            }
        }
    } catch(e) {}
}

// ============================================================
// RÉINITIALISATION DES FILTRES
// ============================================================
window.resetFilters = function() {
    const allBtn = document.querySelector('.filter-btn[data-filter="all"]');
    if (allBtn) allBtn.click();
    
    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) sortSelect.value = 'default';
    currentSort = 'default';
    
    localStorage.removeItem('coursNiveauFilter');
    localStorage.removeItem('coursNiveauSort');
};

// ============================================================
// ANIMATIONS
// ============================================================
function initAnimations() {
    // Animation des cartes stats
    const statValues = document.querySelectorAll('.stat-value');
    statValues.forEach(stat => {
        const finalValue = stat.innerText;
        const isPercentage = finalValue.includes('%');
        const numericValue = parseInt(finalValue) || 0;
        
        if (numericValue > 0) {
            let current = 0;
            const increment = Math.ceil(numericValue / 30);
            const duration = 800;
            const stepTime = duration / (numericValue / increment);
            
            const timer = setInterval(() => {
                current += increment;
                if (current >= numericValue) {
                    stat.innerText = isPercentage ? numericValue + '%' : numericValue;
                    clearInterval(timer);
                } else {
                    stat.innerText = isPercentage ? current + '%' : current;
                }
            }, stepTime);
        }
    });
    
    // Animation d'entrée des cartes cours
    const coursCards = document.querySelectorAll('.cours-card');
    coursCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        setTimeout(() => {
            card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 50);
    });
}

function initProgressAnimations() {
    const progressBars = document.querySelectorAll('.cours-progress-fill');
    progressBars.forEach(bar => {
        const width = bar.style.width;
        bar.style.width = '0%';
        setTimeout(() => {
            bar.style.transition = 'width 0.8s ease-out';
            bar.style.width = width;
        }, 100);
    });
}

// ============================================================
// RACCOURCIS CLAVIER
// ============================================================
function initKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl + F : Focus sur le filtre
        if (e.ctrlKey && e.key === 'f') {
            e.preventDefault();
            const firstFilter = document.querySelector('.filter-btn');
            if (firstFilter) firstFilter.focus();
            showShortcutNotification('Filtres activés');
        }
        
        // Ctrl + S : Focus sur le tri
        if (e.ctrlKey && e.key === 's') {
            e.preventDefault();
            const sortSelect = document.getElementById('sortSelect');
            if (sortSelect) sortSelect.focus();
            showShortcutNotification('Tri activé');
        }
        
        // R : Réinitialiser les filtres
        if (e.key === 'r' || e.key === 'R') {
            if (!e.target.matches('input, textarea, select')) {
                e.preventDefault();
                resetFilters();
                showShortcutNotification('Filtres réinitialisés');
            }
        }
        
        // Échap : Réinitialiser
        if (e.key === 'Escape') {
            resetFilters();
        }
    });
}

function showShortcutNotification(message) {
    let notification = document.getElementById('shortcutNotification');
    if (!notification) {
        notification = document.createElement('div');
        notification.id = 'shortcutNotification';
        notification.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: #667eea;
            color: white;
            padding: 10px 20px;
            border-radius: 10px;
            font-size: 13px;
            z-index: 1000;
            opacity: 0;
            transition: opacity 0.3s;
            pointer-events: none;
        `;
        document.body.appendChild(notification);
    }
    
    notification.innerHTML = `<i class="fas fa-keyboard"></i> ${message}`;
    notification.style.opacity = '1';
    
    setTimeout(() => {
        notification.style.opacity = '0';
    }, 2000);
}

// ============================================================
// TOOLTIPS
// ============================================================
function initTooltips() {
    const cards = document.querySelectorAll('.cours-card');
    
    cards.forEach(card => {
        const title = card.querySelector('h3')?.innerText;
        const description = card.querySelector('.cours-card-description')?.innerText;
        
        if (title && description) {
            card.setAttribute('data-tooltip', `${title} - ${description.substring(0, 100)}`);
            
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
        padding: 8px 14px;
        border-radius: 8px;
        font-size: 12px;
        max-width: 250px;
        z-index: 1000;
        pointer-events: none;
        white-space: normal;
        word-wrap: break-word;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    
    const rect = e.currentTarget.getBoundingClientRect();
    tooltip.style.top = (rect.top - 40) + 'px';
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
// SYSTÈME DE FAVORIS
// ============================================================
function initFavoriteSystem() {
    const cards = document.querySelectorAll('.cours-card');
    
    cards.forEach(card => {
        const coursId = card.querySelector('.cours-card-link')?.getAttribute('href')?.match(/coursId=(\d+)/)?.[1];
        if (coursId) {
            const favoriteBtn = document.createElement('button');
            favoriteBtn.className = 'favorite-btn';
            favoriteBtn.innerHTML = '<i class="far fa-heart"></i>';
            favoriteBtn.setAttribute('data-cours-id', coursId);
            favoriteBtn.onclick = (e) => {
                e.preventDefault();
                e.stopPropagation();
                toggleFavorite(coursId, favoriteBtn);
            };
            
            const actionArea = card.querySelector('.cours-card-action');
            if (actionArea) {
                actionArea.parentElement?.insertBefore(favoriteBtn, actionArea);
            }
            
            // Vérifier si le cours est favori
            if (isFavorite(coursId)) {
                favoriteBtn.innerHTML = '<i class="fas fa-heart"></i>';
                favoriteBtn.classList.add('active');
            }
        }
    });
    
    // Ajouter les styles des favoris
    addFavoriteStyles();
}

function toggleFavorite(coursId, btn) {
    let favorites = getFavorites();
    
    if (favorites.includes(coursId)) {
        favorites = favorites.filter(id => id !== coursId);
        btn.innerHTML = '<i class="far fa-heart"></i>';
        btn.classList.remove('active');
        showNotification('Retiré des favoris', 'info');
    } else {
        favorites.push(coursId);
        btn.innerHTML = '<i class="fas fa-heart"></i>';
        btn.classList.add('active');
        showNotification('Ajouté aux favoris', 'success');
    }
    
    localStorage.setItem('coursFavorites', JSON.stringify(favorites));
}

function getFavorites() {
    try {
        return JSON.parse(localStorage.getItem('coursFavorites')) || [];
    } catch(e) {
        return [];
    }
}

function isFavorite(coursId) {
    return getFavorites().includes(coursId);
}

function addFavoriteStyles() {
    const style = document.createElement('style');
    style.textContent = `
        .favorite-btn {
            background: none;
            border: none;
            font-size: 18px;
            cursor: pointer;
            color: #ccc;
            transition: all 0.2s;
            padding: 8px;
            border-radius: 50%;
            width: 36px;
            height: 36px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .favorite-btn:hover {
            color: #e74c3c;
            background: #fff0f0;
        }
        .favorite-btn.active {
            color: #e74c3c;
        }
        .cours-card .cours-card-link {
            display: flex;
            align-items: center;
        }
        @media (max-width: 900px) {
            .favorite-btn {
                position: absolute;
                top: 10px;
                right: 10px;
            }
        }
    `;
    document.head.appendChild(style);
}

// ============================================================
// SYSTÈME DE VU RÉCEMMENT
// ============================================================
function initRecentlyViewed() {
    const cards = document.querySelectorAll('.cours-card-link');
    
    cards.forEach(link => {
        link.addEventListener('click', function() {
            const coursId = this.getAttribute('href')?.match(/coursId=(\d+)/)?.[1];
            const coursTitle = this.querySelector('h3')?.innerText;
            if (coursId) {
                saveRecentlyViewed(coursId, coursTitle);
            }
        });
    });
}

function saveRecentlyViewed(coursId, coursTitle) {
    let recentlyViewed = getRecentlyViewed();
    
    recentlyViewed = recentlyViewed.filter(item => item.id !== coursId);
    recentlyViewed.unshift({ id: coursId, title: coursTitle, date: new Date().toISOString() });
    
    if (recentlyViewed.length > 5) recentlyViewed.pop();
    
    localStorage.setItem('recentlyViewed', JSON.stringify(recentlyViewed));
}

function getRecentlyViewed() {
    try {
        return JSON.parse(localStorage.getItem('recentlyViewed')) || [];
    } catch(e) {
        return [];
    }
}

// ============================================================
// NOTIFICATION
// ============================================================
function showNotification(message, type = 'success') {
    let notification = document.getElementById('dynamicNotification');
    if (!notification) {
        notification = document.createElement('div');
        notification.id = 'dynamicNotification';
        notification.style.cssText = `
            position: fixed;
            bottom: 80px;
            right: 20px;
            background: ${type === 'success' ? '#27ae60' : '#667eea'};
            color: white;
            padding: 12px 20px;
            border-radius: 10px;
            font-size: 13px;
            font-weight: 500;
            z-index: 1000;
            opacity: 0;
            transition: opacity 0.3s;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: flex;
            align-items: center;
            gap: 8px;
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
// ANIMATIONS CSS DYNAMIQUES
// ============================================================
const animationStyles = document.createElement('style');
animationStyles.textContent = `
    @keyframes fadeOut {
        from { opacity: 1; transform: scale(1); }
        to { opacity: 0; transform: scale(0.95); }
    }
    
    @keyframes fadeInUp {
        from { opacity: 0; transform: translateY(20px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    .cours-card {
        transition: all 0.3s ease;
    }
    
    .btn-reset-filters {
        background: #667eea;
        color: white;
        border: none;
        padding: 8px 20px;
        border-radius: 8px;
        margin-top: 15px;
        cursor: pointer;
        font-family: inherit;
        font-size: 13px;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
    
    .btn-reset-filters:hover {
        background: #5568d3;
    }
    
    .cours-count {
        margin-left: auto;
        font-size: 12px;
        color: #888;
        display: flex;
        align-items: center;
        gap: 6px;
    }
    
    @media (max-width: 900px) {
        .cours-count {
            margin-left: 0;
            margin-top: 10px;
        }
    }
`;
document.head.appendChild(animationStyles);

// ============================================================
// EXPOSITION GLOBALE (pour les appels depuis HTML)
// ============================================================
window.resetFilters = resetFilters;
window.showNotification = showNotification;