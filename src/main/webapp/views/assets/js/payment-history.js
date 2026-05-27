'use strict';

document.addEventListener('DOMContentLoaded', function() {
    
    const filterBtns = document.querySelectorAll('.filter-btn');
    const searchInput = document.getElementById('searchInput');
    const paymentItems = document.querySelectorAll('.payment-item');
    let currentFilter = 'all';
    
    // Filtrer les paiements
    function filterPayments() {
        const searchTerm = searchInput ? searchInput.value.toLowerCase() : '';
        
        paymentItems.forEach(item => {
            const status = item.dataset.status;
            const title = item.dataset.title || '';
            
            let statusMatch = false;
            if (currentFilter === 'all') {
                statusMatch = true;
            } else {
                statusMatch = status === currentFilter;
            }
            
            let searchMatch = true;
            if (searchTerm) {
                searchMatch = title.includes(searchTerm);
            }
            
            if (statusMatch && searchMatch) {
                item.style.display = 'flex';
            } else {
                item.style.display = 'none';
            }
        });
        
        updateEmptyMessage();
    }
    
    // Mettre à jour le message vide
    function updateEmptyMessage() {
        let visibleItems = 0;
        paymentItems.forEach(item => {
            if (item.style.display !== 'none') visibleItems++;
        });
        
        let emptyMsg = document.getElementById('filterEmptyMessage');
        if (visibleItems === 0 && paymentItems.length > 0) {
            if (!emptyMsg) {
                emptyMsg = document.createElement('div');
                emptyMsg.id = 'filterEmptyMessage';
                emptyMsg.className = 'empty-state';
                emptyMsg.innerHTML = `
                    <i class="fas fa-filter"></i>
                    <h3>Aucun résultat</h3>
                    <p>Aucun paiement ne correspond à vos critères de recherche.</p>
                    <button class="btn-browse" onclick="resetFilters()">
                        <i class="fas fa-undo"></i> Réinitialiser les filtres
                    </button>
                `;
                const paymentsList = document.querySelector('.payments-list');
                if (paymentsList) paymentsList.appendChild(emptyMsg);
            }
        } else if (emptyMsg) {
            emptyMsg.remove();
        }
    }
    
    // Réinitialiser les filtres
    window.resetFilters = function() {
        if (searchInput) searchInput.value = '';
        currentFilter = 'all';
        
        filterBtns.forEach(btn => {
            if (btn.dataset.filter === 'all') {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
        
        filterPayments();
    };
    
    // Événements des filtres
    filterBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            filterBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            filterPayments();
        });
    });
    
    // Événement de recherche
    if (searchInput) {
        searchInput.addEventListener('input', filterPayments);
    }
    
    // Animation d'entrée
    paymentItems.forEach((item, index) => {
        item.style.opacity = '0';
        item.style.transform = 'translateX(-10px)';
        setTimeout(() => {
            item.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';
        }, index * 50);
    });
    
    // Sauvegarder les stats dans localStorage
    try {
        const statValues = document.querySelectorAll('.stat-value');
        if (statValues.length > 0) {
            const stats = {
                totalPayments: statValues[0]?.innerText,
                successfulCount: statValues[1]?.innerText,
                totalAmount: statValues[2]?.innerText,
                date: new Date().toISOString()
            };
            localStorage.setItem('lastPaymentStats', JSON.stringify(stats));
        }
    } catch(e) {}
});