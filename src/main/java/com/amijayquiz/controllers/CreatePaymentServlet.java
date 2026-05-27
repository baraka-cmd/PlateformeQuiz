package com.amijayquiz.controllers;

import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.models.Payment;
import com.amijayquiz.services.PaymentService;
import com.amijayquiz.services.CourseService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/joueur/paiement/creer")
public class CreatePaymentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Vérifier la session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        int userId = utilisateur.getId();
        
        // Récupérer les paramètres
        String courseIdParam = request.getParameter("courseId");
        String phoneNumber = request.getParameter("phoneNumber");
        
        if (courseIdParam == null || phoneNumber == null || phoneNumber.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Numéro de téléphone requis");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }
        
        int courseId;
        try {
            courseId = Integer.parseInt(courseIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }
        
        // Formater le numéro de téléphone (s'assurer qu'il commence par +243)
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+243" + phoneNumber.replaceFirst("^0+", "");
        }
        
        // Récupérer le prix du cours
        double coursePrice = CourseService.getCoursePrice(courseId);
        
        try {
            // Initier le paiement
            Payment payment = PaymentService.initiatePayment(userId, courseId, coursePrice, phoneNumber);
            
            // Rediriger vers la page d'attente
            response.sendRedirect(request.getContextPath() + "/joueur/paiement/attente?reference=" + payment.getReference());
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("messageErreur", "Erreur lors de l'initiation du paiement: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + courseId);
        }
    }
}