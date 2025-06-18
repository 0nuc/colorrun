package com.colorrun.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.UserDao;
import com.colorrun.model.User;

@WebServlet("/profile")
@MultipartConfig
public class ProfileServlet extends HttpServlet {
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        Context ctx = new Context(req.getLocale());
        ctx.setVariable("user", session.getAttribute("user"));
        engine.process("profil", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        user.setFirstName(req.getParameter("firstName"));
        user.setLastName(req.getParameter("lastName"));
        user.setAddress(req.getParameter("address"));
        user.setPostalCode(req.getParameter("postalCode"));
        user.setCity(req.getParameter("city"));
        user.setNewsletter(req.getParameter("newsletter") != null);

        Part filePart = req.getPart("profilePicture");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = user.getId() + "_" + filePart.getSubmittedFileName();
            Path uploadPath = Paths.get(getServletContext().getRealPath("/uploads"));
            Files.createDirectories(uploadPath);
            filePart.write(uploadPath.resolve(fileName).toString());
            user.setProfilePicture("/uploads/" + fileName);
        }

        userDao.update(user);
        session.setAttribute("user", user);
        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}