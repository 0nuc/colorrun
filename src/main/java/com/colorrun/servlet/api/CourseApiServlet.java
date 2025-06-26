package com.colorrun.servlet.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.colorrun.dao.CourseDao;
import com.colorrun.model.Course;
import com.colorrun.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebServlet("/api/courses/*")
public class CourseApiServlet extends HttpServlet {
    private CourseDao courseDao;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        courseDao = new CourseDao();
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
                // GET /api/courses - Liste toutes les courses
                List<Course> courses = courseDao.findAll();
                objectMapper.writeValue(resp.getWriter(), courses);
            } else {
                // GET /api/courses/{id} - Détails d'une course
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    int courseId = Integer.parseInt(pathParts[1]);
                    Course course = courseDao.findById(courseId);
                    if (course != null) {
                        objectMapper.writeValue(resp.getWriter(), course);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\":\"Course non trouvée\"}");
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
        
        if (user == null || (!"ORGANISATEUR".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        try {
            // Lire le JSON du body
            Course course = objectMapper.readValue(req.getReader(), Course.class);
            
            // Validation
            if (course.getNom() == null || course.getNom().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Nom de course requis\"}");
                return;
            }
            
            // Définir l'organisateur
            course.setOrganisateurId(user.getId());
            
            // Sauvegarder
            courseDao.save(course);
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), course);
            
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
        
        // Vérifier l'authentification
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null || (!"ORGANISATEUR".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de course requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int courseId = Integer.parseInt(pathParts[1]);
            
            // Vérifier que la course existe
            Course existingCourse = courseDao.findById(courseId);
            if (existingCourse == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Course non trouvée\"}");
                return;
            }
            
            // Vérifier les permissions
            if (!"ADMIN".equals(user.getRole()) && existingCourse.getOrganisateurId() != user.getId()) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Non autorisé à modifier cette course\"}");
                return;
            }
            
            // Lire le JSON du body
            Course course = objectMapper.readValue(req.getReader(), Course.class);
            course.setId(courseId);
            
            // Mettre à jour
            courseDao.update(course);
            
            objectMapper.writeValue(resp.getWriter(), course);
            
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
        
        // Vérifier l'authentification
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null || (!"ORGANISATEUR".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Non autorisé\"}");
            return;
        }
        
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de course requis\"}");
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            int courseId = Integer.parseInt(pathParts[1]);
            
            // Vérifier que la course existe
            Course course = courseDao.findById(courseId);
            if (course == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Course non trouvée\"}");
                return;
            }
            
            // Vérifier les permissions
            if (!"ADMIN".equals(user.getRole()) && course.getOrganisateurId() != user.getId()) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Non autorisé à supprimer cette course\"}");
                return;
            }
            
            // Supprimer
            courseDao.delete(courseId);
            
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