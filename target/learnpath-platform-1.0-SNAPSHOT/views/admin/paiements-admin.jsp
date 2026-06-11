<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     FICHIER : views/admin/paiements-admin.jsp
     RÔLE    : Page d'administration des paiements manuels
              - Tableau des demandes en attente
              - Boutons Valider / Refuser
              - Historique des dernieres validations
              - Statistiques
     ============================================================ --%>

<%
    // Verifications de securite faites par le servlet
    List<Map<String, Object>> demandes = (List<Map<String, Object>>) request.getAttribute("demandes");
    List<Map<String, Object>> historique = (List<Map<String, Object>>) request.getAttribute("historique");
    Integer nbEnAttente = (Integer) request.getAttribute("nbEnAttente");
    Integer nbValideAujourdhui = (Integer) request.getAttribute("nbValideAujourdhui");

    if (demandes == null) demandes = new java.util.ArrayList<Map<String, Object>>();
    if (historique == null) historique = new java.util.ArrayList<Map<String, Object>>();
    if (nbEnAttente == null) nbEnAttente = 0;
    if (nbValideAujourdhui == null) nbValideAujourdhui = 0;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new java.util.Locale("fr", "FR"));

    request.setAttribute("pageTitle", "Paiements en attente - Administration - LearnPath");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/paiements-admin.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-paiements-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-money-bill-wave"></i><span>Paiements manuels</span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TETE
         ============================================================ --%>
    <div class="admin-paiements-header">
        <div class="admin-header-left">
            <div class="admin-header-icon">
                <i class="fas fa-clipboard-check"></i>
            </div>
            <div class="admin-header-text">
                <h1>Validation des paiements manuels</h1>
                <p>Verifiez chaque demande avant de valider</p>
            </div>
        </div>
        <div class="admin-header-stats">
            <div class="admin-stat">
                <span class="admin-stat-nombre"><%= nbEnAttente %></span>
                <span class="admin-stat-label">En attente</span>
            </div>
            <div class="admin-stat">
                <span class="admin-stat-nombre"><%= nbValideAujourdhui %></span>
                <span class="admin-stat-label">Validés aujourd'hui</span>
            </div>
        </div>
    </div>

    <%-- ============================================================
         MESSAGE FLASH
         ============================================================ --%>
    <%
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) {
            session.removeAttribute("messageSucces");
    %>
        <div class="message-alerte message-succes">
            <i class="fas fa-check-circle"></i>
            <span><%= messageSucces %></span>
        </div>
    <% } %>
    <%
        if (messageErreur != null) {
            session.removeAttribute("messageErreur");
    %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ============================================================
         GUIDE DE VÉRIFICATION
         ============================================================ --%>
    <div class="admin-guide-verification">
        <div class="guide-icon">
            <i class="fas fa-info-circle"></i>
        </div>
        <div class="guide-content">
            <strong>Comment vérifier un paiement ?</strong>
            <p>
                Avant de valider, vérifiez sur votre téléphone que :
                <strong>1)</strong> Le montant (10 000 FC) a bien été reçu,
                <strong>2)</strong> L'ID de transaction correspond au SMS reçu,
                <strong>3)</strong> Le numéro de téléphone correspond à l'expéditeur.
            </p>
        </div>
    </div>

    <%-- ============================================================
         TABLEAU DES DEMANDES EN ATTENTE
         ============================================================ --%>
    <div class="admin-section">
        <div class="section-header">
            <h2><i class="fas fa-clock"></i> Demandes en attente de validation</h2>
            <span class="badge-count"><%= demandes.size() %> demande<%= demandes.size() > 1 ? "s" : "" %></span>
        </div>

        <% if (demandes != null && !demandes.isEmpty()) { %>
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Utilisateur</th>
                            <th>Email</th>
                            <th>ID Transaction</th>
                            <th>Opérateur</th>
                            <th>Téléphone</th>
                            <th>Date demande</th>
                            <th class="th-action">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (Map<String, Object> demande : demandes) {
                                int id = (int) demande.get("id");
                                String pseudo = (String) demande.get("pseudo");
                                String email = (String) demande.get("email");
                                String idTransaction = (String) demande.get("id_transaction");
                                String operateur = (String) demande.get("operateur");
                                String telephone = (String) demande.get("telephone");
                                Timestamp dateCreation = (Timestamp) demande.get("date_creation");
                                String dateFormatee = dateCreation != null ? sdf.format(dateCreation) : "N/A";

                                if (pseudo == null) pseudo = "N/A";
                                if (email == null) email = "N/A";
                                if (operateur == null) operateur = "N/A";
                                if (telephone == null) telephone = "N/A";
                        %>
                            <tr id="ligneDemande<%= id %>">
                                <td class="td-id">#<%= id %></td>
                                <td>
                                    <div class="cell-utilisateur">
                                        <div class="utilisateur-avatar">
                                            <i class="fas fa-user"></i>
                                        </div>
                                        <span><%= pseudo %></span>
                                    </div>
                                </td>
                                <td class="td-email"><%= email %></td>
                                <td>
                                    <span class="cell-transaction" title="<%= idTransaction %>">
                                        <i class="fas fa-receipt"></i>
                                        <%= idTransaction.length() > 20 ? idTransaction.substring(0, 20) + "..." : idTransaction %>
                                    </span>
                                </td>
                                <td>
                                    <span class="badge-operateur <%= "AIRTEL".equalsIgnoreCase(operateur) ? "badge-airtel" : "badge-orange" %>">
                                        <%= "AIRTEL".equalsIgnoreCase(operateur) ? "Airtel Money" : "Orange Money" %>
                                    </span>
                                </td>
                                <td class="td-telephone"><%= telephone %></td>
                                <td class="td-date"><%= dateFormatee %></td>
                                <td class="td-actions">
                                    <form action="${pageContext.request.contextPath}/admin/paiements" method="POST" class="form-action" onsubmit="return confirmerAction(event, '<%= pseudo %>', 'valider')">
                                        <input type="hidden" name="action" value="valider">
                                        <input type="hidden" name="id" value="<%= id %>">
                                        <button type="submit" class="btn-action-admin btn-valider" title="Valider ce paiement">
                                            <i class="fas fa-check"></i>
                                            <span>Valider</span>
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/admin/paiements" method="POST" class="form-action" onsubmit="return confirmerAction(event, '<%= pseudo %>', 'refuser')">
                                        <input type="hidden" name="action" value="refuser">
                                        <input type="hidden" name="id" value="<%= id %>">
                                        <button type="submit" class="btn-action-admin btn-refuser" title="Refuser ce paiement">
                                            <i class="fas fa-times"></i>
                                            <span>Refuser</span>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="fas fa-inbox"></i>
                </div>
                <h3>Aucune demande en attente</h3>
                <p>Toutes les demandes ont été traitées.</p>
            </div>
        <% } %>
    </div>

    <%-- ============================================================
         HISTORIQUE DES DERNIÈRES VALIDATIONS
         ============================================================ --%>
    <div class="admin-section">
        <div class="section-header">
            <h2><i class="fas fa-history"></i> Historique récent</h2>
        </div>

        <% if (historique != null && !historique.isEmpty()) { %>
            <div class="table-responsive">
                <table class="admin-table admin-table-historique">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Utilisateur</th>
                            <th>ID Transaction</th>
                            <th>Statut</th>
                            <th>Date validation</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (Map<String, Object> hist : historique) {
                                int id = (int) hist.get("id");
                                String pseudo = (String) hist.get("pseudo");
                                String idTransaction = (String) hist.get("id_transaction");
                                String statut = (String) hist.get("statut");
                                Timestamp dateValidation = (Timestamp) hist.get("date_validation");
                                String dateFormatee = dateValidation != null ? sdf.format(dateValidation) : "N/A";

                                if (pseudo == null) pseudo = "N/A";
                                if (statut == null) statut = "N/A";
                        %>
                            <tr>
                                <td class="td-id">#<%= id %></td>
                                <td><%= pseudo %></td>
                                <td>
                                    <span class="cell-transaction" title="<%= idTransaction %>">
                                        <%= idTransaction != null && idTransaction.length() > 20 ? idTransaction.substring(0, 20) + "..." : idTransaction %>
                                    </span>
                                </td>
                                <td>
                                    <span class="badge-statut <%= "VALIDE".equals(statut) ? "badge-valide" : "badge-refuse" %>">
                                        <i class="fas <%= "VALIDE".equals(statut) ? "fa-check-circle" : "fa-times-circle" %>"></i>
                                        <%= "VALIDE".equals(statut) ? "Validé" : "Refusé" %>
                                    </span>
                                </td>
                                <td class="td-date"><%= dateFormatee %></td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="empty-state">
                <p>Aucun historique pour le moment.</p>
            </div>
        <% } %>
    </div>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard admin
        </a>
    </div>

</div>

<%-- ============================================================
     SCRIPT DE CONFIRMATION
     ============================================================ --%>
<script>
    function confirmerAction(event, pseudo, action) {
        var message = '';
        if (action === 'valider') {
            message = 'Valider le paiement de ' + pseudo + ' ?\n\n' +
                      'Assurez-vous d\'avoir bien reçu 10 000 FC sur votre téléphone.\n' +
                      'Le compte premium sera activé immédiatement.';
        } else {
            message = 'Refuser le paiement de ' + pseudo + ' ?\n\n' +
                      'Cette action est irréversible.';
        }
        return confirm(message);
    }
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/paiements-admin.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>