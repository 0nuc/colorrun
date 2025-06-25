package com.colorrun.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.CourseDao;
import com.colorrun.model.Course;

// @WebServlet("/courses")
public class CoursesServlet extends HttpServlet {
    private CourseDao courseDao;

    @Override
    public void init() {
        System.out.println("CoursesServlet.init() - Démarrage");
        courseDao = new CourseDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        System.out.println("CoursesServlet.doGet() - Début");
        
        try {
            // Configuration Thymeleaf
            System.out.println("Configuration Thymeleaf...");
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resp.setContentType("text/html; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);

            // Récupération des filtres
            String date = req.getParameter("date");
            String ville = req.getParameter("ville");
            String distance = req.getParameter("distance");
            String tri = req.getParameter("tri");

            // Récupération des courses depuis la BDD
            System.out.println("Récupération des courses...");
            List<Course> courses = courseDao.findWithFilters(date, ville, distance, tri);
            System.out.println("Nombre de courses trouvées : " + courses.size());
            
            // Debug détaillé des courses
            for (int i = 0; i < courses.size(); i++) {
                Course course = courses.get(i);
                System.out.println("Course " + (i+1) + ":");
                System.out.println("  - ID: " + course.getId());
                System.out.println("  - Nom: " + course.getNom());
                System.out.println("  - Description: " + course.getDescription());
                System.out.println("  - Date: " + course.getDateHeure());
                System.out.println("  - Lieu: " + course.getLieu());
                System.out.println("  - Distance: " + course.getDistance());
                System.out.println("  - Prix: " + course.getPrix());
                System.out.println("  - MaxParticipants: " + course.getMaxParticipants());
            }

            // Passage des données à la vue
            System.out.println("Préparation du contexte...");
            WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
            ctx.setVariable("courses", courses);
            ctx.setVariable("dateFilter", date);
            ctx.setVariable("villeFilter", ville);
            ctx.setVariable("distanceFilter", distance);
            ctx.setVariable("triFilter", tri);
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // ... existing code ...
    }
} 