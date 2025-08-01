package com.colorrun.servlet.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.MessageDao;
import com.colorrun.model.Message;
import com.colorrun.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebServlet("/api/messages/*")
public class MessageApiServlet extends HttpServlet {
    private MessageDao messageDao;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        messageDao = new MessageDao();
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
                // GET /api/messages?courseId=X - Messages d'une course
                String courseIdParam = req.getParameter("courseId");
                if (courseIdParam != null) {
                    int courseId = Integer.parseInt(courseIdParam);
                    List<Message> messages = messageDao.findByCourseId(courseId);
                    objectMapper.writeValue(resp.getWriter(), messages);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Paramètre courseId requis\"}");
                }
            } else {
                // GET /api/messages/{id} - Détails d'un message
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int messageId = Integer.parseInt(pathParts[1]);
                    Message message = messageDao.findById(messageId);
                    if (message != null) {
                        objectMapper.writeValue(resp.getWriter(), message);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Message non trouvé\"}");
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
            Message message = objectMapper.readValue(req.getReader(), Message.class);
            
            // Validation
            if (message.getContenu() == null || message.getContenu().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Contenu du message requis\"}");
                return;
            }
            
            if (message.getCourseId() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"ID de course requis\"}");
                return;
            }
            
            // Définir l'auteur
            message.setUserId(user.getId());
            
            // Sauvegarder
            messageDao.create(message);
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), message);
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Erreur lors de la création\"}");
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
            resp.getWriter().write("{\"error\":\"ID de message requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int messageId = Integer.parseInt(pathParts[1]);
            
            // Vérifier que le message existe
            Message message = messageDao.findById(messageId);
            if (message == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Message non trouvé\"}");
                return;
            }
            
            // Vérifier les permissions (auteur du message ou admin)
            if (!"ADMIN".equals(user.getRole()) && message.getUserId() != user.getId()) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Non autorisé à supprimer ce message\"}");
                return;
            }
            
            // Supprimer
            messageDao.delete(messageId);
            
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