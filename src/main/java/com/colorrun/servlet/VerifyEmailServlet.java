package com.colorrun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.colorrun.dao.UserDao;
import com.colorrun.model.User;

public class VerifyEmailServlet extends HttpServlet {
    private UserDao userDao;

    @Override
    public void init() {
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        String token = req.getParameter("token");
        System.out.println("=== VERIFY EMAIL DEBUG ===");
        System.out.println("Token reçu: " + token);
        
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Token null ou vide, redirection vers login");
            resp.sendRedirect("login?error=invalid_token");
            return;
        }

        User user = userDao.findByVerificationToken(token);
        System.out.println("Utilisateur trouvé avec le token: " + (user != null ? user.getEmail() : "AUCUN"));
        
        if (user == null) {
            System.out.println("Aucun utilisateur trouvé, redirection vers login avec erreur");
            resp.sendRedirect("login?error=invalid_token");
            return;
        }

        // Vérifier si le compte est déjà vérifié
        if (user.isVerified()) {
            System.out.println("Compte déjà vérifié");
            resp.sendRedirect("login?error=already_verified");
            return;
        }

        // Marquer le compte comme vérifié et supprimer le token
        System.out.println("Vérification du compte pour: " + user.getEmail());
        userDao.verifyUser(user.getId());
        userDao.clearVerificationToken(user.getId());
        
        System.out.println("Compte vérifié avec succès");
        resp.sendRedirect("login?success=email_verified");
    }
} 