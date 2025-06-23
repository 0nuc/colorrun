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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.CourseDao;
import com.colorrun.dao.ParticipantDao;
import com.colorrun.model.Course;
import com.colorrun.model.Participant;
import com.colorrun.model.User;

public class CourseDetailsServlet extends HttpServlet {
    private TemplateEngine engine;
    private CourseDao courseDao;
    private ParticipantDao participantDao;

    @Override
    public void init() {
        // Configuration de Thymeleaf
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode(TemplateMode.HTML);

        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        // Initialisation des DAOs
        courseDao = new CourseDao();
        participantDao = new ParticipantDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Récupération de l'ID de la course depuis l'URL
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendRedirect(request.getContextPath() + "/courses");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 2) {
            response.sendRedirect(request.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(pathParts[1]);
            Course course = courseDao.findById(courseId);
            
            if (course == null) {
                response.sendRedirect(request.getContextPath() + "/courses");
                return;
            }

            // Récupération des participants
            List<Participant> participants = participantDao.findByCourseId(courseId);

            // Vérification si l'utilisateur est inscrit
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            boolean estInscrit = false;
            if (user != null) {
                estInscrit = participantDao.isUserRegistered(courseId, user.getId());
            }

            // Préparation du contexte pour Thymeleaf
            WebContext context = new WebContext(request, response, getServletContext());
            context.setVariable("course", course);
            context.setVariable("participants", participants);
            context.setVariable("user", user);
            context.setVariable("estInscrit", estInscrit);

            // Rendu de la page
            engine.process("course-details", context, response.getWriter());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/courses");
        }
    }
} 