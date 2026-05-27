<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Payment" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.services.CourseService" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    List<Payment> payments = (List<Payment>) request.getAttribute("payments");
    int totalPayments = (Integer) request.getAttribute("totalPayments");
    double totalAmount = (Double) request.getAttribute("totalAmount");
    int successfulCount = (Integer) request.getAttribute("successfulCount");
    
    if (payments == null) payments = new java.util.ArrayList<>();
    if (totalPayments == null) totalPayments = 0;
    if (totalAmount == null) totalAmount = 0.0;
    if (successfulCount == null) successfulCount = 0;
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    String extraHead = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/payment-history.css\">";
    request.setAttribute("extraHead", extraHead);
%>

<%@ include file="../../header.jsp" %>

<div class="history-container">
    
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-history"></i><span>Historique des paiements</span>
        </span>
    </nav>
    
    <!-- En-tête -->
    <div class="history-header">
        <h1><i class="fas fa-credit-card"></i> Historique des paiements</h1>
        <p>Consultez l'ensemble de vos transactions et achats de cours</p>
    </div>
    
    <!-- Statistiques -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon">
                <i class="fas fa-receipt"></i>
            </div>
            <div class="stat-info">
                <span class="stat-value"><%= totalPayments %></span>
                <span class="stat-label">Total transactions</span>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon success">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="stat-info">
                <span class="stat-value"><%= successfulCount %></span>
                <span class="stat-label">Paiements réussis</span>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon total">
                <i class="fas fa-dollar-sign"></i>
            </div>
            <div class="stat-info">
                <span class="stat-value"><%= String.format("%.2f", totalAmount) %> USD</span>
                <span class="stat-label">Montant total</span>
            </div>
        </div>
    </div>
    
    <!-- Filtres -->
    <div class="filters-bar">
        <div class="filter-group">
            <button class="filter-btn active" data-filter="all">Tous</button>
            <button class="filter-btn" data-filter="SUCCESS">Réussis</button>
            <button class="filter-btn" data-filter="PENDING">En attente</button>
            <button class="filter-btn" data-filter="FAILED">Échoués</button>
        </div>
        <div class="search-group">
            <i class="fas fa-search"></i>
            <input type="text" id="searchInput" placeholder="Rechercher un cours..." class="search-input">
        </div>
    </div>
    
    <!-- Liste des paiements -->
    <div class="payments-list">
        <% if (payments.isEmpty()) { %>
            <div class="empty-state">
                <i class="fas fa-inbox"></i>
                <h3>Aucun paiement effectué</h3>
                <p>Vous n'avez encore effectué aucun achat de cours.</p>
                <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-browse">
                    <i class="fas fa-search"></i> Découvrir les cours
                </a>
            </div>
        <% } else { 
            for (Payment payment : payments) {
                String courseTitle = CourseService.getCourseTitle(payment.getCourseId());
                String statusClass = "";
                String statusIcon = "";
                String statusLabel = "";
                
                if (payment.isSuccess()) {
                    statusClass = "status-success";
                    statusIcon = "fas fa-check-circle";
                    statusLabel = "Réussi";
                } else if (payment.isPending()) {
                    statusClass = "status-pending";
                    statusIcon = "fas fa-clock";
                    statusLabel = "En attente";
                } else {
                    statusClass = "status-failed";
                    statusIcon = "fas fa-times-circle";
                    statusLabel = "Échoué";
                }
        %>
            <div class="payment-item" data-status="<%= payment.getStatus().getCode() %>" data-title="<%= courseTitle.toLowerCase() %>">
                <div class="payment-icon <%= statusClass %>">
                    <i class="<%= statusIcon %>"></i>
                </div>
                <div class="payment-info">
                    <h3><%= courseTitle %></h3>
                    <div class="payment-meta">
                        <span class="payment-ref"><i class="fas fa-hashtag"></i> <%= payment.getReference() %></span>
                        <span class="payment-date"><i class="far fa-calendar-alt"></i> <%= sdf.format(payment.getCreatedAt()) %></span>
                    </div>
                </div>
                <div class="payment-amount">
                    <span class="amount-value"><%= String.format("%.2f", payment.getAmount()) %> USD</span>
                    <span class="payment-status <%= statusClass %>">
                        <i class="<%= statusIcon %>"></i> <%= statusLabel %>
                    </span>
                </div>
                <div class="payment-action">
                    <% if (payment.isSuccess()) { %>
                        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= payment.getCourseId() %>" class="btn-access">
                            <i class="fas fa-play-circle"></i>
                        </a>
                    <% } else if (payment.isPending()) { %>
                        <a href="${pageContext.request.contextPath}/joueur/paiement/attente?reference=<%= payment.getReference() %>" class="btn-retry">
                            <i class="fas fa-spinner fa-pulse"></i>
                        </a>
                    <% } else { %>
                        <a href="${pageContext.request.contextPath}/joueur/paiement/checkout?courseId=<%= payment.getCourseId() %>" class="btn-retry">
                            <i class="fas fa-redo-alt"></i>
                        </a>
                    <% } %>
                </div>
            </div>
        <%      }
        } %>
    </div>
    
    <!-- Pagination -->
    <% if (payments.size() >= 10) { %>
        <div class="pagination">
            <button class="page-btn prev" disabled>
                <i class="fas fa-chevron-left"></i>
            </button>
            <span class="page-current">Page 1</span>
            <button class="page-btn next">
                <i class="fas fa-chevron-right"></i>
            </button>
        </div>
    <% } %>
    
    <!-- Lien support -->
    <div class="support-link">
        <i class="fas fa-headset"></i>
        <span>Une question sur un paiement ? </span>
        <a href="${pageContext.request.contextPath}/contact">Contacter le support</a>
    </div>
</div>

<script src="${pageContext.request.contextPath}/views/assets/js/payment-history.js"></script>

<%@ include file="../../footer.jsp" %>