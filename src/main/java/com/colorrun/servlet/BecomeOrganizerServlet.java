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
import com.colorrun.model.OrganizerRequest;
import com.colorrun.model.User;

public class BecomeOrganizerServlet extends HttpServlet {

    private TemplateEngine engine;
    private OrganizerRequestDao requestDao;

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
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebContext context = new WebContext(req, resp, getServletContext());
        engine.process("become-organizer", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String motivation = req.getParameter("motivation");
        WebContext context = new WebContext(req, resp, getServletContext());

        if (motivation == null || motivation.trim().isEmpty()) {
            context.setVariable("errorMessage", "Veuillez renseigner vos motivations.");
            engine.process("become-organizer", context, resp.getWriter());
            return;
        }

        OrganizerRequest newRequest = new OrganizerRequest();
        newRequest.setUserId(user.getId());
        newRequest.setMotivation(motivation);
        newRequest.setStatus("PENDING");

        requestDao.create(newRequest);

        context.setVariable("successMessage", "Votre demande a bien été envoyée. Elle sera examinée prochainement.");
        engine.process("become-organizer", context, resp.getWriter());
    }
} 