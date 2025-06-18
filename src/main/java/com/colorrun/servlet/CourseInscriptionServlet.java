package com.colorrun.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.colorrun.dao.ParticipantDao;
import com.colorrun.model.User;

@WebServlet("/course-inscription")
public class CourseInscriptionServlet extends HttpServlet {
    private ParticipantDao participantDao;

    @Override
    public void init() throws ServletException {
        System.out.println("CourseInscriptionServlet.init() - Démarrage");
        participantDao = new ParticipantDao();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String courseIdStr = req.getParameter("courseId");
        if (courseIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdStr);
            User user = (User) session.getAttribute("user");
            System.out.println("CourseInscriptionServlet.doPost() - CourseId: " + courseId);
            System.out.println("CourseInscriptionServlet.doPost() - UserId: " + user.getId());

            // Vérifier si l'utilisateur est déjà inscrit
            if (participantDao.isUserParticipating(user.getId(), courseId)) {
                System.out.println("CourseInscriptionServlet.doPost() - Utilisateur déjà inscrit");
                resp.sendRedirect(req.getContextPath() + "/course-details?id=" + courseId);
                return;
            }

            // Ajouter le participant
            System.out.println("CourseInscriptionServlet.doPost() - Ajout du participant");
            participantDao.addParticipant(courseId, user.getId());
            System.out.println("CourseInscriptionServlet.doPost() - Participant ajouté avec succès");

            // Rediriger vers la page de la course
            resp.sendRedirect(req.getContextPath() + "/course-details?id=" + courseId);
        } catch (NumberFormatException e) {
            System.out.println("CourseInscriptionServlet.doPost() - Erreur de format de l'ID: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/courses");
        } catch (Exception e) {
            System.out.println("CourseInscriptionServlet.doPost() - Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/courses");
        }
    }
}