package com.colorrun.servlet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
import com.colorrun.model.User;

public class CourseEditServlet extends HttpServlet {
    private CourseDao courseDao;
    private TemplateEngine engine;

    @Override
    public void init() {
        courseDao = new CourseDao();
        // Initialisation Thymeleaf
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode("HTML");
        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null || !"ORGANISATEUR".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getRequestURI();
        Course course = null;
        if (path.contains("/courses/edit/")) {
            String[] parts = path.split("/edit/");
            int courseId = Integer.parseInt(parts[1]);
            course = courseDao.findById(courseId);
            // Sécurité : seul l'organisateur de la course peut modifier
            if (course == null || course.getOrganisateurId() != user.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        // Utilisation de Thymeleaf pour le rendu
        WebContext context = new WebContext(req, resp, getServletContext());
        context.setVariable("course", course);
        context.setVariable("user", user);
        engine.process("course-edit", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        System.out.println("=== COURSE EDIT SERVLET - doPost ===");
        
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null || !"ORGANISATEUR".equals(user.getRole())) {
            System.out.println("CourseEditServlet.doPost() - Utilisateur non autorisé");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String nom = req.getParameter("nom");
        String description = req.getParameter("description");
        String dateHeureStr = req.getParameter("dateHeure");
        String lieu = req.getParameter("lieu");
        int distance = Integer.parseInt(req.getParameter("distance"));
        int maxParticipants = Integer.parseInt(req.getParameter("maxParticipants"));
        double prix = Double.parseDouble(req.getParameter("prix"));
        boolean avecObstacles = Boolean.parseBoolean(req.getParameter("avecObstacles"));
        String causeSoutenue = req.getParameter("causeSoutenue");
        
        System.out.println("CourseEditServlet.doPost() - Données reçues:");
        System.out.println("  - Nom: " + nom);
        System.out.println("  - Description: " + description);
        System.out.println("  - Date: " + dateHeureStr);
        System.out.println("  - Lieu: " + lieu);
        System.out.println("  - Distance: " + distance);
        System.out.println("  - MaxParticipants: " + maxParticipants);
        System.out.println("  - Prix: " + prix);
        System.out.println("  - AvecObstacles: " + avecObstacles);
        System.out.println("  - CauseSoutenue: " + causeSoutenue);
        System.out.println("  - OrganisateurId: " + user.getId());
        
        LocalDateTime dateHeure = LocalDateTime.parse(dateHeureStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        String path = req.getRequestURI();
        System.out.println("CourseEditServlet.doPost() - Path: " + path);
        
        if (path.endsWith("/create")) {
            System.out.println("CourseEditServlet.doPost() - Création d'une nouvelle course");
            Course course = new Course();
            course.setNom(nom);
            course.setDescription(description);
            course.setDateHeure(dateHeure);
            course.setLieu(lieu);
            course.setDistance(distance);
            course.setMaxParticipants(maxParticipants);
            course.setPrix(prix);
            course.setAvecObstacles(avecObstacles);
            course.setCauseSoutenue(causeSoutenue);
            course.setOrganisateurId(user.getId());
            
            System.out.println("CourseEditServlet.doPost() - Sauvegarde de la course...");
            courseDao.save(course);
            System.out.println("CourseEditServlet.doPost() - Course sauvegardée avec succès");
        } else if (path.contains("/courses/edit/")) {
            System.out.println("CourseEditServlet.doPost() - Modification d'une course existante");
            String[] parts = path.split("/edit/");
            int courseId = Integer.parseInt(parts[1]);
            Course course = courseDao.findById(courseId);
            if (course == null || course.getOrganisateurId() != user.getId()) {
                System.out.println("CourseEditServlet.doPost() - Course non trouvée ou non autorisée");
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            course.setNom(nom);
            course.setDescription(description);
            course.setDateHeure(dateHeure);
            course.setLieu(lieu);
            course.setDistance(distance);
            course.setMaxParticipants(maxParticipants);
            course.setPrix(prix);
            course.setAvecObstacles(avecObstacles);
            course.setCauseSoutenue(causeSoutenue);
            courseDao.update(course);
            System.out.println("CourseEditServlet.doPost() - Course modifiée avec succès");
        }
        
        System.out.println("CourseEditServlet.doPost() - Redirection vers /courses");
        resp.sendRedirect(req.getContextPath() + "/courses");
    }
} 