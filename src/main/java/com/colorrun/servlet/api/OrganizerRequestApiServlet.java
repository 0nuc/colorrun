package com.colorrun.servlet.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.OrganizerRequestDao;
import com.colorrun.model.OrganizerRequest;
import com.colorrun.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebServlet("/api/organizer-requests/*")
public class OrganizerRequestApiServlet extends HttpServlet {
    private OrganizerRequestDao organizerRequestDao;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        organizerRequestDao = new OrganizerRequestDao();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Vérifier l'authentification admin
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null || !"ADMIN".equals(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/organizer-requests - Liste toutes les demandes
                List<OrganizerRequest> requests = organizerRequestDao.findAll();
                objectMapper.writeValue(resp.getWriter(), requests);
            } else {
                // GET /api/organizer-requests/{id} - Détails d'une demande
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int requestId = Integer.parseInt(pathParts[1]);
                    OrganizerRequest request = organizerRequestDao.findById(requestId);
                    if (request != null) {
                        objectMapper.writeValue(resp.getWriter(), request);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Demande non trouvée\"}");
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
            OrganizerRequest request = objectMapper.readValue(req.getReader(), OrganizerRequest.class);
            
            // Validation
            if (request.getMotivation() == null || request.getMotivation().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Motif requis\"}");
                return;
            }
            
            // Définir l'utilisateur
            request.setUserId(user.getId());
            request.setStatus("EN_ATTENTE");
            
            // Vérifier si déjà une demande en cours
            if (organizerRequestDao.hasPendingRequest(user.getId())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("{\"error\":\"Vous avez déjà une demande en cours\"}");
                return;
            }
            
            // Sauvegarder
            organizerRequestDao.save(request);
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), request);
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Erreur lors de la création\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Vérifier l'authentification admin
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null || !"ADMIN".equals(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de demande requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int requestId = Integer.parseInt(pathParts[1]);
            
            // Vérifier que la demande existe
            OrganizerRequest existingRequest = organizerRequestDao.findById(requestId);
            if (existingRequest == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Demande non trouvée\"}");
                return;
            }
            
            // Lire le JSON du body
            OrganizerRequest request = objectMapper.readValue(req.getReader(), OrganizerRequest.class);
            request.setId(requestId);
            
            // Mettre à jour
            organizerRequestDao.update(request);
            
            objectMapper.writeValue(resp.getWriter(), request);
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID invalide\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Erreur lors de la modification\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Vérifier l'authentification admin
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null || !"ADMIN".equals(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de demande requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int requestId = Integer.parseInt(pathParts[1]);
            
            // Vérifier que la demande existe
            OrganizerRequest request = organizerRequestDao.findById(requestId);
            if (request == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Demande non trouvée\"}");
                return;
            }
            
            // Supprimer
            organizerRequestDao.delete(requestId);
            
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID invalide\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Erreur lors de la suppression\"}");
        }
    }
} 