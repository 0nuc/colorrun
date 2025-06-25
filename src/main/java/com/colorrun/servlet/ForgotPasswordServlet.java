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

public class ForgotPasswordServlet extends HttpServlet {
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
        engine.process("forgot-password", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        String email = req.getParameter("email");
        
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());

        if (email == null || email.trim().isEmpty()) {
            ctx.setVariable("error", "Veuillez entrer votre adresse email.");
            engine.process("forgot-password", ctx, resp.getWriter());
            return;
        }

        User user = userDao.findByEmail(email);
        if (user == null) {
            // Pour des raisons de sécurité, on ne révèle pas si l'email existe ou non
            ctx.setVariable("success", "Si cette adresse email existe dans notre base de données, vous recevrez un lien de réinitialisation.");
            engine.process("forgot-password", ctx, resp.getWriter());
            return;
        }

        // Générer un token unique
        String resetToken = UUID.randomUUID().toString();
        user.setVerificationToken(resetToken);
        userDao.updateUserWithToken(user);

        // Envoyer l'email
        String resetLink = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + 
                          req.getContextPath() + "/reset-password?token=" + resetToken;
        
        String subject = "Réinitialisation de votre mot de passe - ColorRun";
        String message = "Bonjour " + user.getFirstName() + ",\n\n" +
                        "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                        "Cliquez sur le lien suivant pour créer un nouveau mot de passe :\n" +
                        resetLink + "\n\n" +
                        "Ce lien expirera dans 24 heures.\n\n" +
                        "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                        "Cordialement,\nL'équipe ColorRun";

                        try {
                          emailService.sendEmailTest(user.getEmail(), subject, message);
                          ctx.setVariable("success", "Un email de réinitialisation a été envoyé à votre adresse email.");
                      } catch (Exception e) {
                          ctx.setVariable("error", "Erreur lors de l'envoi de l'email. Veuillez réessayer.");
                      }

        engine.process("forgot-password", ctx, resp.getWriter());
    }
}