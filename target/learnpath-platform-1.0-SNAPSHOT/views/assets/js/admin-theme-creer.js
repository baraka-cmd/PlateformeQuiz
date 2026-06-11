// ============================================================
// FICHIER : views/assets/js/admin-theme-creer.js
// RÔLE    : Script du formulaire création/modification thème
//           - Prévisualisation en temps réel
//           - Sélecteur d'icônes interactif
//           - Palette de couleurs dynamique
//           - Validation avancée
//           - Compteurs de caractères
//           - Raccourcis clavier
//           - Messages toast
//           - Sauvegarde automatique (brouillon)
// ============================================================

(function() {
    'use strict';

    // ==========================================================
    // 1. CONFIGURATION
    // ==========================================================
    var CONFIG = {
        autoSaveInterval: 30000,        // Sauvegarde automatique toutes les 30 secondes
        searchDebounceDelay: 200,       // Délai pour la recherche d'icônes
        colorValidationRegex: /^[0-9a-fA-F]{6}$/,
        nomMinLength: 2,
        nomMaxLength: 100,
        descMaxLength: 300,
        toastDuration: 4000
    };

    // ==========================================================
    // 2. ÉTAT GLOBAL
    // ==========================================================
    var state = {
        selectedIcon: 'fas fa-book',
        selectedColor: '667eea',
        isDirty: false,
        autoSaveTimer: null,
        isSubmitting: false
    };

    // ==========================================================
    // 3. INITIALISATION
    // ==========================================================
    document.addEventListener('DOMContentLoaded', function() {
        initAll();
        log('Formulaire thème - Initialisé');
    });

    function initAll() {
        initIconSelector();
        initColorPicker();
        initLivePreview();
        initFormValidation();
        initCharacterCounters();
        initOrdreButtons();
        initAutoSave();
        initKeyboardShortcuts();
        initBeforeUnloadWarning();
        initTooltips();
    }

    // ==========================================================
    // 4. SÉLECTEUR D'ICÔNES
    // ==========================================================
    function initIconSelector() {
        var searchInput = document.getElementById('iconSearch');
        var iconItems = document.querySelectorAll('.atc-icon-item');
        var iconInput = document.getElementById('iconeInput');

        if (!searchInput || !iconInput) return;

        // Recherche d'icônes avec debounce
        var searchTimer;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimer);
            var searchTerm = this.value.toLowerCase().trim();

            searchTimer = setTimeout(function() {
                filterIcons(searchTerm);
            }, CONFIG.searchDebounceDelay);
        });

        // Raccourci : Échap pour vider la recherche
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                this.value = '';
                filterIcons('');
                this.blur();
            }
        });

        // Initialiser avec l'icône par défaut
        highlightSelectedIcon(state.selectedIcon);
    }

    function filterIcons(searchTerm) {
        var categories = document.querySelectorAll('.atc-icon-category');
        var totalVisible = 0;

        categories.forEach(function(category) {
            var items = category.querySelectorAll('.atc-icon-item');
            var categoryVisible = false;

            items.forEach(function(item) {
                var iconClass = item.getAttribute('data-icon').toLowerCase();
                var categoryName = category.getAttribute('data-category').toLowerCase();

                var matches = searchTerm === '' || 
                              iconClass.includes(searchTerm) || 
                              categoryName.includes(searchTerm);

                if (matches) {
                    item.style.display = '';
                    categoryVisible = true;
                    totalVisible++;
                } else {
                    item.style.display = 'none';
                }
            });

            category.style.display = categoryVisible ? '' : 'none';
        });

        // Afficher un message si aucun résultat
        var noResults = document.getElementById('iconNoResults');
        if (totalVisible === 0 && searchTerm !== '') {
            if (!noResults) {
                noResults = document.createElement('div');
                noResults.id = 'iconNoResults';
                noResults.style.cssText = 'text-align: center; padding: 20px; color: #8b8fa3; font-size: 13px;';
                noResults.innerHTML = '<i class="fas fa-search" style="font-size: 24px; display: block; margin-bottom: 8px;"></i>Aucune icône trouvée pour "' + searchTerm + '"';
                document.getElementById('iconSelector').appendChild(noResults);
            } else {
                noResults.style.display = '';
                noResults.querySelector('span').textContent = 'Aucune icône trouvée pour "' + searchTerm + '"';
            }
        } else if (noResults) {
            noResults.style.display = 'none';
        }
    }

    function highlightSelectedIcon(iconClass) {
        document.querySelectorAll('.atc-icon-item.selected').forEach(function(item) {
            item.classList.remove('selected');
        });

        var selectedItem = document.querySelector('.atc-icon-item[data-icon="' + iconClass + '"]');
        if (selectedItem) {
            selectedItem.classList.add('selected');
            // Faire défiler jusqu'à l'icône sélectionnée
            selectedItem.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }

    // Fonction globale pour sélectionner une icône
    window.selectionnerIcone = function(button, iconClass) {
        // Éviter la double sélection
        if (state.selectedIcon === iconClass) return;

        state.selectedIcon = iconClass;
        state.isDirty = true;

        // Mettre à jour le champ caché
        var iconInput = document.getElementById('iconeInput');
        if (iconInput) iconInput.value = iconClass;

        // Mettre à jour l'affichage de l'icône sélectionnée
        var displayIcon = document.querySelector('#iconSelectedDisplay i');
        var displayCode = document.querySelector('#iconSelectedDisplay code');
        if (displayIcon) displayIcon.className = iconClass;
        if (displayCode) displayCode.textContent = iconClass;

        // Mettre à jour l'aperçu
        var previewIcon = document.querySelector('#previewIcon i');
        var previewIconCode = document.getElementById('previewIconCode');
        if (previewIcon) previewIcon.className = iconClass;
        if (previewIconCode) previewIconCode.textContent = iconClass;

        // Mettre à jour la sélection visuelle
        highlightSelectedIcon(iconClass);

        // Animation de feedback
        if (button) {
            button.style.transform = 'scale(1.2)';
            setTimeout(function() {
                button.style.transform = button.classList.contains('selected') ? 'scale(1.05)' : '';
            }, 200);
        }
    };

    // ==========================================================
    // 5. SÉLECTEUR DE COULEUR
    // ==========================================================
    function initColorPicker() {
        var colorPicker = document.getElementById('couleurPicker');
        var colorInput = document.getElementById('couleur');
        var previewIcon = document.getElementById('previewIcon');
        var previewCouleurHex = document.getElementById('previewCouleurHex');
        var previewCouleurCode = document.getElementById('previewCouleurCode');

        if (!colorPicker || !colorInput) return;

        // Synchronisation color picker → champ texte
        colorPicker.addEventListener('input', function() {
            var hex = this.value.replace('#', '');
            updateColor(hex);
            highlightColorSwatch(hex);
            state.isDirty = true;
        });

        // Synchronisation champ texte → color picker
        colorInput.addEventListener('input', function() {
            var hex = this.value.replace('#', '').toLowerCase();
            
            // Accepter seulement les caractères hexadécimaux
            if (hex.match(/^[0-9a-f]*$/)) {
                if (hex.length <= 6) {
                    this.value = hex;
                    if (hex.length === 6) {
                        colorPicker.value = '#' + hex;
                        updateColor(hex);
                        highlightColorSwatch(hex);
                        state.isDirty = true;
                        colorInput.classList.remove('atc-input-error');
                    }
                }
            } else {
                // Empêcher les caractères invalides
                this.value = hex.replace(/[^0-9a-f]/gi, '').substring(0, 6);
            }
        });

        // Validation au blur
        colorInput.addEventListener('blur', function() {
            var hex = this.value.replace('#', '');
            if (hex.length === 6 && CONFIG.colorValidationRegex.test(hex)) {
                colorPicker.value = '#' + hex;
                updateColor(hex);
                highlightColorSwatch(hex);
                this.classList.remove('atc-input-error');
            } else if (hex.length > 0 && hex.length < 6) {
                // Compléter avec des zéros si incomplet
                var padded = hex.padEnd(6, '0');
                this.value = padded;
                colorPicker.value = '#' + padded;
                updateColor(padded);
                this.classList.remove('atc-input-error');
            }
        });

        // Coller une couleur complète
        colorInput.addEventListener('paste', function(e) {
            e.preventDefault();
            var pastedText = (e.clipboardData || window.clipboardData).getData('text');
            var hex = pastedText.replace('#', '').replace(/[^0-9a-f]/gi, '').substring(0, 6).toLowerCase();
            this.value = hex;
            if (hex.length === 6) {
                colorPicker.value = '#' + hex;
                updateColor(hex);
                highlightColorSwatch(hex);
                state.isDirty = true;
            }
        });

        // Empêcher les caractères non hexadécimaux
        colorInput.addEventListener('keydown', function(e) {
            var key = e.key.toLowerCase();
            var isValid = /^[0-9a-f]$/.test(key) || 
                          key === 'backspace' || 
                          key === 'delete' || 
                          key === 'arrowleft' || 
                          key === 'arrowright' || 
                          key === 'tab' ||
                          (e.ctrlKey && (key === 'v' || key === 'c' || key === 'a'));

            if (!isValid) {
                e.preventDefault();
            }
        });
    }

    function updateColor(hex) {
        var couleur = '#' + hex;
        state.selectedColor = hex;

        // Mettre à jour l'aperçu
        var previewIcon = document.getElementById('previewIcon');
        var previewCouleurHex = document.getElementById('previewCouleurHex');
        var previewCouleurCode = document.getElementById('previewCouleurCode');

        if (previewIcon) {
            previewIcon.style.background = 'linear-gradient(135deg, ' + couleur + ', ' + couleur + 'dd)';
        }
        if (previewCouleurHex) previewCouleurHex.textContent = couleur;
        if (previewCouleurCode) previewCouleurCode.textContent = couleur;

        // Mettre à jour la bordure de la carte aperçu
        var previewCard = document.getElementById('previewCard');
        if (previewCard) {
            previewCard.style.borderColor = couleur + '40';
        }
    }

    function highlightColorSwatch(hex) {
        document.querySelectorAll('.atc-color-swatch.selected').forEach(function(item) {
            item.classList.remove('selected');
        });

        var matchingSwatch = document.querySelector('.atc-color-swatch[data-color="' + hex + '"]');
        if (matchingSwatch) {
            matchingSwatch.classList.add('selected');
        }
    }

    // Fonction globale pour sélectionner une couleur
    window.selectionnerCouleur = function(button, couleurHex) {
        if (state.selectedColor === couleurHex) return;

        state.selectedColor = couleurHex;
        state.isDirty = true;

        document.getElementById('couleur').value = couleurHex;
        document.getElementById('couleurPicker').value = '#' + couleurHex;

        updateColor(couleurHex);
        highlightColorSwatch(couleurHex);

        // Animation de feedback
        button.style.transform = 'scale(1.2)';
        setTimeout(function() {
            button.style.transform = button.classList.contains('selected') ? 'scale(1.1)' : '';
        }, 200);
    };

    // ==========================================================
    // 6. PRÉVISUALISATION EN TEMPS RÉEL
    // ==========================================================
    function initLivePreview() {
        var nomInput = document.getElementById('nom');
        var descInput = document.getElementById('description');
        var ordreInput = document.getElementById('ordre');

        // Nom
        if (nomInput) {
            nomInput.addEventListener('input', function() {
                var nom = this.value.trim() || 'Nom du thème';
                var previewNom = document.getElementById('previewNom');
                if (previewNom) previewNom.textContent = nom;
                state.isDirty = true;
            });
        }

        // Description
        if (descInput) {
            descInput.addEventListener('input', function() {
                var desc = this.value.trim() || 'Description du thème apparaîtra ici';
                if (desc.length > 80) desc = desc.substring(0, 80) + '...';
                var previewDesc = document.getElementById('previewDescription');
                if (previewDesc) previewDesc.textContent = desc;
                state.isDirty = true;
            });
        }

        // Ordre
        if (ordreInput) {
            ordreInput.addEventListener('input', function() {
                var ordre = this.value || '1';
                var previewOrdre = document.getElementById('previewOrdre');
                var previewOrdreValue = document.getElementById('previewOrdreValue');
                if (previewOrdre) previewOrdre.textContent = 'Ordre : ' + ordre;
                if (previewOrdreValue) previewOrdreValue.textContent = ordre;
                state.isDirty = true;
            });
        }
    }

    // ==========================================================
    // 7. VALIDATION DU FORMULAIRE
    // ==========================================================
    function initFormValidation() {
        var form = document.getElementById('formThemeCreer');
        if (!form) return;

        form.addEventListener('submit', function(e) {
            if (!validateForm(e)) {
                e.preventDefault();
            } else {
                state.isSubmitting = true;
                // Désactiver le bouton pour éviter double soumission
                var submitBtn = document.getElementById('btnCreerTheme');
                if (submitBtn) {
                    submitBtn.disabled = true;
                    submitBtn.classList.add('loading');
                    submitBtn.querySelector('span').textContent = 'Création en cours...';
                }
            }
        });

        // Validation en temps réel
        var nomInput = document.getElementById('nom');
        if (nomInput) {
            nomInput.addEventListener('blur', function() {
                validateNom(false);
            });
            nomInput.addEventListener('input', function() {
                if (this.classList.contains('atc-input-error')) {
                    validateNom(false);
                }
            });
        }

        var couleurInput = document.getElementById('couleur');
        if (couleurInput) {
            couleurInput.addEventListener('blur', function() {
                validateCouleur(false);
            });
        }
    }

    function validateForm(showErrors) {
        var isValid = true;

        if (!validateNom(showErrors)) isValid = false;
        if (!validateCouleur(showErrors)) isValid = false;

        return isValid;
    }

    function validateNom(showErrors) {
        var nomInput = document.getElementById('nom');
        var nomError = document.getElementById('nomError');
        var nom = nomInput ? nomInput.value.trim() : '';

        if (!nom) {
            if (showErrors !== false) {
                nomInput.classList.add('atc-input-error');
                if (nomError) nomError.textContent = 'Le nom du thème est obligatoire.';
            }
            return false;
        }

        if (nom.length < CONFIG.nomMinLength) {
            if (showErrors !== false) {
                nomInput.classList.add('atc-input-error');
                if (nomError) nomError.textContent = 'Le nom doit contenir au moins ' + CONFIG.nomMinLength + ' caractères.';
            }
            return false;
        }

        if (nom.length > CONFIG.nomMaxLength) {
            if (showErrors !== false) {
                nomInput.classList.add('atc-input-error');
                if (nomError) nomError.textContent = 'Le nom ne peut pas dépasser ' + CONFIG.nomMaxLength + ' caractères.';
            }
            return false;
        }

        // Succès
        nomInput.classList.remove('atc-input-error');
        if (nomError) nomError.textContent = '';
        return true;
    }

    function validateCouleur(showErrors) {
        var couleurInput = document.getElementById('couleur');
        var couleur = couleurInput ? couleurInput.value.trim() : '';

        if (!couleur || !CONFIG.colorValidationRegex.test(couleur)) {
            if (showErrors !== false) {
                couleurInput.classList.add('atc-input-error');
            }
            return false;
        }

        couleurInput.classList.remove('atc-input-error');
        return true;
    }

    // ==========================================================
    // 8. COMPTEURS DE CARACTÈRES
    // ==========================================================
    function initCharacterCounters() {
        // Compteur pour le nom
        var nomInput = document.getElementById('nom');
        var nomCount = document.getElementById('nomCount');
        var nomCounter = document.getElementById('nomCounter');

        if (nomInput && nomCount) {
            updateCounter(nomInput, nomCount, nomCounter, CONFIG.nomMaxLength);
            nomInput.addEventListener('input', function() {
                updateCounter(this, nomCount, nomCounter, CONFIG.nomMaxLength);
            });
        }

        // Compteur pour la description
        var descInput = document.getElementById('description');
        var descCount = document.getElementById('descCount');
        var descCounter = document.getElementById('descCounter');

        if (descInput && descCount) {
            updateCounter(descInput, descCount, descCounter, CONFIG.descMaxLength);
            descInput.addEventListener('input', function() {
                updateCounter(this, descCount, descCounter, CONFIG.descMaxLength);
            });
        }
    }

    function updateCounter(input, countElement, counterElement, maxLength) {
        var length = input.value.length;
        countElement.textContent = length;

        if (counterElement) {
            var ratio = length / maxLength;
            if (ratio > 0.9) {
                counterElement.style.color = '#e74c3c';
            } else if (ratio > 0.7) {
                counterElement.style.color = '#f39c12';
            } else {
                counterElement.style.color = '#8b8fa3';
            }
        }
    }

    // ==========================================================
    // 9. BOUTONS D'ORDRE +/-
    // ==========================================================
    function initOrdreButtons() {
        window.incrementerOrdre = function() {
            var input = document.getElementById('ordre');
            if (!input) return;
            var val = parseInt(input.value) || 0;
            if (val < 999) {
                input.value = val + 1;
                input.dispatchEvent(new Event('input', { bubbles: true }));
                state.isDirty = true;
            }
        };

        window.decrementerOrdre = function() {
            var input = document.getElementById('ordre');
            if (!input) return;
            var val = parseInt(input.value) || 2;
            if (val > 1) {
                input.value = val - 1;
                input.dispatchEvent(new Event('input', { bubbles: true }));
                state.isDirty = true;
            }
        };

        // Support molette de souris sur le champ ordre
        var ordreInput = document.getElementById('ordre');
        if (ordreInput) {
            ordreInput.addEventListener('wheel', function(e) {
                if (document.activeElement === this) {
                    e.preventDefault();
                    if (e.deltaY < 0) {
                        window.incrementerOrdre();
                    } else {
                        window.decrementerOrdre();
                    }
                }
            });
        }
    }

    // ==========================================================
    // 10. SAUVEGARDE AUTOMATIQUE (BROUILLON LOCAL)
    // ==========================================================
    function initAutoSave() {
        window.addEventListener('beforeunload', function() {
            if (state.isDirty && !state.isSubmitting) {
                saveDraft();
            }
        });

        state.autoSaveTimer = setInterval(function() {
            if (state.isDirty && !state.isSubmitting) {
                saveDraft();
            }
        }, CONFIG.autoSaveInterval);

        // ⚠️ Restaurer le brouillon UNIQUEMENT sur la page de CRÉATION
        // Sur la page de modification, on supprime le brouillon
        var isCreationPage = window.location.pathname.indexOf('/creer') !== -1;

        if (isCreationPage) {
            restoreDraft();
        } else {
            // Page de modification : supprimer le brouillon pour éviter les conflits
            localStorage.removeItem('atc_theme_draft');
        }
    }
    function saveDraft() {
        var draft = {
            nom: document.getElementById('nom') ? document.getElementById('nom').value : '',
            description: document.getElementById('description') ? document.getElementById('description').value : '',
            icone: state.selectedIcon,
            couleur: state.selectedColor,
            ordre: document.getElementById('ordre') ? document.getElementById('ordre').value : '1',
            timestamp: Date.now()
        };

        try {
            localStorage.setItem('atc_theme_draft', JSON.stringify(draft));
        } catch (e) {
            // localStorage plein ou désactivé
        }
    }

    function restoreDraft() {
        try {
            var draftJson = localStorage.getItem('atc_theme_draft');
            if (!draftJson) return;

            var draft = JSON.parse(draftJson);
            
            // Vérifier si le brouillon date de moins de 24h
            var maxAge = 24 * 60 * 60 * 1000;
            if (Date.now() - draft.timestamp > maxAge) {
                localStorage.removeItem('atc_theme_draft');
                return;
            }

            // Restaurer les champs
            if (draft.nom && document.getElementById('nom')) {
                document.getElementById('nom').value = draft.nom;
                document.getElementById('nom').dispatchEvent(new Event('input'));
            }
            if (draft.description && document.getElementById('description')) {
                document.getElementById('description').value = draft.description;
                document.getElementById('description').dispatchEvent(new Event('input'));
            }
            if (draft.icone) {
                state.selectedIcon = draft.icone;
                document.getElementById('iconeInput').value = draft.icone;
                highlightSelectedIcon(draft.icone);
                var displayIcon = document.querySelector('#iconSelectedDisplay i');
                var displayCode = document.querySelector('#iconSelectedDisplay code');
                if (displayIcon) displayIcon.className = draft.icone;
                if (displayCode) displayCode.textContent = draft.icone;
                var previewIcon = document.querySelector('#previewIcon i');
                var previewIconCode = document.getElementById('previewIconCode');
                if (previewIcon) previewIcon.className = draft.icone;
                if (previewIconCode) previewIconCode.textContent = draft.icone;
            }
            if (draft.couleur) {
                state.selectedColor = draft.couleur;
                document.getElementById('couleur').value = draft.couleur;
                document.getElementById('couleurPicker').value = '#' + draft.couleur;
                updateColor(draft.couleur);
                highlightColorSwatch(draft.couleur);
            }
            if (draft.ordre && document.getElementById('ordre')) {
                document.getElementById('ordre').value = draft.ordre;
                document.getElementById('ordre').dispatchEvent(new Event('input'));
            }

            state.isDirty = true;
            showToast('Brouillon restauré avec succès', 'info');
        } catch (e) {
            // Données corrompues
            localStorage.removeItem('atc_theme_draft');
        }
    }

    // ==========================================================
    // 11. RACCOURCIS CLAVIER
    // ==========================================================
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ctrl+S : Sauvegarder (soumettre le formulaire)
            if ((e.ctrlKey || e.metaKey) && e.key === 's') {
                e.preventDefault();
                var form = document.getElementById('formThemeCreer');
                if (form && validateForm(true)) {
                    form.submit();
                }
            }

            // Échap : Retour à la liste
            if (e.key === 'Escape' && !document.activeElement.closest('input, textarea, select')) {
                window.location.href = document.querySelector('.atc-btn-secondary').getAttribute('href');
            }
        });
    }

    // ==========================================================
    // 12. AVERTISSEMENT AVANT DE QUITTER
    // ==========================================================
    function initBeforeUnloadWarning() {
        window.addEventListener('beforeunload', function(e) {
            if (state.isDirty && !state.isSubmitting) {
                var message = 'Vous avez des modifications non sauvegardées. Voulez-vous vraiment quitter ?';
                e.returnValue = message;
                return message;
            }
        });
    }

    // ==========================================================
    // 13. TOOLTIPS
    // ==========================================================
    function initTooltips() {
        var tooltipElements = document.querySelectorAll('[title]');

        tooltipElements.forEach(function(element) {
            var title = element.getAttribute('title');
            if (!title) return;

            element.addEventListener('mouseenter', function(e) {
                showTooltip(element, title, e);
            });

            element.addEventListener('mouseleave', function() {
                hideTooltip();
            });

            element.addEventListener('click', function() {
                hideTooltip();
            });
        });
    }

    var currentTooltip = null;

    function showTooltip(element, text, event) {
        hideTooltip();

        var tooltip = document.createElement('div');
        tooltip.className = 'atc-tooltip';
        tooltip.textContent = text;
        tooltip.style.cssText = [
            'position: fixed;',
            'z-index: 9999;',
            'background: #1a1a2e;',
            'color: white;',
            'padding: 8px 14px;',
            'border-radius: 8px;',
            'font-size: 12px;',
            'font-weight: 500;',
            'font-family: "Poppins", sans-serif;',
            'white-space: nowrap;',
            'pointer-events: none;',
            'box-shadow: 0 6px 20px rgba(0,0,0,0.25);',
            'opacity: 0;',
            'transform: translateY(4px);',
            'transition: opacity 0.2s ease, transform 0.2s ease;'
        ].join(' ');

        document.body.appendChild(tooltip);

        var rect = element.getBoundingClientRect();
        var tooltipRect = tooltip.getBoundingClientRect();
        var top = rect.top - tooltipRect.height - 8;
        var left = rect.left + (rect.width / 2) - (tooltipRect.width / 2);

        if (left < 12) left = 12;
        if (left + tooltipRect.width > window.innerWidth - 12) {
            left = window.innerWidth - tooltipRect.width - 12;
        }
        if (top < 12) {
            top = rect.bottom + 8;
        }

        tooltip.style.top = top + 'px';
        tooltip.style.left = left + 'px';

        requestAnimationFrame(function() {
            tooltip.style.opacity = '1';
            tooltip.style.transform = 'translateY(0)';
        });

        currentTooltip = tooltip;
    }

    function hideTooltip() {
        if (currentTooltip) {
            currentTooltip.style.opacity = '0';
            currentTooltip.style.transform = 'translateY(4px)';
            var ref = currentTooltip;
            setTimeout(function() {
                if (ref.parentNode) ref.parentNode.removeChild(ref);
            }, 200);
            currentTooltip = null;
        }
    }

    // ==========================================================
    // 14. NOTIFICATION TOAST
    // ==========================================================
    function showToast(message, type) {
        type = type || 'info';

        var icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            info: 'fa-info-circle',
            warning: 'fa-exclamation-triangle'
        };

        var colors = {
            success: '#27ae60',
            error: '#e74c3c',
            info: '#667eea',
            warning: '#f39c12'
        };

        var existingToasts = document.querySelectorAll('.atc-toast');
        existingToasts.forEach(function(t) { t.remove(); });

        var toast = document.createElement('div');
        toast.className = 'atc-toast';
        toast.setAttribute('role', 'alert');
        toast.style.cssText = [
            'position: fixed;',
            'bottom: 30px;',
            'right: 30px;',
            'z-index: 9999;',
            'background: white;',
            'padding: 14px 20px;',
            'border-radius: 12px;',
            'box-shadow: 0 10px 40px rgba(0,0,0,0.15);',
            'display: flex;',
            'align-items: center;',
            'gap: 10px;',
            'font-size: 13px;',
            'font-weight: 500;',
            'font-family: "Poppins", sans-serif;',
            'max-width: 400px;',
            'animation: atcToastIn 0.4s ease;',
            'border-left: 4px solid ' + (colors[type] || '#667eea') + ';'
        ].join(' ');

        toast.innerHTML = [
            '<i class="fas ' + (icons[type] || 'fa-info-circle') + '"',
            'style="font-size: 18px; color: ' + (colors[type] || '#667eea') + '; flex-shrink: 0;"></i>',
            '<span style="flex: 1; color: #1a1a2e;">' + message + '</span>'
        ].join('');

        document.body.appendChild(toast);

        setTimeout(function() {
            if (toast.parentNode) {
                toast.style.transition = 'all 0.3s ease';
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(40px)';
                setTimeout(function() {
                    if (toast.parentNode) toast.parentNode.removeChild(toast);
                }, 300);
            }
        }, CONFIG.toastDuration);
    }

    // Injecter l'animation du toast
    if (!document.getElementById('atc-toast-style')) {
        var style = document.createElement('style');
        style.id = 'atc-toast-style';
        style.textContent = '@keyframes atcToastIn { from { opacity: 0; transform: translateX(50px); } to { opacity: 1; transform: translateX(0); } }';
        document.head.appendChild(style);
    }

    // ==========================================================
    // 15. NETTOYAGE
    // ==========================================================
    window.addEventListener('unload', function() {
        if (state.isSubmitting) {
            // Si le formulaire a été soumis, supprimer le brouillon
            localStorage.removeItem('atc_theme_draft');
        }
        if (state.autoSaveTimer) {
            clearInterval(state.autoSaveTimer);
        }
    });

    // ==========================================================
    // 16. UTILITAIRES
    // ==========================================================
    function log(message) {
        if (window.console && console.log) {
            console.log('%c[Admin Thème Créer] %c' + message,
                'color: #667eea; font-weight: 700;',
                'color: #555;');
        }
    }

    // Exposer les fonctions globales
    window.selectionnerIcone = window.selectionnerIcone;
    window.selectionnerCouleur = window.selectionnerCouleur;
    window.incrementerOrdre = window.incrementerOrdre;
    window.decrementerOrdre = window.decrementerOrdre;

})();