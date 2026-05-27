package com.amijayquiz.controllers;

import com.amijayquiz.dao.PaymentDAO;
import com.amijayquiz.models.Payment;
import com.amijayquiz.models.PaymentStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/joueur/paiement/retour")
public class PaymentCallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        String reference = request.getParameter("reference");
        
        if (reference == null || reference.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }
        
        Payment payment = PaymentDAO.findByReference(reference);
        
        if (payment == null) {
            session.setAttribute("messageErreur", "Paiement non trouvé");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }
        
        if (payment.isSuccess()) {
            session.setAttribute("messageSucces", "Paiement réussi ! Accès au cours débloqué.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + payment.getCourseId());
        } else if (payment.isPending()) {
            request.setAttribute("reference", reference);
            request.setAttribute("courseId", payment.getCourseId());
            request.getRequestDispatcher("/views/joueur/paiement/payment-processing.jsp").forward(request, response);
        } else {
            session.setAttribute("messageErreur", "Le paiement a échoué. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + payment.getCourseId());
        }
    }
}