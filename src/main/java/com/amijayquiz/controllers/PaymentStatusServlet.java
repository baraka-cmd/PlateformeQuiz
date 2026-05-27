package com.amijayquiz.controllers;

import com.amijayquiz.models.PaymentStatus;
import com.amijayquiz.services.PaymentService;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/joueur/paiement/statut")
public class PaymentStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String reference = request.getParameter("reference");
        
        if (reference == null || reference.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        JsonObject jsonResponse = new JsonObject();
        
        try {
            PaymentStatus status = PaymentService.checkPaymentStatus(reference);
            
            jsonResponse.addProperty("reference", reference);
            jsonResponse.addProperty("status", status.getCode());
            jsonResponse.addProperty("statusLabel", status.getLabel());
            jsonResponse.addProperty("isSuccess", status.isSuccess());
            jsonResponse.addProperty("isPending", status.isPending());
            
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }
}