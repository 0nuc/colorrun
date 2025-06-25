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
import com.colorrun.dao.MessageDao;
import com.colorrun.dao.ParticipantDao;
import com.colorrun.model.Course;
import com.colorrun.model.Message;
import com.colorrun.model.Participant;
import com.colorrun.model.User;

public class CourseDetailsServlet extends HttpServlet {
    private TemplateEngine engine;
    private CourseDao courseDao;
    private ParticipantDao participantDao;
    private MessageDao messageDao;

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
        messageDao = new MessageDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // Récupération de l'ID de la course depuis l'URL
        String pathInfo = req.getPathInfo();
        System.out.println("[DEBUG] pathInfo = " + pathInfo);
        if (pathInfo == null || pathInfo.equals("/")) {
            System.out.println("[DEBUG] pathInfo null ou /, redirection vers /courses");
            resp.sendRedirect(req.getContextPath() + "/courses");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        System.out.println("[DEBUG] pathParts = " + java.util.Arrays.toString(pathParts));
        if (pathParts.length < 2) {
            System.out.println("[DEBUG] pathParts trop court, redirection vers /courses");
            resp.sendRedirect(req.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(pathParts[1]);
            System.out.println("[DEBUG] courseId = " + courseId);
            Course course = courseDao.findById(courseId);
            System.out.println("[DEBUG] course = " + course);
            
            if (course == null) {
                System.out.println("[DEBUG] course null, redirection vers /courses");
                resp.sendRedirect(req.getContextPath() + "/courses");
                return;
            }

            // Récupération des participants
            List<Participant> participants = participantDao.findByCourseId(courseId);
            System.out.println("[DEBUG] participants.size = " + (participants != null ? participants.size() : "null"));

            // Récupération des messages
            List<Message> messages = messageDao.findByCourseId(courseId);
            System.out.println("[DEBUG] messages.size = " + (messages != null ? messages.size() : "null"));

            // Vérification si l'utilisateur est inscrit
            HttpSession session = req.getSession();
            User user = (User) session.getAttribute("user");
            System.out.println("[DEBUG] user = " + user);
            boolean estInscrit = false;
            if (user != null) {
                estInscrit = participantDao.isUserRegistered(courseId, user.getId());
            }
            System.out.println("[DEBUG] estInscrit = " + estInscrit);

            // Préparation du contexte pour Thymeleaf
            WebContext context = new WebContext(req, resp, getServletContext());
            context.setVariable("course", course);
            context.setVariable("participants", participants);
            context.setVariable("messages", messages);
            context.setVariable("user", user);
            context.setVariable("estInscrit", estInscrit);

            // Rendu de la page
            System.out.println("[DEBUG] rendu de la page course-details");
            engine.process("course-details", context, resp.getWriter());
        } catch (NumberFormatException e) {
            System.out.println("[DEBUG] NumberFormatException, redirection vers /courses");
            resp.sendRedirect(req.getContextPath() + "/courses");
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