/**
 * ============================================================
 * FICHIER : views/assets/js/cours-lecon.js
 * RÔLE    : Scripts de la page de contenu d'une leçon
 *           - Copie du code source dans le presse-papier
 *           - Agrandissement/réduction du bloc de code
 *           - Calcul et affichage du temps de lecture
 *           - Suivi du temps passé sur la leçon
 *           - Validation de la leçon
 *           - Gestion de la barre latérale sticky
 *           - Défilement fluide
 *           - Raccourcis clavier
 *           - Notification de copie
 * ============================================================
 */

(function() {
    'use strict';

    /* ==========================================================
     SECTION 1 : VARIABLES GLOBALES
     ========================================================== */
    var tempsDebutLecon = window.TEMPS_DEBUT_LECON || Date.now();
    var estTerminee = window.EST_TERMINEE || false;
    var leconId = window.LECON_ID || 0;
    var coursId = window.COURS_ID || 0;
    var chapitreId = window.CHAPITRE_ID || 0;
    var tempsPasseTotal = 0;
    var tempsInterval = null;

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCodeCopy();
        initCodeExpand();
        initTempsLecture();
        initSuiviTemps();
        initSidebarSticky();
        initSmoothScroll();
        initKeyboardShortcuts();
        initTableResponsive();
        initExternalLinks();
    });

    /* ==========================================================
     SECTION 3 : COPIE DU CODE SOURCE
     ========================================================== */
    function initCodeCopy() {
        var copyBtn = document.querySelector('.code-copy-btn');
        if (!copyBtn) return;

        copyBtn.addEventListener('click', function() {
            var codeBlock = document.getElementById('codeBlock');
            if (!codeBlock) return;

            var codeText = codeBlock.textContent || codeBlock.innerText;

            // Utiliser l'API Clipboard moderne
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(codeText).then(function() {
                    showCopyNotification('Code copié dans le presse-papier !', 'success');
                    animateCopyButton(copyBtn);
                }).catch(function() {
                    fallbackCopy(codeText);
                    showCopyNotification('Code copié dans le presse-papier !', 'success');
                    animateCopyButton(copyBtn);
                });
            } else {
                // Fallback pour navigateurs plus anciens
                fallbackCopy(codeText);
                showCopyNotification('Code copié dans le presse-papier !', 'success');
                animateCopyButton(copyBtn);
            }
        });
    }

    /**
     * Méthode de fallback pour copier le texte
     */
    function fallbackCopy(text) {
        var textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
        } catch (err) {
            console.error('Erreur lors de la copie :', err);
        }
        document.body.removeChild(textarea);
    }

    /**
     * Anime le bouton de copie après un clic
     */
    function animateCopyButton(btn) {
        var originalHTML = btn.innerHTML;
        
        btn.innerHTML = '<i class="fas fa-check"></i><span>Copié !</span>';
        btn.style.background = 'rgba(39, 174, 96, 0.4)';
        btn.style.transition = 'all 0.3s ease';
        
        setTimeout(function() {
            btn.innerHTML = originalHTML;
            btn.style.background = 'rgba(102, 126, 234, 0.2)';
        }, 2000);
    }

    /* ==========================================================
     SECTION 4 : AGRANDISSEMENT DU BLOC DE CODE
     ========================================================== */
    function initCodeExpand() {
        var expandBtn = document.querySelector('.code-expand-btn');
        if (!expandBtn) return;

        var codeBlock = document.getElementById('codeBlock');
        if (!codeBlock) return;

        var isExpanded = false;

        expandBtn.addEventListener('click', function() {
            isExpanded = !isExpanded;

            if (isExpanded) {
                codeBlock.classList.add('expanded');
                expandBtn.innerHTML = '<i class="fas fa-compress"></i><span>Réduire</span>';
                
                // Scroll vers le bloc de code
                codeBlock.scrollIntoView({ behavior: 'smooth', block: 'start' });
            } else {
                codeBlock.classList.remove('expanded');
                expandBtn.innerHTML = '<i class="fas fa-expand"></i><span>Agrandir</span>';
            }
        });

        // Raccourci clavier pour agrandir/réduire
        document.addEventListener('keydown', function(e) {
            if (e.ctrlKey && e.key === 'e') {
                e.preventDefault();
                expandBtn.click();
            }
        });
    }

    /* ==========================================================
     SECTION 5 : NOTIFICATION DE COPIE
     ========================================================== */
    function showCopyNotification(message, type) {
        var notification = document.getElementById('copieNotification');
        if (!notification) {
            // Créer la notification si elle n'existe pas
            notification = document.createElement('div');
            notification.id = 'copieNotification';
            notification.className = 'copie-notification';
            document.body.appendChild(notification);
        }

        notification.innerHTML = '<i class="fas ' + (type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle') + '"></i><span>' + message + '</span>';
        
        // Afficher
        notification.classList.add('show');

        // Masquer après 2.5 secondes
        clearTimeout(notification._timeout);
        notification._timeout = setTimeout(function() {
            notification.classList.remove('show');
        }, 2500);
    }

    /* ==========================================================
     SECTION 6 : CALCUL DU TEMPS DE LECTURE
     ========================================================== */
    function initTempsLecture() {
        var texteElement = document.querySelector('.lecon-texte');
        if (!texteElement) return;

        // Le temps de lecture est déjà calculé côté serveur
        // On peut ajouter une barre de progression de lecture
        createReadingProgressBar();
    }

    /**
     * Crée une barre de progression de lecture en haut de la page
     */
    function createReadingProgressBar() {
        var progressBar = document.createElement('div');
        progressBar.className = 'reading-progress-bar';
        progressBar.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            height: 3px;
            background: linear-gradient(90deg, #667eea, #764ba2);
            z-index: 10000;
            transition: width 0.2s ease;
        `;
        document.body.appendChild(progressBar);

        // Mettre à jour la barre au scroll
        window.addEventListener('scroll', function() {
            var windowHeight = window.innerHeight;
            var documentHeight = document.documentElement.scrollHeight - windowHeight;
            var scrollTop = window.scrollY;
            
            if (documentHeight > 0) {
                var progress = (scrollTop / documentHeight) * 100;
                progressBar.style.width = Math.min(progress, 100) + '%';
            }
        });
    }

    /* ==========================================================
     SECTION 7 : SUIVI DU TEMPS PASSÉ
     ========================================================== */
    function initSuiviTemps() {
        if (estTerminee) return;

        // Mettre à jour le temps passé toutes les 30 secondes
        tempsInterval = setInterval(function() {
            tempsPasseTotal += 30;
            updateTempsPasseInput();
        }, 30000);

        // Sauvegarder le temps quand l'utilisateur quitte la page
        window.addEventListener('beforeunload', function() {
            tempsPasseTotal += Math.floor((Date.now() - tempsDebutLecon) / 1000);
            updateTempsPasseInput();
            
            // Envoyer le temps via Beacon API si disponible
            if (navigator.sendBeacon) {
                var formData = new FormData();
                formData.append('coursId', coursId);
                formData.append('chapitreId', chapitreId);
                formData.append('leconId', leconId);
                formData.append('tempsPasse', tempsPasseTotal);
                
                navigator.sendBeacon(
                    window.location.origin + '/amijayQuizz/joueur/cours/valider',
                    formData
                );
            }
        });

        // Mettre en pause quand la page est cachée
        document.addEventListener('visibilitychange', function() {
            if (document.hidden) {
                tempsPasseTotal += Math.floor((Date.now() - tempsDebutLecon) / 1000);
                tempsDebutLecon = Date.now();
            }
        });
    }

    /**
     * Met à jour le champ caché du temps passé
     */
    function updateTempsPasseInput() {
        var input = document.getElementById('tempsPasseInput');
        if (input) {
            input.value = tempsPasseTotal;
        }
    }

    /* ==========================================================
     SECTION 8 : VALIDATION DE LA LEÇON
     ========================================================== */
    window.validerLecon = function() {
        if (estTerminee) return;

        // Arrêter le suivi du temps
        if (tempsInterval) {
            clearInterval(tempsInterval);
        }
        
        tempsPasseTotal += Math.floor((Date.now() - tempsDebutLecon) / 1000);
        updateTempsPasseInput();

        // Afficher un indicateur de chargement
        var btnValider = document.querySelector('.btn-nav-lecon.valider');
        if (btnValider) {
            var originalHTML = btnValider.innerHTML;
            btnValider.innerHTML = '<i class="fas fa-spinner fa-spin"></i><div class="btn-nav-text"><span class="nav-label">Validation...</span></div>';
            btnValider.style.pointerEvents = 'none';
            btnValider.style.opacity = '0.7';
        }

        // Soumettre le formulaire
        var form = document.getElementById('formValiderLecon');
        if (form) {
            form.submit();
        }
    };

    /* ==========================================================
     SECTION 9 : BARRE LATÉRALE STICKY
     ========================================================== */
    function initSidebarSticky() {
        var sidebar = document.querySelector('.lecon-sidebar');
        if (!sidebar || window.innerWidth < 900) return;

        var sidebarTop = sidebar.getBoundingClientRect().top + window.scrollY;
        var footer = document.querySelector('footer');
        
        window.addEventListener('scroll', function() {
            if (!footer) return;
            
            var footerTop = footer.getBoundingClientRect().top + window.scrollY;
            var sidebarHeight = sidebar.offsetHeight;
            var scrollY = window.scrollY;
            var offset = 20;

            if (scrollY + sidebarHeight + offset > footerTop) {
                // Le sidebar atteint le footer
                var newTop = footerTop - sidebarHeight - offset;
                sidebar.style.position = 'absolute';
                sidebar.style.top = newTop + 'px';
            } else if (scrollY > sidebarTop - offset) {
                // Le sidebar est sticky
                sidebar.style.position = 'fixed';
                sidebar.style.top = offset + 'px';
            } else {
                // Le sidebar est en position normale
                sidebar.style.position = 'sticky';
                sidebar.style.top = offset + 'px';
            }
        });
    }

    /* ==========================================================
     SECTION 10 : DÉFILEMENT FLUIDE
     ========================================================== */
    function initSmoothScroll() {
        // Défilement fluide pour les ancres dans le contenu
        document.querySelectorAll('.lecon-texte a[href^="#"]').forEach(function(anchor) {
            anchor.addEventListener('click', function(e) {
                var targetId = this.getAttribute('href').substring(1);
                var target = document.getElementById(targetId);
                
                if (target) {
                    e.preventDefault();
                    target.scrollIntoView({ 
                        behavior: 'smooth', 
                        block: 'start' 
                    });
                    
                    // Mettre à jour l'URL
                    history.pushState(null, null, '#' + targetId);
                }
            });
        });
    }

    /* ==========================================================
     SECTION 11 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            // Ne pas déclencher si on est dans un champ de formulaire
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.isContentEditable) {
                return;
            }

            switch (e.key.toLowerCase()) {
                case 'arrowleft':
                    if (e.altKey) {
                        // Leçon précédente
                        e.preventDefault();
                        var btnPrecedent = document.querySelector('.btn-nav-lecon.precedent');
                        if (btnPrecedent) {
                            window.location.href = btnPrecedent.getAttribute('href');
                        }
                    }
                    break;
                    
                case 'arrowright':
                    if (e.altKey) {
                        // Leçon suivante ou validation
                        e.preventDefault();
                        var btnSuivant = document.querySelector('.btn-nav-lecon.valider');
                        if (btnSuivant) {
                            if (btnSuivant.tagName === 'BUTTON') {
                                btnSuivant.click();
                            } else if (btnSuivant.tagName === 'A') {
                                window.location.href = btnSuivant.getAttribute('href');
                            }
                        }
                    }
                    break;
                    
                case 'enter':
                    if (e.ctrlKey && !estTerminee) {
                        // Valider la leçon
                        e.preventDefault();
                        window.validerLecon();
                    }
                    break;
                    
                case 'c':
                    if (e.ctrlKey && e.shiftKey) {
                        // Copier le code
                        e.preventDefault();
                        var copyBtn = document.querySelector('.code-copy-btn');
                        if (copyBtn) {
                            copyBtn.click();
                        }
                    }
                    break;
                    
                case 'e':
                    if (e.ctrlKey && !e.shiftKey) {
                        // Agrandir/réduire le code
                        e.preventDefault();
                        var expandBtn = document.querySelector('.code-expand-btn');
                        if (expandBtn) {
                            expandBtn.click();
                        }
                    }
                    break;
            }
        });
    }

    /* ==========================================================
     SECTION 12 : TABLEAUX RESPONSIVE
     ========================================================== */
    function initTableResponsive() {
        var tables = document.querySelectorAll('.lecon-texte table');
        if (tables.length === 0) return;

        tables.forEach(function(table) {
            var wrapper = document.createElement('div');
            wrapper.className = 'table-responsive-wrapper';
            wrapper.style.cssText = 'overflow-x: auto; -webkit-overflow-scrolling: touch; margin: 20px 0;';
            
            table.parentNode.insertBefore(wrapper, table);
            wrapper.appendChild(table);
        });
    }

    /* ==========================================================
     SECTION 13 : LIENS EXTERNES
     ========================================================== */
    function initExternalLinks() {
        // Ouvrir les liens externes dans un nouvel onglet
        var externalLinks = document.querySelectorAll('.lecon-texte a[href^="http"]');
        externalLinks.forEach(function(link) {
            // Ne pas modifier les liens déjà configurés
            if (!link.hasAttribute('target')) {
                link.setAttribute('target', '_blank');
                link.setAttribute('rel', 'noopener noreferrer');
            }
            
            // Ajouter un tooltip
            link.setAttribute('title', 'Ouvrir dans un nouvel onglet : ' + link.getAttribute('href'));
        });
    }

    /* ==========================================================
     SECTION 14 : ANIMATION DES BLOCS SPÉCIAUX
     ========================================================== */
    function initSpecialBlocksAnimation() {
        var specialBlocks = document.querySelectorAll(
            '.note-info, .note-success, .note-warning, .note-danger, .note-tip, .definition, .exemple, .resume, .exercice'
        );
        if (specialBlocks.length === 0) return;

        var observerOptions = {
            root: null,
            rootMargin: '0px 0px -30px 0px',
            threshold: 0.1
        };

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateX(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, observerOptions);

        specialBlocks.forEach(function(block) {
            block.style.opacity = '0';
            block.style.transform = 'translateX(-15px)';
            block.style.transition = 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            observer.observe(block);
        });
    }

    setTimeout(initSpecialBlocksAnimation, 300);

    /* ==========================================================
     SECTION 15 : GESTION DES IMAGES
     ========================================================== */
    function initImageZoom() {
        var images = document.querySelectorAll('.lecon-texte img');
        if (images.length === 0) return;

        images.forEach(function(img) {
            img.style.cursor = 'zoom-in';
            img.setAttribute('title', 'Cliquer pour agrandir');
            
            img.addEventListener('click', function() {
                openImageModal(this.getAttribute('src'), this.getAttribute('alt'));
            });
        });
    }

    /**
     * Ouvre une modale pour afficher l'image en grand
     */
    function openImageModal(src, alt) {
        // Supprimer une modale existante
        var existingModal = document.querySelector('.image-modal-overlay');
        if (existingModal) {
            existingModal.remove();
        }

        var overlay = document.createElement('div');
        overlay.className = 'image-modal-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.9);
            z-index: 10001;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: zoom-out;
            animation: clFadeIn 0.3s ease;
        `;

        var img = document.createElement('img');
        img.src = src;
        img.alt = alt || '';
        img.style.cssText = `
            max-width: 90%;
            max-height: 90%;
            border-radius: 8px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
        `;

        var closeBtn = document.createElement('button');
        closeBtn.innerHTML = '<i class="fas fa-times"></i>';
        closeBtn.style.cssText = `
            position: absolute;
            top: 20px;
            right: 20px;
            background: rgba(255,255,255,0.2);
            border: none;
            color: white;
            width: 44px;
            height: 44px;
            border-radius: 50%;
            font-size: 20px;
            cursor: pointer;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            justify-content: center;
        `;
        closeBtn.addEventListener('mouseenter', function() {
            this.style.background = 'rgba(255,255,255,0.4)';
            this.style.transform = 'scale(1.1)';
        });
        closeBtn.addEventListener('mouseleave', function() {
            this.style.background = 'rgba(255,255,255,0.2)';
            this.style.transform = 'scale(1)';
        });

        overlay.appendChild(img);
        overlay.appendChild(closeBtn);
        document.body.appendChild(overlay);

        // Fermer au clic sur l'overlay
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) {
                overlay.remove();
            }
        });

        // Fermer avec le bouton
        closeBtn.addEventListener('click', function() {
            overlay.remove();
        });

        // Fermer avec Échap
        var escHandler = function(e) {
            if (e.key === 'Escape') {
                overlay.remove();
                document.removeEventListener('keydown', escHandler);
            }
        };
        document.addEventListener('keydown', escHandler);
    }

    setTimeout(initImageZoom, 500);

    /* ==========================================================
     SECTION 16 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            document.body.classList.add('page-hidden');
        } else {
            document.body.classList.remove('page-hidden');
        }
    });

    var performanceStyle = document.createElement('style');
    performanceStyle.textContent = `
        .page-hidden .lecon-texte * {
            animation: none !important;
            transition: none !important;
        }
        
        @keyframes clFadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(performanceStyle);

})();