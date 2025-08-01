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

import com.colorrun.dao.OrganizerRequestDao;
import com.colorrun.dao.UserDao;
import com.colorrun.model.OrganizerRequest;
import com.colorrun.model.User;

public class AdminDashboardServlet extends HttpServlet {

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
        engine.addDialect(new Java8TimeDialect());
        requestDao = new OrganizerRequestDao();
        userDao = new UserDao();
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

        WebContext context = new WebContext(req, resp, getServletContext());
        context.setVariable("requests", requests);
        context.setVariable("users", users);

        HttpSession session = req.getSession();
        if (session.getAttribute("successMessage") != null) {
            context.setVariable("successMessage", session.getAttribute("successMessage"));
            session.removeAttribute("successMessage");
        }

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

        int requestId = Integer.parseInt(req.getParameter("requestId"));
        String action = req.getParameter("action");

        if ("approve".equals(action)) {
            int userId = Integer.parseInt(req.getParameter("userId"));
            // Mettre à jour le statut de la demande
            requestDao.updateStatus(requestId, "APPROVED");
            // Mettre à jour le rôle de l'utilisateur
            userDao.updateUserRole(userId, "ORGANISATEUR");
            req.getSession().setAttribute("successMessage", "La demande a été approuvée.");
        } else if ("reject".equals(action)) {
            requestDao.updateStatus(requestId, "REJECTED");
            req.getSession().setAttribute("successMessage", "La demande a été rejetée.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/requests");
    }
} 