package com.colorrun.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();

        // Configuration du template resolver
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/META-INF/resources/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        // Configuration du template engine
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Création du contexte
        Context context = new Context(req.getLocale());
        context.setVariable("contextPath", req.getContextPath());

        // Ajout des variables au contexte
        HttpSession session = req.getSession(false);
        if (session != null) {
            context.setVariable("user", session.getAttribute("user"));
        }

        // Process du template
        resp.setContentType("text/html;charset=UTF-8");
        templateEngine.process("home", context, resp.getWriter());
    }
}