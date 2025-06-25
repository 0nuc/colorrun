package com.colorrun.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.colorrun.dao.CourseDao;
import com.colorrun.dao.UserDao;
import com.colorrun.model.Course;
import com.colorrun.model.User;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1MB
    maxFileSize = 1024 * 1024 * 5,   // 5MB
    maxRequestSize = 1024 * 1024 * 10 // 10MB
)
public class ProfileServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";
    private CourseDao courseDao;

    @Override
    public void init() throws ServletException {
        courseDao = new CourseDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        System.out.println("[ProfileServlet] doGet called");
        HttpSession session = req.getSession(false);
        
        User user = (User) (session != null ? session.getAttribute("user") : null);

        if (user == null) {
            System.out.println("[ProfileServlet] No user in session, redirecting to login");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        System.out.println("[ProfileServlet] User values before rendering:");
        System.out.println("  id=" + user.getId());
        System.out.println("  firstName=" + user.getFirstName());
        System.out.println("  lastName=" + user.getLastName());
        System.out.println("  email=" + user.getEmail());
        System.out.println("  phone=" + user.getPhone());
        System.out.println("  address=" + user.getAddress());
        System.out.println("  postalCode=" + user.getPostalCode());
        System.out.println("  city=" + user.getCity());
        System.out.println("  newsletter=" + user.isNewsletter());
        System.out.println("  profilePicture=" + user.getProfilePicture());

        String successMessage = (String) req.getAttribute("successMessage");
        System.out.println("[ProfileServlet] Rendering profile for user: " + user.getEmail());

        List<Course> userRaces = courseDao.findCoursesByUserId(user.getId());

        // Calcul des statistiques
        int completedRaces = 0;
        int upcomingRaces = 0;
        int totalDistance = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Course race : userRaces) {
            if (race.getDateHeure() != null) {
                if (race.getDateHeure().isBefore(now)) {
                    completedRaces++;
                    totalDistance += race.getDistance();
                } else {
                    upcomingRaces++;
                }
            }
        }
        user.setCompletedRaces(completedRaces);
        user.setUpcomingRaces(upcomingRaces);
        user.setTotalDistance(totalDistance);

        try {
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("/WEB-INF/views/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding("UTF-8");
            TemplateEngine engine = new TemplateEngine();
            engine.setTemplateResolver(resolver);

            WebContext ctx = new WebContext(req, resp, getServletContext(), req.getLocale());
            ctx.setVariable("user", user);
            ctx.setVariable("userRaces", userRaces);
            ctx.setVariable("now", now);
            if (successMessage != null) {
                ctx.setVariable("successMessage", successMessage);
            }
            engine.process("profil", ctx, resp.getWriter());
            System.out.println("[ProfileServlet] Profile page rendered successfully");
        } catch (Exception e) {
            System.out.println("[ProfileServlet] Exception during Thymeleaf rendering: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        System.out.println("[ProfileServlet] doPost called");
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        if (user == null) {
            System.out.println("[ProfileServlet] No user in session, redirecting to login");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        System.out.println("[ProfileServlet] User values before update:");
        System.out.println("  id=" + user.getId());
        System.out.println("  firstName=" + user.getFirstName());
        System.out.println("  lastName=" + user.getLastName());
        System.out.println("  email=" + user.getEmail());
        System.out.println("  phone=" + user.getPhone());
        System.out.println("  address=" + user.getAddress());
        System.out.println("  postalCode=" + user.getPostalCode());
        System.out.println("  city=" + user.getCity());
        System.out.println("  newsletter=" + user.isNewsletter());
        System.out.println("  profilePicture=" + user.getProfilePicture());

        if (req.getContentType() != null && req.getContentType().toLowerCase().startsWith("multipart/")) {
            Part filePart = req.getPart("profilePicture");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();

                String filePath = uploadPath + File.separator + user.getId() + "_" + fileName;
                filePart.write(filePath);

                user.setProfilePicture("/" + UPLOAD_DIR + "/" + user.getId() + "_" + fileName);
            }
        }

        UserDao userDao = new UserDao();
        String newPassword = req.getParameter("newPassword");
        boolean passwordChanged = false;
        if (newPassword != null && !newPassword.isEmpty()) {
            // Changement de mot de passe uniquement
            try {
                userDao.updatePassword(user.getId(), newPassword);
                user.setPassword(newPassword);
                passwordChanged = true;
                req.setAttribute("successMessage", "Mot de passe modifié !");
            } catch (Exception e) {
                req.setAttribute("errorMessage", "Erreur lors de la modification du mot de passe.");
            }
        } else {
            // Mise à jour des autres infos
            User updatedUser = new User();
            updatedUser.setId(user.getId());
            updatedUser.setEmail(user.getEmail());
            updatedUser.setFirstName(req.getParameter("firstName"));
            updatedUser.setLastName(req.getParameter("lastName"));
            updatedUser.setAddress(req.getParameter("address"));
            updatedUser.setPostalCode(req.getParameter("postalCode"));
            updatedUser.setCity(req.getParameter("city"));
            updatedUser.setNewsletter(req.getParameter("newsletter") != null);
            updatedUser.setPhone(req.getParameter("phone"));
            updatedUser.setProfilePicture(user.getProfilePicture());
            updatedUser.setRole(user.getRole());
            updatedUser.setPassword(user.getPassword());
            try {
                userDao.update(updatedUser);
                session.setAttribute("user", updatedUser);
                req.setAttribute("successMessage", "Profil mis à jour avec succès !");
            } catch (Exception e) {
                // Si erreur, on recharge l'utilisateur depuis la base pour garder des valeurs cohérentes
                User freshUser = userDao.findByEmail(user.getEmail());
                session.setAttribute("user", freshUser);
                req.setAttribute("errorMessage", "Erreur lors de la mise à jour du profil (colonne manquante ?).");
            }
        }
        doGet(req, resp);
    }
}