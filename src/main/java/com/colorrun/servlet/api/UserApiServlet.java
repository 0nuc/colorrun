package com.colorrun.servlet.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.UserDao;
import com.colorrun.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebServlet("/api/users/*")
public class UserApiServlet extends HttpServlet {
    private UserDao userDao;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
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
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/users - Liste tous les utilisateurs
                List<User> users = userDao.findAll();
                objectMapper.writeValue(resp.getWriter(), users);
            } else {
                // GET /api/users/{id} - Détails d'un utilisateur
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int userId = Integer.parseInt(pathParts[1]);
                    User user = userDao.findById(userId);
                    if (user != null) {
                        // Masquer le mot de passe
                        user.setPassword(null);
                        objectMapper.writeValue(resp.getWriter(), user);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Utilisateur non trouvé\"}");
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Vérifier l'authentification
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID utilisateur requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int userId = Integer.parseInt(pathParts[1]);
            
            // Vérifier les permissions
            if (!"ADMIN".equals(currentUser.getRole()) && currentUser.getId() != userId) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Non autorisé à modifier cet utilisateur\"}");
                return;
            }
            
            // Vérifier que l'utilisateur existe
            User existingUser = userDao.findById(userId);
            if (existingUser == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Utilisateur non trouvé\"}");
                return;
            }
            
            // Lire le JSON du body
            User user = objectMapper.readValue(req.getReader(), User.class);
            user.setId(userId);
            
            // Si ce n'est pas un admin, on ne peut pas changer le rôle
            if (!"ADMIN".equals(currentUser.getRole())) {
                user.setRole(existingUser.getRole());
            }
            
            // Mettre à jour
            userDao.update(user);
            
            // Masquer le mot de passe dans la réponse
            user.setPassword(null);
            objectMapper.writeValue(resp.getWriter(), user);
            
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
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID utilisateur requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int userId = Integer.parseInt(pathParts[1]);
            
            // Vérifier que l'utilisateur existe
            User user = userDao.findById(userId);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Utilisateur non trouvé\"}");
                return;
            }
            
            // Empêcher la suppression de l'admin principal
            if (currentUser.getId() == userId) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Impossible de supprimer votre propre compte\"}");
                return;
            }
            
            // Supprimer
            userDao.delete(userId);
            
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