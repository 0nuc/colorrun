package com.colorrun.servlet.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.ParticipantDao;
import com.colorrun.model.Participant;
import com.colorrun.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebServlet("/api/participants/*")
public class ParticipantApiServlet extends HttpServlet {
    private ParticipantDao participantDao;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        participantDao = new ParticipantDao();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/participants?courseId=X - Participants d'une course
                String courseIdParam = req.getParameter("courseId");
                if (courseIdParam != null) {
                    int courseId = Integer.parseInt(courseIdParam);
                    List<Participant> participants = participantDao.findByCourseId(courseId);
                    objectMapper.writeValue(resp.getWriter(), participants);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Paramètre courseId requis\"}");
                }
            } else {
                // GET /api/participants/{id} - Détails d'un participant
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int participantId = Integer.parseInt(pathParts[1]);
                    Participant participant = participantDao.findById(participantId);
                    if (participant != null) {
                        objectMapper.writeValue(resp.getWriter(), participant);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Participant non trouvé\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"URL invalide\"}");
                }
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID invalide\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Erreur serveur\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Vérifier l'authentification
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        try {
            // Lire le JSON du body
            Participant participant = objectMapper.readValue(req.getReader(), Participant.class);
            
            // Validation
            if (participant.getCourseId() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"ID de course requis\"}");
                return;
            }
            
            // Définir l'utilisateur
            participant.setUserId(user.getId());
            
            // Vérifier si déjà inscrit
            if (participantDao.isUserRegistered(participant.getCourseId(), user.getId())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("{\"error\":\"Déjà inscrit à cette course\"}");
                return;
            }
            
            // Sauvegarder
            participantDao.add(participant);
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), participant);
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Erreur lors de l'inscription\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Vérifier l'authentification
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de participant requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int participantId = Integer.parseInt(pathParts[1]);
            
            // Vérifier que le participant existe
            Participant participant = participantDao.findById(participantId);
            if (participant == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Participant non trouvé\"}");
                return;
            }
            
            // Vérifier les permissions (le participant lui-même ou admin)
            if (!"ADMIN".equals(user.getRole()) && participant.getUserId() != user.getId()) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Non autorisé à désinscrire ce participant\"}");
                return;
            }
            
            // Supprimer
            participantDao.delete(participantId);
            
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID invalide\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Erreur lors de la désinscription\"}");
        }
    }
} 