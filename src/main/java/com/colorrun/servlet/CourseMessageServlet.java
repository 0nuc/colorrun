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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Récupérer l'ID de la course depuis le paramètre
        String courseIdParam = request.getParameter("courseId");
        if (courseIdParam == null || courseIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdParam);
            String contenu = request.getParameter("contenu");
            
            if (contenu == null || contenu.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/courses/" + courseId + "?error=emptyMessage");
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
            response.sendRedirect(request.getContextPath() + "/courses/" + courseId + "?success=messageSent");
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/courses");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/courses");
        }
    }
} 