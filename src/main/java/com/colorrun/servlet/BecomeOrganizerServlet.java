package com.colorrun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.OrganizerRequestDao;
import com.colorrun.dao.UserDao;
import com.colorrun.model.OrganizerRequest;
import com.colorrun.model.User;

public class BecomeOrganizerServlet extends HttpServlet {

    private TemplateEngine engine;
    private OrganizerRequestDao requestDao;
    private UserDao userDao;

    @Override
    public void init() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode(TemplateMode.HTML);
        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        requestDao = new OrganizerRequestDao();
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // Vérifier si l'utilisateur est connecté
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        WebContext context = new WebContext(req, resp, getServletContext());
        
        // Ajouter l'utilisateur au contexte pour la navbar
        context.setVariable("user", user);
        
        engine.process("become-organizer", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String motivation = req.getParameter("motivation");
        WebContext context = new WebContext(req, resp, getServletContext());
        
        // Ajouter l'utilisateur au contexte pour la navbar
        context.setVariable("user", user);

        System.out.println("BecomeOrganizerServlet.doPost() - Début");
        System.out.println("BecomeOrganizerServlet.doPost() - User ID: " + user.getId());
        System.out.println("BecomeOrganizerServlet.doPost() - User email: " + user.getEmail());

        // Vérifier que l'utilisateur existe bien en base
        User userInDb = userDao.findByEmail(user.getEmail());
        if (userInDb == null) {
            System.out.println("BecomeOrganizerServlet.doPost() - ERREUR: Utilisateur non trouvé en base avec l'email: " + user.getEmail());
            context.setVariable("errorMessage", "Erreur: Utilisateur non trouvé. Veuillez vous reconnecter.");
            engine.process("become-organizer", context, resp.getWriter());
            return;
        }
        System.out.println("BecomeOrganizerServlet.doPost() - Utilisateur trouvé en base: " + userInDb.getEmail());
        System.out.println("BecomeOrganizerServlet.doPost() - ID de session: " + user.getId() + ", ID en base: " + userInDb.getId());

        if (motivation == null || motivation.trim().isEmpty()) {
            context.setVariable("errorMessage", "Veuillez renseigner vos motivations.");
            engine.process("become-organizer", context, resp.getWriter());
            return;
        }

        OrganizerRequest newRequest = new OrganizerRequest();
        newRequest.setUserId(userInDb.getId());
        newRequest.setMotivation(motivation);
        newRequest.setStatus("PENDING");

        System.out.println("BecomeOrganizerServlet.doPost() - Création de la demande pour l'utilisateur ID: " + userInDb.getId());
        requestDao.create(newRequest);
        System.out.println("BecomeOrganizerServlet.doPost() - Demande créée avec succès");

        context.setVariable("successMessage", "Votre demande a bien été envoyée. Elle sera examinée prochainement.");
        engine.process("become-organizer", context, resp.getWriter());
    }
} 