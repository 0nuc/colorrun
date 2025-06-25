package com.colorrun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.UserDao;
import com.colorrun.model.User;

public class ResetPasswordServlet extends HttpServlet {
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
        System.out.println("=== RESET PASSWORD DEBUG ===");
        System.out.println("Token reçu: " + token);
        
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Token null ou vide, redirection vers login");
            resp.sendRedirect("login");
            return;
        }

        User user = userDao.findByVerificationToken(token);
        System.out.println("Utilisateur trouvé avec le token: " + (user != null ? user.getEmail() : "AUCUN"));
        
        if (user == null) {
            System.out.println("Aucun utilisateur trouvé, redirection vers login avec erreur");
            resp.sendRedirect("login?error=invalid_token");
            return;
        }

        System.out.println("Affichage de la page reset-password");
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
        ctx.setVariable("token", token);
        engine.process("reset-password", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        String token = req.getParameter("token");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
        ctx.setVariable("token", token);

        if (token == null || token.trim().isEmpty()) {
            ctx.setVariable("error", "Token invalide.");
            engine.process("reset-password", ctx, resp.getWriter());
            return;
        }

        User user = userDao.findByVerificationToken(token);
        if (user == null) {
            ctx.setVariable("error", "Token invalide ou expiré.");
            engine.process("reset-password", ctx, resp.getWriter());
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            ctx.setVariable("error", "Veuillez entrer un mot de passe.");
            engine.process("reset-password", ctx, resp.getWriter());
            return;
        }

        if (password.length() < 6) {
            ctx.setVariable("error", "Le mot de passe doit contenir au moins 6 caractères.");
            engine.process("reset-password", ctx, resp.getWriter());
            return;
        }

        if (!password.equals(confirmPassword)) {
            ctx.setVariable("error", "Les mots de passe ne correspondent pas.");
            engine.process("reset-password", ctx, resp.getWriter());
            return;
        }

        // Mettre à jour le mot de passe et supprimer le token
        System.out.println("=== RESET PASSWORD - MISE À JOUR ===");
        System.out.println("Utilisateur: " + user.getEmail() + " (ID: " + user.getId() + ")");
        System.out.println("Nouveau mot de passe: " + password);
        
        userDao.updatePassword(user.getId(), password);
        userDao.clearVerificationToken(user.getId());
        
        System.out.println("Mot de passe mis à jour et token supprimé avec succès");
        resp.sendRedirect("login?success=password_reset");
    }
}