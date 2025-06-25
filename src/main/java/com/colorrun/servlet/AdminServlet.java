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
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.CourseDao;
import com.colorrun.dao.OrganizerRequestDao;
import com.colorrun.dao.UserDao;
import com.colorrun.model.Course;
import com.colorrun.model.OrganizerRequest;
import com.colorrun.model.User;

public class AdminServlet extends HttpServlet {
    private OrganizerRequestDao requestDao;
    private UserDao userDao;
    private CourseDao courseDao;
    private TemplateEngine engine;

    @Override
    public void init() {
        requestDao = new OrganizerRequestDao();
        userDao = new UserDao();
        courseDao = new CourseDao();
        // Configuration Thymeleaf pour charger depuis le classpath
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode(TemplateMode.HTML);
        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.addDialect(new Java8TimeDialect());
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            return false;
        }
        User user = (User) session.getAttribute("user");
        return "ADMIN".equals(user.getRole());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        List<OrganizerRequest> requests = requestDao.findAll();
        List<User> users = userDao.findAll();
        List<Course> courses = courseDao.findAll();
        User user = (User) req.getSession().getAttribute("user");
        WebContext context = new WebContext(req, resp, getServletContext(), req.getLocale());
        context.setVariable("requests", requests);
        context.setVariable("users", users);
        context.setVariable("courses", courses);
        context.setVariable("session", req.getSession());
        context.setVariable("user", user);
        engine.process("admin", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = req.getParameter("action");
        HttpSession session = req.getSession();
        if ("approve".equals(action)) {
            int requestId = Integer.parseInt(req.getParameter("requestId"));
            int userId = Integer.parseInt(req.getParameter("userId"));
            requestDao.updateStatus(requestId, "APPROVED");
            userDao.updateUserRole(userId, "ORGANISATEUR");
        } else if ("reject".equals(action)) {
            int requestId = Integer.parseInt(req.getParameter("requestId"));
            requestDao.updateStatus(requestId, "REJECTED");
        } else if ("changeRole".equals(action)) {
            int userId = Integer.parseInt(req.getParameter("userId"));
            String newRole = req.getParameter("role");
            userDao.updateUserRole(userId, newRole);
        } else if ("deleteUser".equals(action)) {
            int userId = Integer.parseInt(req.getParameter("userId"));
            User currentUser = (User) session.getAttribute("user");
            if (currentUser.getId() != userId) {
                userDao.delete(userId);
            }
        } else if ("deleteCourse".equals(action)) {
            int courseId = Integer.parseInt(req.getParameter("courseId"));
            courseDao.delete(courseId);
        }
        resp.sendRedirect(req.getContextPath() + "/admin");
    }
} 