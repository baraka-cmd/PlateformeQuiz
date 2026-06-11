// ============================================================
// FICHIER : views/assets/js/cours-lecon.js
// RÔLE    : Scripts de la page de contenu d'une lecon LearnPath
//           - Copie du code source dans le presse-papier
//           - Agrandissement/reduction du bloc de code
//           - Suivi du temps passe sur la lecon
//           - Validation de la lecon
//           - Modales pour le plan du cours et la vue du chapitre
//           - Barre de progression de lecture
//           - Gestion de la sidebar sticky
//           - Raccourcis clavier
//           - Zoom des images
//           - Notification de copie
// ============================================================

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
    var codeExpanded = false;

    /* ==========================================================
     SECTION 2 : INITIALISATION
     ========================================================== */
    document.addEventListener('DOMContentLoaded', function() {
        initCodeCopy();
        initCodeExpand();
        initSuiviTemps();
        initSidebarSticky();
        initKeyboardShortcuts();
        initTableResponsive();
        initExternalLinks();
        initImageZoom();
        initModalCloseOnEscape();
        initModalCloseOnOverlayClick();
        initReadingProgressBar();
        initSmoothScrollAnchors();
        initSpecialBlocksAnimation();
        console.log('Cours Lecon - Page initialisee');
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

            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(codeText).then(function() {
                    showCopyNotification('Code copie dans le presse-papier', 'success');
                    animateCopyButton(copyBtn);
                }).catch(function() {
                    fallbackCopy(codeText);
                    showCopyNotification('Code copie dans le presse-papier', 'success');
                    animateCopyButton(copyBtn);
                });
            } else {
                fallbackCopy(codeText);
                showCopyNotification('Code copie dans le presse-papier', 'success');
                animateCopyButton(copyBtn);
            }
        });
    }

    function fallbackCopy(text) {
        var textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        textarea.style.top = '0';
        textarea.style.left = '0';
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        try {
            document.execCommand('copy');
        } catch (err) {
            console.error('Erreur lors de la copie :', err);
        }
        document.body.removeChild(textarea);
    }

    function animateCopyButton(btn) {
        var originalHTML = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-check"></i><span>Copie</span>';
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

        expandBtn.addEventListener('click', function() {
            codeExpanded = !codeExpanded;

            if (codeExpanded) {
                if (codeBlock) {
                    codeBlock.classList.add('expanded');
                }
                expandBtn.innerHTML = '<i class="fas fa-compress"></i><span>Reduire</span>';
                if (codeBlock) {
                    codeBlock.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            } else {
                if (codeBlock) {
                    codeBlock.classList.remove('expanded');
                }
                expandBtn.innerHTML = '<i class="fas fa-expand"></i><span>Agrandir</span>';
            }
        });
    }

    /* ==========================================================
     SECTION 5 : NOTIFICATION DE COPIE
     ========================================================== */
    function showCopyNotification(message, type) {
        var notification = document.getElementById('copieNotification');
        if (!notification) {
            notification = document.createElement('div');
            notification.id = 'copieNotification';
            notification.className = 'copie-notification';
            document.body.appendChild(notification);
        }

        var iconClass = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
        notification.innerHTML = '<i class="fas ' + iconClass + '"></i><span>' + message + '</span>';
        notification.classList.add('show');

        clearTimeout(notification._timeout);
        notification._timeout = setTimeout(function() {
            notification.classList.remove('show');
        }, 2500);
    }

    /* ==========================================================
     SECTION 6 : BARRE DE PROGRESSION DE LECTURE
     ========================================================== */
    function initReadingProgressBar() {
        var progressBar = document.createElement('div');
        progressBar.className = 'reading-progress-bar';
        progressBar.setAttribute('aria-hidden', 'true');
        progressBar.style.cssText = 'position:fixed;top:0;left:0;height:3px;background:linear-gradient(90deg,#667eea,#764ba2);z-index:10001;transition:width 0.2s ease;pointer-events:none;';
        document.body.appendChild(progressBar);

        window.addEventListener('scroll', function() {
            var windowHeight = window.innerHeight;
            var documentHeight = document.documentElement.scrollHeight - windowHeight;
            var scrollTop = window.scrollY || window.pageYOffset;

            if (documentHeight > 0) {
                var progress = Math.min((scrollTop / documentHeight) * 100, 100);
                progressBar.style.width = progress + '%';
            }
        }, { passive: true });
    }

    /* ==========================================================
     SECTION 7 : SUIVI DU TEMPS PASSE
     ========================================================== */
    function initSuiviTemps() {
        if (estTerminee) return;

        tempsInterval = setInterval(function() {
            tempsPasseTotal += 30;
            updateTempsPasseInput();
        }, 30000);

        window.addEventListener('beforeunload', function() {
            tempsPasseTotal += Math.floor((Date.now() - tempsDebutLecon) / 1000);
            updateTempsPasseInput();

            if (navigator.sendBeacon) {
                var formData = new FormData();
                formData.append('coursId', coursId);
                formData.append('chapitreId', chapitreId);
                formData.append('leconId', leconId);
                formData.append('tempsPasse', tempsPasseTotal);
                navigator.sendBeacon(
                    window.location.origin + '/LearnPathz/joueur/cours/valider',
                    formData
                );
            }
        });

        document.addEventListener('visibilitychange', function() {
            if (document.hidden) {
                tempsPasseTotal += Math.floor((Date.now() - tempsDebutLecon) / 1000);
                tempsDebutLecon = Date.now();
            } else {
                tempsDebutLecon = Date.now();
            }
        });
    }

    function updateTempsPasseInput() {
        var input = document.getElementById('tempsPasseInput');
        if (input) {
            input.value = tempsPasseTotal;
        }
    }

    /* ==========================================================
     SECTION 8 : VALIDATION DE LA LECON
     ========================================================== */
    window.validerLecon = function() {
        if (estTerminee) return;

        if (tempsInterval) {
            clearInterval(tempsInterval);
        }

        tempsPasseTotal += Math.floor((Date.now() - tempsDebutLecon) / 1000);
        updateTempsPasseInput();

        var btnValider = document.querySelector('.btn-nav-lecon.valider');
        if (btnValider && btnValider.tagName === 'BUTTON') {
            btnValider.innerHTML = '<i class="fas fa-spinner fa-spin"></i><div class="btn-nav-text"><span class="nav-label">Validation...</span></div>';
            btnValider.style.pointerEvents = 'none';
            btnValider.style.opacity = '0.7';
        }

        var form = document.getElementById('formValiderLecon');
        if (form) {
            form.submit();
        }
    };

    /* ==========================================================
     SECTION 9 : BARRE LATERALE STICKY
     ========================================================== */
    function initSidebarSticky() {
        var sidebar = document.getElementById('leconSidebar');
        if (!sidebar || window.innerWidth < 1000) return;

        var footer = document.querySelector('footer');
        var navbar = document.querySelector('.navbar');
        var navbarHeight = navbar ? navbar.offsetHeight : 65;
        var offset = 20;

        function updateSidebarPosition() {
            if (!sidebar || !footer) return;

            var sidebarRect = sidebar.getBoundingClientRect();
            var footerRect = footer.getBoundingClientRect();
            var sidebarHeight = sidebar.offsetHeight;
            var scrollY = window.scrollY || window.pageYOffset;

            var sidebarNaturalTop = sidebar.offsetTop;
            var footerTop = footerRect.top + scrollY;

            if (scrollY + sidebarHeight + offset > footerTop) {
                sidebar.style.position = 'absolute';
                sidebar.style.top = (footerTop - sidebarHeight - offset) + 'px';
            } else if (scrollY > sidebarNaturalTop - navbarHeight - offset) {
                sidebar.style.position = 'fixed';
                sidebar.style.top = (navbarHeight + offset) + 'px';
            } else {
                sidebar.style.position = 'sticky';
                sidebar.style.top = (navbarHeight + offset) + 'px';
            }
        }

        updateSidebarPosition();

        window.addEventListener('scroll', updateSidebarPosition, { passive: true });
        window.addEventListener('resize', updateSidebarPosition, { passive: true });
    }

    /* ==========================================================
     SECTION 10 : MODALES
     ========================================================== */
    window.ouvrirModalPlanCours = function() {
        var modal = document.getElementById('modalPlanCours');
        if (!modal) return;
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        modal.setAttribute('aria-hidden', 'false');
        var focusable = modal.querySelector('button, [tabindex]');
        if (focusable) focusable.focus();
    };

    window.fermerModalPlanCours = function() {
        var modal = document.getElementById('modalPlanCours');
        if (!modal) return;
        modal.style.display = 'none';
        document.body.style.overflow = '';
        modal.setAttribute('aria-hidden', 'true');
    };

    window.ouvrirModalVueChapitre = function() {
        var modal = document.getElementById('modalVueChapitre');
        if (!modal) return;
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        modal.setAttribute('aria-hidden', 'false');
        var focusable = modal.querySelector('button, [tabindex]');
        if (focusable) focusable.focus();
    };

    window.fermerModalVueChapitre = function() {
        var modal = document.getElementById('modalVueChapitre');
        if (!modal) return;
        modal.style.display = 'none';
        document.body.style.overflow = '';
        modal.setAttribute('aria-hidden', 'true');
    };

    function initModalCloseOnEscape() {
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                var modalPlan = document.getElementById('modalPlanCours');
                var modalChapitre = document.getElementById('modalVueChapitre');

                if (modalPlan && modalPlan.style.display === 'flex') {
                    fermerModalPlanCours();
                }
                if (modalChapitre && modalChapitre.style.display === 'flex') {
                    fermerModalVueChapitre();
                }
            }
        });
    }

    function initModalCloseOnOverlayClick() {
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('modal-overlay')) {
                if (e.target.id === 'modalPlanCours') {
                    fermerModalPlanCours();
                }
                if (e.target.id === 'modalVueChapitre') {
                    fermerModalVueChapitre();
                }
            }
        });
    }

    /* ==========================================================
     SECTION 11 : RACCOURCIS CLAVIER
     ========================================================== */
    function initKeyboardShortcuts() {
        document.addEventListener('keydown', function(e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.isContentEditable) {
                return;
            }

            var modalPlan = document.getElementById('modalPlanCours');
            var modalChapitre = document.getElementById('modalVueChapitre');
            var isModalOpen = (modalPlan && modalPlan.style.display === 'flex') ||
                              (modalChapitre && modalChapitre.style.display === 'flex');

            if (isModalOpen) return;

            switch (e.key) {
                case 'ArrowLeft':
                    if (e.altKey) {
                        e.preventDefault();
                        var btnPrecedent = document.querySelector('.btn-nav-lecon.precedent');
                        if (btnPrecedent && btnPrecedent.getAttribute('href')) {
                            window.location.href = btnPrecedent.getAttribute('href');
                        }
                    }
                    break;

                case 'ArrowRight':
                    if (e.altKey) {
                        e.preventDefault();
                        var btnSuivant = document.querySelector('.btn-nav-lecon.valider');
                        if (btnSuivant) {
                            if (btnSuivant.tagName === 'BUTTON') {
                                btnSuivant.click();
                            } else if (btnSuivant.getAttribute('href')) {
                                window.location.href = btnSuivant.getAttribute('href');
                            }
                        }
                    }
                    break;

                case 'Enter':
                    if (e.ctrlKey && !estTerminee) {
                        e.preventDefault();
                        window.validerLecon();
                    }
                    break;

                case 'p':
                    if (e.ctrlKey && e.shiftKey) {
                        e.preventDefault();
                        window.ouvrirModalPlanCours();
                    }
                    break;

                case 'c':
                    if (e.ctrlKey && e.shiftKey) {
                        e.preventDefault();
                        var copyBtn = document.querySelector('.code-copy-btn');
                        if (copyBtn) copyBtn.click();
                    }
                    break;

                case 'e':
                    if (e.ctrlKey && !e.shiftKey) {
                        e.preventDefault();
                        var expandBtn = document.querySelector('.code-expand-btn');
                        if (expandBtn) expandBtn.click();
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
            if (table.closest('.table-responsive-wrapper')) return;

            var wrapper = document.createElement('div');
            wrapper.className = 'table-responsive-wrapper';
            wrapper.style.cssText = 'overflow-x:auto;-webkit-overflow-scrolling:touch;margin:20px 0;';
            table.parentNode.insertBefore(wrapper, table);
            wrapper.appendChild(table);
        });
    }

    /* ==========================================================
     SECTION 13 : LIENS EXTERNES
     ========================================================== */
    function initExternalLinks() {
        var externalLinks = document.querySelectorAll('.lecon-texte a[href^="http"]');
        externalLinks.forEach(function(link) {
            if (!link.hasAttribute('target')) {
                link.setAttribute('target', '_blank');
                link.setAttribute('rel', 'noopener noreferrer');
            }
            link.setAttribute('title', 'Ouvrir dans un nouvel onglet : ' + link.getAttribute('href'));
        });
    }

    /* ==========================================================
     SECTION 14 : ZOOM DES IMAGES
     ========================================================== */
    function initImageZoom() {
        var images = document.querySelectorAll('.lecon-texte img');
        if (images.length === 0) return;

        images.forEach(function(img) {
            img.style.cursor = 'zoom-in';
            img.setAttribute('title', 'Cliquer pour agrandir');
            img.setAttribute('tabindex', '0');
            img.setAttribute('role', 'button');

            img.addEventListener('click', function() {
                openImageModal(this.getAttribute('src'), this.getAttribute('alt'));
            });

            img.addEventListener('keydown', function(e) {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    openImageModal(this.getAttribute('src'), this.getAttribute('alt'));
                }
            });
        });
    }

    function openImageModal(src, alt) {
        var existingModal = document.querySelector('.image-modal-overlay');
        if (existingModal) existingModal.remove();

        var overlay = document.createElement('div');
        overlay.className = 'image-modal-overlay';
        overlay.setAttribute('role', 'dialog');
        overlay.setAttribute('aria-label', 'Image agrandie');
        overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.9);z-index:10002;display:flex;align-items:center;justify-content:center;cursor:zoom-out;animation:clFadeInImg 0.3s ease;';

        var img = document.createElement('img');
        img.src = src;
        img.alt = alt || '';
        img.style.cssText = 'max-width:90%;max-height:90%;border-radius:8px;box-shadow:0 20px 60px rgba(0,0,0,0.5);';

        var closeBtn = document.createElement('button');
        closeBtn.innerHTML = '<i class="fas fa-times"></i>';
        closeBtn.setAttribute('aria-label', 'Fermer');
        closeBtn.style.cssText = 'position:absolute;top:20px;right:20px;background:rgba(255,255,255,0.2);border:none;color:white;width:44px;height:44px;border-radius:50%;font-size:20px;cursor:pointer;transition:all 0.3s ease;display:flex;align-items:center;justify-content:center;';
        closeBtn.addEventListener('mouseenter', function() { this.style.background = 'rgba(255,255,255,0.4)'; this.style.transform = 'scale(1.1)'; });
        closeBtn.addEventListener('mouseleave', function() { this.style.background = 'rgba(255,255,255,0.2)'; this.style.transform = 'scale(1)'; });

        overlay.appendChild(img);
        overlay.appendChild(closeBtn);
        document.body.appendChild(overlay);

        function fermerImageModal() {
            overlay.style.opacity = '0';
            overlay.style.transition = 'opacity 0.2s ease';
            setTimeout(function() { overlay.remove(); }, 200);
            document.removeEventListener('keydown', escHandler);
        }

        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) fermerImageModal();
        });

        closeBtn.addEventListener('click', fermerImageModal);

        var escHandler = function(e) {
            if (e.key === 'Escape') fermerImageModal();
        };
        document.addEventListener('keydown', escHandler);

        var styleImgModal = document.createElement('style');
        styleImgModal.textContent = '@keyframes clFadeInImg { from { opacity:0; } to { opacity:1; } }';
        document.head.appendChild(styleImgModal);
    }

    /* ==========================================================
     SECTION 15 : ANIMATION DES BLOCS SPECIAUX
     ========================================================== */
    function initSpecialBlocksAnimation() {
        var specialBlocks = document.querySelectorAll(
            '.note-info, .note-success, .note-warning, .note-danger, .note-tip, .definition, .exemple, .resume, .exercice'
        );
        if (specialBlocks.length === 0) return;

        if (!('IntersectionObserver' in window)) return;

        var observer = new IntersectionObserver(function(entries) {
            entries.forEach(function(entry) {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateX(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, { rootMargin: '0px 0px -30px 0px', threshold: 0.1 });

        specialBlocks.forEach(function(block) {
            block.style.opacity = '0';
            block.style.transform = 'translateX(-15px)';
            block.style.transition = 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
            observer.observe(block);
        });
    }

    /* ==========================================================
     SECTION 16 : DEFILEMENT FLUIDE POUR LES ANCRES
     ========================================================== */
    function initSmoothScrollAnchors() {
        document.querySelectorAll('.lecon-texte a[href^="#"]').forEach(function(anchor) {
            anchor.addEventListener('click', function(e) {
                var targetId = this.getAttribute('href').substring(1);
                if (!targetId) return;

                var target = document.getElementById(targetId);
                if (target) {
                    e.preventDefault();
                    var navbarHeight = document.querySelector('.navbar') ? document.querySelector('.navbar').offsetHeight : 65;
                    var targetPosition = target.getBoundingClientRect().top + window.pageYOffset - navbarHeight - 20;
                    window.scrollTo({ top: targetPosition, behavior: 'smooth' });
                    history.pushState(null, null, '#' + targetId);
                }
            });
        });
    }

    /* ==========================================================
     SECTION 17 : PERFORMANCE
     ========================================================== */
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            document.body.classList.add('page-hidden');
        } else {
            document.body.classList.remove('page-hidden');
        }
    });

    var performanceStyle = document.createElement('style');
    performanceStyle.textContent = '.page-hidden .lecon-texte * { animation: none !important; transition: none !important; }';
    document.head.appendChild(performanceStyle);

    /* ==========================================================
     SECTION 18 : EXPOSITION GLOBALE
     ========================================================== */
    window.copierCode = function() {
        var copyBtn = document.querySelector('.code-copy-btn');
        if (copyBtn) copyBtn.click();
    };

    window.toggleCodeExpand = function() {
        var expandBtn = document.querySelector('.code-expand-btn');
        if (expandBtn) expandBtn.click();
    };

})();