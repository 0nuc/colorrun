package com.colorrun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.UserDao;
import com.colorrun.model.User;

public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        Context ctx = new Context(req.getLocale());
        HttpSession session = req.getSession(false);
        if (session != null) {
            ctx.setVariable("user", session.getAttribute("user"));
        }
        engine.process("login", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        
        System.out.println("Tentative de connexion pour l'email: " + email);
        
        User user = userDao.findByEmail(email);
        System.out.println("Utilisateur trouvé: " + (user != null));
        
        if (user != null) {
            System.out.println("Utilisateur ID: " + user.getId());
            System.out.println("Mot de passe fourni: " + password);
            System.out.println("Mot de passe en base: " + user.getPassword());
            System.out.println("Rôle de l'utilisateur: " + user.getRole());
            System.out.println("Compte vérifié: " + user.isVerified());
        }

        if (user != null && password.equals(user.getPassword())) {
            // Vérifier si le compte est vérifié
            if (!user.isVerified()) {
                System.out.println("Compte non vérifié pour: " + email);
                req.setAttribute("error", "Votre compte n'est pas encore vérifié. Veuillez vérifier votre email et cliquer sur le lien de vérification.");
                ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
                resolver.setPrefix("/WEB-INF/views/");
                resolver.setSuffix(".html");
                resolver.setTemplateMode("HTML");
                resolver.setCharacterEncoding("UTF-8");
                TemplateEngine engine = new TemplateEngine();
                engine.setTemplateResolver(resolver);
                Context ctx = new Context(req.getLocale());
                ctx.setVariable("error", req.getAttribute("error"));
                HttpSession session = req.getSession(false);
                if (session != null) {
                    ctx.setVariable("user", session.getAttribute("user"));
                }
                engine.process("login", ctx, resp.getWriter());
                return;
            }
            
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            System.out.println("Connexion réussie pour: " + email);
            resp.sendRedirect(req.getContextPath() + "/courses");
        } else {
            System.out.println("Échec de la connexion pour: " + email);
            req.setAttribute("error", "Email ou mot de passe incorrect.");
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);
            Context ctx = new Context(req.getLocale());
            ctx.setVariable("error", req.getAttribute("error"));
            HttpSession session = req.getSession(false);
            if (session != null) {
                ctx.setVariable("user", session.getAttribute("user"));
            }
            engine.process("login", ctx, resp.getWriter());
        }
    }
}