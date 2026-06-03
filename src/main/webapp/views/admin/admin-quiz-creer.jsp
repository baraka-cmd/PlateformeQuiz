<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Quiz" %>
<%@ page import="com.amijayquiz.models.Question" %>

<%-- ============================================================
     FICHIER : views/admin/admin-quiz-creer.jsp
     RÔLE    : Page de création d'un quiz avec ses questions
              - Étape 1 : Informations du quiz
              - Étape 2 : Liste des questions ajoutées
              - Bouton pour ajouter une question
              - Bouton pour enregistrer (si 10 questions)
     ============================================================ --%>

<%
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    List<Question> questions = (List<Question>) request.getAttribute("questions");
    Integer compteur = (Integer) request.getAttribute("compteur");
    Integer nbRequis = (Integer) request.getAttribute("nbRequis");
    Boolean quizComplet = (Boolean) request.getAttribute("quizComplet");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (quiz == null) quiz = new Quiz();
    if (questions == null) questions = new java.util.ArrayList<Question>();
    if (compteur == null) compteur = 0;
    if (nbRequis == null) nbRequis = 10;
    if (quizComplet == null) quizComplet = false;

    String themeActuel = "";
    String titreActuel = quiz.getTitre();
    if (titreActuel != null && titreActuel.contains(" - Partie")) {
        themeActuel = titreActuel.substring(0, titreActuel.lastIndexOf(" - Partie"));
    }

    request.setAttribute("pageTitle", "Créer un quiz - Administration - AmijayQuiz");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-quiz-creer.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-quiz-creer-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/quiz" class="breadcrumb-link">
            <i class="fas fa-list-check"></i><span>Quiz</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-plus-circle"></i><span>Créer un quiz</span>
        </span>
    </nav>

    <%-- ===== MESSAGES FLASH ===== --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes">
            <i class="fas fa-check-circle"></i>
            <span><%= messageSucces %></span>
        </div>
    <% } %>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== INDICATEUR DE PROGRESSION ===== --%>
    <div class="creation-progress">
        <div class="progress-step <%= quiz.getTitre() != null && !quiz.getTitre().isEmpty() ? "completed" : "active" %>">
            <div class="step-number">1</div>
            <span>Informations du quiz</span>
        </div>
        <div class="progress-line <%= quiz.getTitre() != null && !quiz.getTitre().isEmpty() ? "completed" : "" %>"></div>
        <div class="progress-step <%= compteur >= nbRequis ? "completed" : (quiz.getTitre() != null && !quiz.getTitre().isEmpty() ? "active" : "") %>">
            <div class="step-number">2</div>
            <span>Questions (<%= compteur %>/<%= nbRequis %>)</span>
        </div>
        <div class="progress-line <%= compteur >= nbRequis ? "completed" : "" %>"></div>
        <div class="progress-step <%= quizComplet ? "completed" : "" %>">
            <div class="step-number">3</div>
            <span>Enregistrer</span>
        </div>
    </div>

    <%-- ===== DISPOSITION 2 COLONNES ===== --%>
    <div class="creation-layout">

        <%-- ============================================================
             COLONNE GAUCHE : INFORMATIONS DU QUIZ
             ============================================================ --%>
        <div class="creation-col creation-col-info">
            <div class="creation-card">
                <div class="card-header">
                    <div class="card-header-icon">
                        <i class="fas fa-info-circle"></i>
                    </div>
                    <h2>Informations du quiz</h2>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/admin/quiz/creer" method="POST" id="formInfos">
                        <input type="hidden" name="action" value="enregistrerInfo">

                        <div class="form-group">
                            <label for="theme">
                                <i class="fas fa-tag"></i> Thème <span class="required">*</span>
                            </label>
                            <input type="text" id="theme" name="theme" 
                                   value="<%= themeActuel %>"
                                   placeholder="Ex: Informatique Générale" required>
                            <span class="form-hint">Le titre sera : <strong id="previewTitre"><%= themeActuel.isEmpty() ? "Thème - Partie X" : themeActuel + " - Partie " + quiz.getOrdrePartie() %></strong></span>
                        </div>

                        <div class="form-row">
                            <div class="form-group form-half">
                                <label for="niveau">
                                    <i class="fas fa-signal"></i> Niveau <span class="required">*</span>
                                </label>
                                <select id="niveau" name="niveau" required>
                                    <option value="elementaire" <%= "elementaire".equals(quiz.getNiveau()) ? "selected" : "" %>>Élémentaire</option>
                                    <option value="moyen" <%= "moyen".equals(quiz.getNiveau()) ? "selected" : "" %>>Moyen</option>
                                    <option value="avance" <%= "avance".equals(quiz.getNiveau()) ? "selected" : "" %>>Avancé</option>
                                </select>
                            </div>
                            <div class="form-group form-half">
                                <label for="ordrePartie">
                                    <i class="fas fa-hashtag"></i> Numéro de partie <span class="required">*</span>
                                </label>
                                <input type="number" id="ordrePartie" name="ordrePartie" 
                                       value="<%= quiz.getOrdrePartie() %>" min="1" max="20" required>
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-group form-half">
                                <label for="dureeMaxMinutes">
                                    <i class="far fa-clock"></i> Durée (minutes)
                                </label>
                                <input type="number" id="dureeMaxMinutes" name="dureeMaxMinutes" 
                                       value="<%= quiz.getDureeMaxMinutes() %>" min="1" max="120">
                            </div>
                            <div class="form-group form-half">
                                <label for="description">
                                    <i class="fas fa-align-left"></i> Description
                                </label>
                                <input type="text" id="description" name="description" 
                                       value="<%= quiz.getDescription() != null ? quiz.getDescription() : "" %>"
                                       placeholder="Description (optionnelle)">
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn-action btn-enregistrer-infos">
                                <i class="fas fa-save"></i> Enregistrer les informations
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <%-- ============================================================
             COLONNE DROITE : QUESTIONS
             ============================================================ --%>
        <div class="creation-col creation-col-questions">
            <div class="creation-card">
                <div class="card-header">
                    <div class="card-header-icon">
                        <i class="fas fa-question-circle"></i>
                    </div>
                    <h2>Questions du quiz</h2>
                    <span class="badge-compteur <%= compteur >= nbRequis ? "complete" : "" %>">
                        <%= compteur %>/<%= nbRequis %>
                    </span>
                </div>
                <div class="card-body">

                    <%-- ===== LISTE DES QUESTIONS AJOUTÉES ===== --%>
                    <% if (questions != null && !questions.isEmpty()) { %>
                        <div class="questions-liste">
                            <%
                                String[] lettres = {"A", "B", "C", "D"};
                                for (int i = 0; i < questions.size(); i++) {
                                    Question q = questions.get(i);
                                    String texteQ = q.getTexteQuestion();
                                    if (texteQ != null && texteQ.length() > 70) texteQ = texteQ.substring(0, 70) + "...";
                            %>
                                <div class="question-item">
                                    <div class="question-item-numero"><%= i + 1 %></div>
                                    <div class="question-item-content">
                                        <p class="question-item-texte"><%= texteQ %></p>
                                        <span class="question-item-reponse">
                                            <i class="fas fa-check-circle"></i> Réponse : <strong><%= lettres[q.getBonneReponse() - 1] %></strong>
                                        </span>
                                    </div>
                                    <form action="${pageContext.request.contextPath}/admin/quiz/creer" method="POST" class="question-item-action">
                                        <input type="hidden" name="action" value="supprimerQuestion">
                                        <input type="hidden" name="index" value="<%= i %>">
                                        <button type="submit" class="btn-supprimer-question" 
                                                title="Supprimer cette question"
                                                onclick="return confirm('Supprimer cette question ?')">
                                            <i class="fas fa-trash-alt"></i>
                                        </button>
                                    </form>
                                </div>
                            <%
                                }
                            %>
                        </div>
                    <% } else { %>
                        <div class="questions-vide">
                            <div class="vide-icon">
                                <i class="fas fa-inbox"></i>
                            </div>
                            <h3>Aucune question pour le moment</h3>
                            <p>Ajoutez <%= nbRequis %> questions en cliquant sur le bouton ci-dessous.</p>
                        </div>
                    <% } %>

                    <%-- ===== BOUTONS D'ACTION ===== --%>
                    <div class="questions-actions">
                        <% if (compteur < nbRequis) { %>
                            <a href="${pageContext.request.contextPath}/admin/quiz/creer/ajouter-question" 
                               class="btn-action btn-ajouter-question">
                                <i class="fas fa-plus-circle"></i> 
                                Ajouter une question (<%= compteur %>/<%= nbRequis %>)
                            </a>
                        <% } else { %>
                            <div class="questions-complete-message">
                                <i class="fas fa-check-circle"></i>
                                <span>Vous avez ajouté les <%= nbRequis %> questions requises !</span>
                            </div>
                        <% } %>

                        <form action="${pageContext.request.contextPath}/admin/quiz/creer" method="POST" class="form-enregistrer">
                            <input type="hidden" name="action" value="enregistrerQuiz">
                            <button type="submit" class="btn-action btn-enregistrer-quiz <%= quizComplet ? "" : "disabled" %>" 
                                    <%= quizComplet ? "" : "disabled" %>
                                    title="<%= quizComplet ? "Enregistrer le quiz" : "Ajoutez " + nbRequis + " questions avant d'enregistrer" %>">
                                <i class="fas fa-check-circle"></i> Enregistrer le quiz
                            </button>
                        </form>

                        <form action="${pageContext.request.contextPath}/admin/quiz/creer" method="POST" class="form-reinitialiser">
                            <input type="hidden" name="action" value="reinitialiser">
                            <button type="submit" class="btn-action btn-reinitialiser" 
                                    onclick="return confirm('Réinitialiser ? Toutes les données seront perdues.')">
                                <i class="fas fa-redo"></i> Réinitialiser
                            </button>
                        </form>
                    </div>

                </div>
            </div>
        </div>

    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/quiz" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour à la liste des quiz
        </a>
    </div>

</div>

<script>
    // Mise à jour en temps réel de l'aperçu du titre
    var themeInput = document.getElementById('theme');
    var ordreInput = document.getElementById('ordrePartie');
    var preview = document.getElementById('previewTitre');

    function updatePreview() {
        var theme = themeInput.value.trim() || 'Thème';
        var ordre = ordreInput.value || 'X';
        preview.textContent = theme + ' - Partie ' + ordre;
    }

    if (themeInput && ordreInput && preview) {
        themeInput.addEventListener('input', updatePreview);
        ordreInput.addEventListener('input', updatePreview);
    }
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-quiz-creer.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>