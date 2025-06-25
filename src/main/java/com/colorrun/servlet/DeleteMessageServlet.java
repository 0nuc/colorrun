package com.colorrun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.CourseDao;
import com.colorrun.dao.MessageDao;
import com.colorrun.model.Course;
import com.colorrun.model.Message;
import com.colorrun.model.User;

public class DeleteMessageServlet extends HttpServlet {
    private MessageDao messageDao;
    private CourseDao courseDao;

    @Override
    public void init() {
        messageDao = new MessageDao();
        courseDao = new CourseDao();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        System.out.println("=== DELETE MESSAGE SERVLET - doPost ===");
        
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            System.out.println("DeleteMessageServlet.doPost() - Utilisateur non connecté");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Récupérer l'ID du message à supprimer
        String messageIdStr = req.getParameter("messageId");
        if (messageIdStr == null || messageIdStr.trim().isEmpty()) {
            System.out.println("DeleteMessageServlet.doPost() - ID du message manquant");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID du message manquant");
            return;
        }

        int messageId;
        try {
            messageId = Integer.parseInt(messageIdStr);
        } catch (NumberFormatException e) {
            System.out.println("DeleteMessageServlet.doPost() - ID du message invalide: " + messageIdStr);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID du message invalide");
            return;
        }

        System.out.println("DeleteMessageServlet.doPost() - Suppression du message ID: " + messageId);

        // Récupérer le message
        Message message = messageDao.findById(messageId);
        if (message == null) {
            System.out.println("DeleteMessageServlet.doPost() - Message non trouvé");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Message non trouvé");
            return;
        }

        // Récupérer la course
        Course course = courseDao.findById(message.getCourseId());
        if (course == null) {
            System.out.println("DeleteMessageServlet.doPost() - Course non trouvée");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Course non trouvée");
            return;
        }

        // Vérifier les permissions : seul l'organisateur de la course ou un admin peut supprimer
        boolean canDelete = false;
        
        if ("ADMIN".equals(user.getRole())) {
            canDelete = true;
            System.out.println("DeleteMessageServlet.doPost() - Admin autorisé à supprimer");
        } else if ("ORGANISATEUR".equals(user.getRole()) && course.getOrganisateurId() == user.getId()) {
            canDelete = true;
            System.out.println("DeleteMessageServlet.doPost() - Organisateur autorisé à supprimer");
        } else if (message.getUserId() == user.getId()) {
            canDelete = true;
            System.out.println("DeleteMessageServlet.doPost() - Auteur du message autorisé à supprimer");
        }

        if (!canDelete) {
            System.out.println("DeleteMessageServlet.doPost() - Utilisateur non autorisé à supprimer ce message");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous n'êtes pas autorisé à supprimer ce message");
            return;
        }

        // Supprimer le message
        boolean deleted = messageDao.delete(messageId);
        if (deleted) {
            System.out.println("DeleteMessageServlet.doPost() - Message supprimé avec succès");
            // Rediriger vers la page de détails de la course avec un message de succès
            resp.sendRedirect(req.getContextPath() + "/courses/" + course.getId() + "?success=messageDeleted");
        } else {
            System.out.println("DeleteMessageServlet.doPost() - Erreur lors de la suppression");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la suppression du message");
        }
    }
} 