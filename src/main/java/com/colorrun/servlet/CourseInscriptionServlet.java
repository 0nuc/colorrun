package com.colorrun.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.CourseDao;
import com.colorrun.dao.ParticipantDao;
import com.colorrun.model.Course;
import com.colorrun.model.Participant;
import com.colorrun.model.User;
import com.colorrun.service.BibService;

public class CourseInscriptionServlet extends HttpServlet {
    private ParticipantDao participantDao;
    private CourseDao courseDao;
    private BibService bibService;

    @Override
    public void init() {
        System.out.println("CourseInscriptionServlet.init() - Démarrage");
        participantDao = new ParticipantDao();
        courseDao = new CourseDao();
        bibService = new BibService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        System.out.println("CourseInscriptionServlet.doGet() - Début");
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("CourseInscriptionServlet.doGet() - Utilisateur non connecté");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("user");

        // Récupérer l'ID de la course depuis l'URL
        String pathInfo = req.getPathInfo();
        System.out.println("CourseInscriptionServlet.doGet() - PathInfo: " + pathInfo);
        
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 2) {
            System.out.println("CourseInscriptionServlet.doGet() - URL invalide");
            resp.sendRedirect(req.getContextPath() + "/courses");
            return;
        }

        try {
            int courseId = Integer.parseInt(pathParts[1]);
            System.out.println("CourseInscriptionServlet.doGet() - CourseId: " + courseId);

            // Vérifier si l'utilisateur est déjà inscrit
            if (participantDao.isUserRegistered(courseId, user.getId())) {
                System.out.println("CourseInscriptionServlet.doGet() - Utilisateur déjà inscrit");
                resp.sendRedirect(req.getContextPath() + "/courses/" + courseId + "?error=alreadyRegistered");
                return;
            }

            // Récupérer les détails de la course
            Course course = courseDao.findById(courseId);
            if (course == null) {
                System.out.println("CourseInscriptionServlet.doGet() - Course non trouvée");
                resp.sendRedirect(req.getContextPath() + "/courses");
                return;
            }
            
            // Créer le participant
            Participant participant = new Participant();
            participant.setCourseId(courseId);
            participant.setUserId(user.getId());
            
            // Ajouter le participant et récupérer l'objet avec l'ID
            Participant newParticipant = participantDao.add(participant);
            if (newParticipant == null || newParticipant.getId() == 0) {
                 System.out.println("CourseInscriptionServlet.doGet() - Erreur lors de l'ajout du participant");
                 resp.sendRedirect(req.getContextPath() + "/courses/" + courseId + "?error=registrationFailed");
                 return;
            }
            System.out.println("CourseInscriptionServlet.doGet() - Participant ajouté avec succès avec l'ID: " + newParticipant.getId());
            
            // Générer le PDF du dossard
            try {
                System.out.println("CourseInscriptionServlet.doGet() - Génération du PDF du dossard");
                byte[] pdfBytes = bibService.generateBibPdf(user, course, newParticipant);
                
                // Configurer la réponse pour le téléchargement
                resp.setContentType("application/pdf");
                resp.setHeader("Content-Disposition", "attachment; filename=\"dossard_" + course.getNom().replace(" ", "_") + "_" + user.getLastName() + ".pdf\"");
                resp.setContentLength(pdfBytes.length);
                
                // Écrire le PDF dans la réponse
                resp.getOutputStream().write(pdfBytes);
                resp.getOutputStream().flush();
                return; // Arrêter ici pour éviter la redirection
            } catch (Exception e) {
                System.out.println("CourseInscriptionServlet.doGet() - Erreur lors de la génération du PDF: " + e.getMessage());
                e.printStackTrace();
                // En cas d'erreur de génération du PDF, rediriger avec un message
                resp.sendRedirect(req.getContextPath() + "/courses/" + courseId + "?error=pdfGenerationFailed");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("CourseInscriptionServlet.doGet() - Erreur de format de l'ID: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/courses");
        } catch (Exception e) {
            System.out.println("CourseInscriptionServlet.doGet() - Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/courses");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // ... existing code ...
    }
} 