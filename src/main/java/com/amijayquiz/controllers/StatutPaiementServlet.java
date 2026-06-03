package com.amijayquiz.controllers;

import com.amijayquiz.dao.PaiementDAO;
import com.amijayquiz.models.Paiement;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "StatutPaiementServlet", urlPatterns = {"/api/paiements/statut"})
public class StatutPaiementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String reference = request.getParameter("reference");

        JsonObject json = new JsonObject();

        if (reference == null || reference.trim().isEmpty()) {
            json.addProperty("statut", "UNKNOWN");
            json.addProperty("message", "Reference manquante");
            response.getWriter().write(json.toString());
            return;
        }

        PaiementDAO paiementDAO = new PaiementDAO();
        Paiement paiement = paiementDAO.trouverParReference(reference);

        if (paiement == null) {
            json.addProperty("statut", "UNKNOWN");
            json.addProperty("message", "Paiement introuvable");
        } else {
            json.addProperty("statut", paiement.getStatut());
            json.addProperty("reference", paiement.getReference());
        }

        response.getWriter().write(json.toString());
    }
}