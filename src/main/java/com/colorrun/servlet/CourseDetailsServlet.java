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
import com.colorrun.dao.ParticipantDao;
import com.colorrun.model.Course;
import com.colorrun.model.Message;
import com.colorrun.model.User;

@WebServlet("/course-details")
public class CourseDetailsServlet extends HttpServlet {
    private CourseDao courseDao;
    private ParticipantDao participantDao;

    @Override
    public void init() throws ServletException {
        courseDao = new CourseDao();
        participantDao = new ParticipantDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String courseIdStr = req.getParameter("id");
        if (courseIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdStr);
            Course course = courseDao.findById(courseId);
            if (course == null) {
                resp.sendRedirect(req.getContextPath() + "/courses");
                return;
            }

            List<User> participants = participantDao.findParticipantsByCourseId(courseId);
            List<Message> messages = courseDao.findMessagesByCourseId(courseId);

            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);
            Context ctx = new Context(req.getLocale());
            ctx.setVariable("course", course);
            ctx.setVariable("participants", participants);
            ctx.setVariable("messages", messages);

            HttpSession session = req.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                ctx.setVariable("user", user);
                if (user != null) {
                    boolean isParticipant = participantDao.isUserParticipating(user.getId(), courseId);
                    ctx.setVariable("isParticipant", isParticipant);
                }
            }

            engine.process("course-details", ctx, resp.getWriter());
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/courses");
        }
    }
}