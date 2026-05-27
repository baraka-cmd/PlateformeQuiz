<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>
<%@ page import="com.amijayquiz.models.ProgressionModule" %>
<%@ page import="com.amijayquiz.models.ExamenModule" %>

<%-- ============================================================
     Composant : cours-navigation.jsp
     Rôle    : Barre latérale de navigation pour les cours
              - Modules : ouvrir/fermer avec flèche ▼/▲
              - Chapitres : ouvrir/fermer avec flèche ▼/▲
              - Un seul module ouvert à la fois
              - Modules verrouillés : scrollable, grisés, pas d'accès
              - Modules ouverts : chapitres visibles, leçons cliquables
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    List<Map<String, Object>> modules = (List<Map<String, Object>>) request.getAttribute("modules");
    Integer leconActiveId = (Integer) request.getAttribute("leconActiveId");
    Integer chapitreActiveId = (Integer) request.getAttribute("chapitreActiveId");
    Integer moduleActiveNumero = (Integer) request.getAttribute("moduleActiveNumero");

    if (moduleActiveNumero == null) moduleActiveNumero = 1;
%>

<aside class="cours-sidebar" id="coursSidebar">

    <%-- ===== LISTE DES MODULES ===== --%>
    <nav class="sidebar-nav" id="sidebarNav">
        
        <% if (modules != null && !modules.isEmpty()) {
            for (Map<String, Object> moduleData : modules) {
                int numero = (Integer) moduleData.get("numero");
                boolean debloque = (Boolean) moduleData.get("debloque");
                boolean reussi = (Boolean) moduleData.get("reussi");
                ProgressionModule progression = (ProgressionModule) moduleData.get("progression");
                ExamenModule examen = (ExamenModule) moduleData.get("examen");
                List<Map<String, Object>> chapitresData = (List<Map<String, Object>>) moduleData.get("chapitres");
                int nbLecons = (Integer) moduleData.get("nbLecons");

                boolean isModuleActive = (moduleActiveNumero != null && moduleActiveNumero == numero);
                boolean isModuleOpen = isModuleActive;
                boolean isLocked = !debloque && !reussi;
        %>
            <%-- ===== MODULE ===== --%>
            <div class="sidebar-module <%= isLocked ? "verrouille" : "" %> <%= isModuleOpen ? "open" : "" %>" 
                 data-module="<%= numero %>">

                <%-- En-tête du module --%>
                <button type="button" 
                        class="sidebar-module-header" 
                        onclick="toggleModule(<%= numero %>)">
                    
                    <span class="module-indicator" style="background: <%= isLocked ? "#ccc" : (reussi ? "#27ae60" : "#667eea") %>;"></span>
                    
                    <span class="module-label">Module <%= numero %></span>
                    
                    <span class="module-lecons-count"><%= nbLecons %></span>
                    
                    <i class="fas fa-chevron-<%= isModuleOpen ? "up" : "down" %> module-arrow" id="moduleArrow<%= numero %>"></i>
                </button>

                <%-- Contenu du module --%>
                <div class="sidebar-module-content" id="moduleContent<%= numero %>" 
                     style="display: <%= isModuleOpen ? "block" : "none" %>;">

                    <%-- Examen du module --%>
                    <% if (reussi && progression != null) { %>
                        <div class="sidebar-examen reussi">
                            <i class="fas fa-trophy"></i>
                            <span>Réussi - <%= progression.getPourcentageFormate() %></span>
                        </div>
                    <% } else if (debloque && examen != null) { %>
                        <a href="${pageContext.request.contextPath}/joueur/cours/examen-module?coursId=<%= cours.getId() %>&module=<%= numero %>" 
                           class="sidebar-examen accessible">
                            <i class="fas fa-clipboard-check"></i>
                            <span>Examen du module</span>
                        </a>
                    <% } %>

                    <%-- Chapitres --%>
                    <% if (chapitresData != null && !chapitresData.isEmpty()) {
                        for (Map<String, Object> chapData : chapitresData) {
                            Chapitre chapitre = (Chapitre) chapData.get("chapitre");
                            List<Map<String, Object>> leconsData = (List<Map<String, Object>>) chapData.get("lecons");
                            int nbLeconsChap = (Integer) chapData.get("nbLecons");
                            boolean isChapitreActive = (chapitreActiveId != null && chapitreActiveId == chapitre.getId());
                    %>
                        <%-- ===== CHAPITRE ===== --%>
                        <div class="sidebar-chapitre">
                            
                            <button type="button" 
                                    class="sidebar-chapitre-header <%= isChapitreActive ? "active" : "" %>"
                                    onclick="toggleChapitre(this, event)">
                                
                                <span class="chapitre-dot" style="background: <%= isLocked ? "#ddd" : "#667eea" %>;"></span>
                                
                                <span class="chapitre-label"><%= chapitre.getTitre() %></span>
                                
                                <span class="chapitre-lecons-count"><%= nbLeconsChap %></span>
                                
                                <i class="fas fa-chevron-<%= isChapitreActive || isModuleActive ? "up" : "down" %> chapitre-arrow"></i>
                            </button>

                            <%-- Leçons --%>
                            <ul class="sidebar-lecons" style="display: <%= isChapitreActive || isModuleActive ? "block" : "none" %>;">
                                <% if (leconsData != null && !leconsData.isEmpty()) {
                                    for (Map<String, Object> leconData : leconsData) {
                                        Lecon lecon = (Lecon) leconData.get("lecon");
                                        boolean leconAccessible = (Boolean) leconData.get("accessible");
                                        boolean isLeconActive = (leconActiveId != null && leconActiveId == lecon.getId());
                                %>
                                    <li class="sidebar-lecon <%= isLeconActive ? "active" : "" %>">
                                        <% if (!isLocked && leconAccessible) { %>
                                            <a href="${pageContext.request.contextPath}/joueur/cours/lecon?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>&leconId=<%= lecon.getId() %>" 
                                               class="sidebar-lecon-link">
                                                <span class="lecon-dot <%= isLeconActive ? "active" : "" %>"></span>
                                                <span class="lecon-label"><%= lecon.getTitre() %></span>
                                            </a>
                                        <% } else { %>
                                            <span class="sidebar-lecon-link disabled">
                                                <span class="lecon-dot locked"></span>
                                                <span class="lecon-label"><%= lecon.getTitre() %></span>
                                                <i class="fas fa-lock lecon-lock-icon"></i>
                                            </span>
                                        <% } %>
                                    </li>
                                <%      }
                                } %>
                            </ul>
                        </div>
                    <%      }
                    } %>
                </div>
            </div>
        <%  }
        } %>
    </nav>

</aside>

<%-- ===== BOUTON MOBILE ===== --%>
<button type="button" class="sidebar-mobile-toggle" id="sidebarMobileToggle" onclick="toggleMobileSidebar()">
    <i class="fas fa-bars"></i>
    <span>Navigation</span>
</button>

<div class="sidebar-overlay" id="sidebarOverlay" onclick="toggleMobileSidebar()"></div>