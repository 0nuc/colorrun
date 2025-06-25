package com.colorrun.servlet;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.MessageDao;
import com.colorrun.model.Message;
import com.colorrun.model.User;

public class CourseMessageServlet extends HttpServlet {
    private MessageDao messageDao;

    @Override
    public void init() {
        messageDao = new MessageDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // ... existing code ...
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Récupérer l'ID de la course depuis le paramètre
        String courseIdParam = req.getParameter("courseId");
        if (courseIdParam == null || courseIdParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdParam);
            String contenu = req.getParameter("contenu");
            
            if (contenu == null || contenu.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/courses/" + courseId + "?error=emptyMessage");
                return;
            }
            
            // Créer le message
            Message message = new Message();
            message.setCourseId(courseId);
            message.setUserId(user.getId());
            message.setContenu(contenu.trim());
            message.setDateHeure(LocalDateTime.now());
            
            System.out.println("CourseMessageServlet.doPost() - Création du message:");
            System.out.println("  - CourseId: " + message.getCourseId());
            System.out.println("  - UserId: " + message.getUserId());
            System.out.println("  - Contenu: " + message.getContenu());
            System.out.println("  - DateHeure: " + message.getDateHeure());
            
            // Sauvegarder le message
            messageDao.create(message);
            System.out.println("CourseMessageServlet.doPost() - Message sauvegardé avec succès");
            
            // Rediriger vers la page de détails de la course
            resp.sendRedirect(req.getContextPath() + "/courses/" + courseId + "?success=messageSent");
            
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/courses");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/courses");
        }
    }
} 