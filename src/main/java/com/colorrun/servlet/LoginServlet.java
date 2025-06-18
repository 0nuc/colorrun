package com.colorrun.servlet;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.UserDao;
import com.colorrun.model.User;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        Context ctx = new Context(req.getLocale());
        HttpSession session = req.getSession(false);
        if (session != null) {
            ctx.setVariable("user", session.getAttribute("user"));
        }
        engine.process("login", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        Optional<User> userOpt = userDao.findByEmailAndPassword(email, password);

        if (userOpt.isPresent()) {
            HttpSession session = req.getSession();
            session.setAttribute("user", userOpt.get());
            resp.sendRedirect(req.getContextPath() + "/home");
        } else {
            resp.sendRedirect(req.getContextPath() + "/login?error=true");
        }
    }
}