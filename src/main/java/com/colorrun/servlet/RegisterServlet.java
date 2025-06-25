package com.colorrun.servlet;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.UserDao;
import com.colorrun.model.User;
import com.colorrun.service.EmailService;

public class RegisterServlet extends HttpServlet {
    private UserDao userDao;
    private EmailService emailService;

    @Override
    public void init() {
        userDao = new UserDao();
        emailService = new EmailService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
        engine.process("register", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        String firstName = req.getParameter("firstName");
        String lastName  = req.getParameter("lastName");
        String email     = req.getParameter("email");
        String password  = req.getParameter("password");
        String role      = "PARTICIPANT";

        // Vérifier si l'email existe déjà
        if (userDao.findByEmail(email) != null) {
            req.setAttribute("error", "Email déjà utilisé.");
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);
            WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
            ctx.setVariable("error", req.getAttribute("error"));
            engine.process("register", ctx, resp.getWriter());
            return;
        }

        // Créer l'utilisateur avec un token de vérification
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setVerified(false); // Compte non vérifié par défaut
        
        // Générer un token de vérification
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        
        // Sauvegarder l'utilisateur
        userDao.save(user);
        
        // Envoyer l'email de vérification
        String verificationLink = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + 
                                 req.getContextPath() + "/verify-email?token=" + verificationToken;
        
        String subject = "Vérification de votre compte - ColorRun";
        String message = "Bonjour " + firstName + ",\n\n" +
                        "Merci de vous être inscrit sur ColorRun !\n\n" +
                        "Pour activer votre compte, veuillez cliquer sur le lien suivant :\n" +
                        verificationLink + "\n\n" +
                        "Ce lien expirera dans 24 heures.\n\n" +
                        "Si vous n'avez pas créé de compte sur ColorRun, ignorez cet email.\n\n" +
                        "Cordialement,\nL'équipe ColorRun";

        try {
            emailService.sendEmailTest(email, subject, message);
            
            // Afficher la page de confirmation
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);
            
            WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
            ctx.setVariable("email", email);
            ctx.setVariable("success", "Inscription réussie ! Un email de vérification a été envoyé à votre adresse.");
            engine.process("register", ctx, resp.getWriter());
            
        } catch (Exception e) {
            req.setAttribute("error", "Erreur lors de l'envoi de l'email de vérification. Veuillez réessayer.");
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);
            WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
            ctx.setVariable("error", req.getAttribute("error"));
            engine.process("register", ctx, resp.getWriter());
        }
    }
}