package com.colorrun.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.colorrun.model.Course;
import com.colorrun.util.DatabaseInitializer;

public class CourseDao {
    private static final String DB_URL = "jdbc:h2:file:./colorrun2;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public CourseDao() {
        // Initialiser la base de données une seule fois
        DatabaseInitializer.getInstance().initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM course ORDER BY date_heure")) {
            
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setNom(rs.getString("nom"));
                course.setDescription(rs.getString("description"));
                course.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                course.setLieu(rs.getString("lieu"));
                course.setDistance(rs.getInt("distance"));
                course.setMaxParticipants(rs.getInt("max_participants"));
                course.setPrix(rs.getDouble("prix"));
                course.setAvecObstacles(rs.getBoolean("avec_obstacles"));
                course.setCauseSoutenue(rs.getString("cause_soutenue"));
                course.setOrganisateurId(rs.getInt("organisateur_id"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public List<Course> findWithFilters(String date, String ville, String distance, String tri) {
        List<Course> courses = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM course WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (date != null && !date.isEmpty()) {
            sql.append(" AND DATE(date_heure) = ?");
            params.add(date);
        }

        if (ville != null && !ville.isEmpty()) {
            sql.append(" AND LOWER(lieu) LIKE ?");
            params.add("%" + ville.toLowerCase() + "%");
        }

        if (distance != null && !distance.isEmpty()) {
            sql.append(" AND distance <= ?");
            params.add(Integer.parseInt(distance));
        }

        if (tri != null && !tri.isEmpty()) {
            switch (tri) {
                case "date":
                    sql.append(" ORDER BY date_heure");
                    break;
                case "ville":
                    sql.append(" ORDER BY lieu");
                    break;
                case "distance":
                    sql.append(" ORDER BY distance");
                    break;
            }
        } else {
            sql.append(" ORDER BY date_heure");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setNom(rs.getString("nom"));
                course.setDescription(rs.getString("description"));
                course.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                course.setLieu(rs.getString("lieu"));
                course.setDistance(rs.getInt("distance"));
                course.setMaxParticipants(rs.getInt("max_participants"));
                course.setPrix(rs.getDouble("prix"));
                course.setAvecObstacles(rs.getBoolean("avec_obstacles"));
                course.setCauseSoutenue(rs.getString("cause_soutenue"));
                course.setOrganisateurId(rs.getInt("organisateur_id"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public List<Course> findCoursesByUserId(int userId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM course c " +
                "JOIN participants p ON c.id = p.course_id " +
                "WHERE p.user_id = ? ORDER BY c.date_heure DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setNom(rs.getString("nom"));
                course.setDescription(rs.getString("description"));
                course.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                course.setLieu(rs.getString("lieu"));
                course.setDistance(rs.getInt("distance"));
                course.setMaxParticipants(rs.getInt("max_participants"));
                course.setPrix(rs.getDouble("prix"));
                course.setAvecObstacles(rs.getBoolean("avec_obstacles"));
                course.setCauseSoutenue(rs.getString("cause_soutenue"));
                course.setOrganisateurId(rs.getInt("organisateur_id"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public Course findById(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM course WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setNom(rs.getString("nom"));
                course.setDescription(rs.getString("description"));
                course.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                course.setLieu(rs.getString("lieu"));
                course.setDistance(rs.getInt("distance"));
                course.setMaxParticipants(rs.getInt("max_participants"));
                course.setPrix(rs.getDouble("prix"));
                course.setAvecObstacles(rs.getBoolean("avec_obstacles"));
                course.setCauseSoutenue(rs.getString("cause_soutenue"));
                course.setOrganisateurId(rs.getInt("organisateur_id"));
                return course;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(Course course) {
        System.out.println("CourseDao.save() - Début");
        System.out.println("CourseDao.save() - Course à sauvegarder:");
        System.out.println("  - Nom: " + course.getNom());
        System.out.println("  - Description: " + course.getDescription());
        System.out.println("  - Date: " + course.getDateHeure());
        System.out.println("  - Lieu: " + course.getLieu());
        System.out.println("  - Distance: " + course.getDistance());
        System.out.println("  - MaxParticipants: " + course.getMaxParticipants());
        System.out.println("  - Prix: " + course.getPrix());
        System.out.println("  - AvecObstacles: " + course.isAvecObstacles());
        System.out.println("  - CauseSoutenue: " + course.getCauseSoutenue());
        System.out.println("  - OrganisateurId: " + course.getOrganisateurId());
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO course(nom, description, date_heure, lieu, distance, max_participants, prix, avec_obstacles, cause_soutenue, organisateur_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getNom());
            ps.setString(2, course.getDescription());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(course.getDateHeure()));
            ps.setString(4, course.getLieu());
            ps.setInt(5, course.getDistance());
            ps.setInt(6, course.getMaxParticipants());
            ps.setDouble(7, course.getPrix());
            ps.setBoolean(8, course.isAvecObstacles());
            ps.setString(9, course.getCauseSoutenue());
            ps.setInt(10, course.getOrganisateurId());
            
            System.out.println("CourseDao.save() - Exécution de l'INSERT...");
            int result = ps.executeUpdate();
            System.out.println("CourseDao.save() - INSERT exécuté, lignes affectées: " + result);
            
            // Récupérer l'ID généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    course.setId(id);
                    System.out.println("CourseDao.save() - ID généré: " + id);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("CourseDao.save() - ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("CourseDao.save() - Fin");
    }

    public void delete(int id) {
        try (Connection conn = getConnection()) {
            // Désactiver l'auto-commit pour gérer la transaction
            conn.setAutoCommit(false);
            try {
                // Supprimer d'abord les messages de la course
                String deleteMessagesSql = "DELETE FROM messages WHERE course_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteMessagesSql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // Supprimer les participants de la course
                String deleteParticipantsSql = "DELETE FROM participants WHERE course_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteParticipantsSql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // Enfin, supprimer la course
                String deleteCourseSql = "DELETE FROM course WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteCourseSql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                
                // Valider la transaction
                conn.commit();
            } catch (SQLException e) {
                // En cas d'erreur, annuler la transaction
                conn.rollback();
                throw e;
            } finally {
                // Réactiver l'auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Course course) {
        String sql = "UPDATE course SET nom=?, description=?, date_heure=?, lieu=?, distance=?, max_participants=?, prix=?, avec_obstacles=?, cause_soutenue=?, organisateur_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getNom());
            ps.setString(2, course.getDescription());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(course.getDateHeure()));
            ps.setString(4, course.getLieu());
            ps.setInt(5, course.getDistance());
            ps.setInt(6, course.getMaxParticipants());
            ps.setDouble(7, course.getPrix());
            ps.setBoolean(8, course.isAvecObstacles());
            ps.setString(9, course.getCauseSoutenue());
            ps.setInt(10, course.getOrganisateurId());
            ps.setInt(11, course.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Course> findLastCourses(int limit) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course ORDER BY date_heure DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setNom(rs.getString("nom"));
                course.setDescription(rs.getString("description"));
                course.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                course.setLieu(rs.getString("lieu"));
                course.setDistance(rs.getInt("distance"));
                course.setMaxParticipants(rs.getInt("max_participants"));
                course.setPrix(rs.getDouble("prix"));
                course.setAvecObstacles(rs.getBoolean("avec_obstacles"));
                course.setCauseSoutenue(rs.getString("cause_soutenue"));
                course.setOrganisateurId(rs.getInt("organisateur_id"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public List<Course> findRandomCourses(int limit) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course ORDER BY RAND() LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setId(rs.getInt("id"));
                course.setNom(rs.getString("nom"));
                course.setDescription(rs.getString("description"));
                course.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                course.setLieu(rs.getString("lieu"));
                course.setDistance(rs.getInt("distance"));
                course.setMaxParticipants(rs.getInt("max_participants"));
                course.setPrix(rs.getDouble("prix"));
                course.setAvecObstacles(rs.getBoolean("avec_obstacles"));
                course.setCauseSoutenue(rs.getString("cause_soutenue"));
                course.setOrganisateurId(rs.getInt("organisateur_id"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
}