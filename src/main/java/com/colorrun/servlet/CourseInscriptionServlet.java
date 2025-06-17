package com.colorrun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.ParticipantDao;
import com.colorrun.model.Participant;
import com.colorrun.model.User;

@WebServlet("/courses/*/inscription")
public class CourseInscriptionServlet extends HttpServlet {
    private ParticipantDao participantDao;

    @Override
    public void init() {
        System.out.println("CourseInscriptionServlet.init() - Démarrage");
        participantDao = new ParticipantDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        System.out.println("CourseInscriptionServlet.doGet() - Début");
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            System.out.println("CourseInscriptionServlet.doGet() - Utilisateur non connecté");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Récupérer l'ID de la course depuis l'URL
        String pathInfo = request.getPathInfo();
        System.out.println("CourseInscriptionServlet.doGet() - PathInfo: " + pathInfo);
        
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 2) {
            System.out.println("CourseInscriptionServlet.doGet() - URL invalide");
            response.sendRedirect(request.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(pathParts[1]);
            System.out.println("CourseInscriptionServlet.doGet() - CourseId: " + courseId);
            System.out.println("CourseInscriptionServlet.doGet() - UserId: " + user.getId());
            
            // Vérifier si l'utilisateur est déjà inscrit
            if (participantDao.isUserRegistered(courseId, user.getId())) {
                System.out.println("CourseInscriptionServlet.doGet() - Utilisateur déjà inscrit");
                response.sendRedirect(request.getContextPath() + "/courses/" + courseId);
                return;
            }
            
            // Créer le participant
            Participant participant = new Participant();
            participant.setCourseId(courseId);
            participant.setUserId(user.getId());
            
            // Ajouter le participant
            System.out.println("CourseInscriptionServlet.doGet() - Ajout du participant");
            participantDao.add(participant);
            System.out.println("CourseInscriptionServlet.doGet() - Participant ajouté avec succès");
            
            // Rediriger vers la page de la course
            response.sendRedirect(request.getContextPath() + "/courses/" + courseId);
        } catch (NumberFormatException e) {
            System.out.println("CourseInscriptionServlet.doGet() - Erreur de format de l'ID: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/courses");
        } catch (Exception e) {
            System.out.println("CourseInscriptionServlet.doGet() - Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/courses");
        }
    }
} 