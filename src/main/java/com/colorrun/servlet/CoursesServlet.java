package com.colorrun.servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.CourseDao;
import com.colorrun.model.Course;

@WebServlet("/courses")
public class CoursesServlet extends HttpServlet {
    private CourseDao courseDao;

    @Override
    public void init() throws ServletException {
        System.out.println("CoursesServlet.init() - Démarrage");
        courseDao = new CourseDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("CoursesServlet.doGet() - Début");

        try {
            // Configuration Thymeleaf
            System.out.println("Configuration Thymeleaf...");
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);

            // Récupération des courses depuis la BDD
            System.out.println("Récupération des courses...");
            List<Course> courses = courseDao.findAll();
            System.out.println("Nombre de courses trouvées : " + courses.size());

            // Passage des données à la vue
            System.out.println("Préparation du contexte...");
            Context ctx = new Context(req.getLocale());
            ctx.setVariable("courses", courses);
            HttpSession session = req.getSession(false);
            if (session != null) {
                ctx.setVariable("user", session.getAttribute("user"));
            }

            // Rendu de la vue
            System.out.println("Rendu de la vue...");
            engine.process("courses", ctx, resp.getWriter());
            System.out.println("CoursesServlet.doGet() - Fin");

        } catch (Exception e) {
            System.err.println("ERREUR dans CoursesServlet.doGet() :");
            e.printStackTrace();
            throw e;
        }
    }
}