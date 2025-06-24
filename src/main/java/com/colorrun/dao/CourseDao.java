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

public class CourseDao {
    private static final String DB_URL = "jdbc:h2:file:./colorrun;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
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
        try {
            // Vérifie si les tables existent
            try (Connection conn = getConnection();
                 ResultSet rs = conn.getMetaData().getTables(null, null, "USERS", null)) {
                if (!rs.next()) {
                    // Les tables n'existent pas, on les crée
                    createTables(conn);
                    insertTestData(conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Table users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "profile_picture VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Table course
            stmt.execute("CREATE TABLE IF NOT EXISTS course (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nom VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "date_heure TIMESTAMP NOT NULL, " +
                    "lieu VARCHAR(255) NOT NULL, " +
                    "distance INT NOT NULL, " +
                    "max_participants INT NOT NULL, " +
                    "prix DECIMAL(10,2) NOT NULL, " +
                    "avec_obstacles BOOLEAN DEFAULT FALSE, " +
                    "cause_soutenue VARCHAR(200), " +
                    "organisateur_id INT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (organisateur_id) REFERENCES users(id)" +
                    ")");

            // Ajout colonne organisateur_id si elle n'existe pas déjà
            try {
                stmt.execute("ALTER TABLE course ADD COLUMN organisateur_id INT");
            } catch (SQLException e) {
                // Ignore si déjà existante
            }

            // Table participants
            stmt.execute("CREATE TABLE IF NOT EXISTS participants (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "course_id INT NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT unique_participation UNIQUE(course_id, user_id), " +
                    "FOREIGN KEY (course_id) REFERENCES course(id), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")");
        }
    }

    private void insertTestData(Connection conn) throws SQLException {
        // Vérifie si la table users est vide
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insère les utilisateurs de test
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)")) {
                    
                    // Admin
                    pstmt.setString(1, "Admin");
                    pstmt.setString(2, "Admin");
                    pstmt.setString(3, "admin@colorrun.com");
                    pstmt.setString(4, "admin123"); // En production, il faudrait hasher le mot de passe
                    pstmt.setString(5, "ADMIN");
                    pstmt.executeUpdate();

                    // Participant
                    pstmt.setString(1, "Dupont");
                    pstmt.setString(2, "Jean");
                    pstmt.setString(3, "jean@example.com");
                    pstmt.setString(4, "jean123");
                    pstmt.setString(5, "PARTICIPANT");
                    pstmt.executeUpdate();

                    // Organisateur
                    pstmt.setString(1, "Martin");
                    pstmt.setString(2, "Sophie");
                    pstmt.setString(3, "sophie@example.com");
                    pstmt.setString(4, "sophie123");
                    pstmt.setString(5, "ORGANISATEUR");
                    pstmt.executeUpdate();
                }
            }
        }

        // Vérifie si la table course est vide
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM course")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Insère les données de test
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO course (nom, description, date_heure, lieu, distance, max_participants, prix, avec_obstacles, cause_soutenue) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    
                    // Course 1
                    pstmt.setString(1, "ColorRun Paris");
                    pstmt.setString(2, "Course colorée dans les rues de Paris");
                    pstmt.setTimestamp(3, java.sql.Timestamp.valueOf("2025-06-15 10:00:00"));
                    pstmt.setString(4, "Paris");
                    pstmt.setInt(5, 5);
                    pstmt.setInt(6, 1000);
                    pstmt.setDouble(7, 35.0);
                    pstmt.setBoolean(8, true);
                    pstmt.setString(9, "Association A");
                    pstmt.executeUpdate();

                    // Course 2
                    pstmt.setString(1, "ColorRun Lyon");
                    pstmt.setString(2, "Course colorée dans les rues de Lyon");
                    pstmt.setTimestamp(3, java.sql.Timestamp.valueOf("2025-07-20 09:00:00"));
                    pstmt.setString(4, "Lyon");
                    pstmt.setInt(5, 10);
                    pstmt.setInt(6, 800);
                    pstmt.setDouble(7, 40.0);
                    pstmt.setBoolean(8, false);
                    pstmt.setString(9, "Association B");
                    pstmt.executeUpdate();

                    // Course 3
                    pstmt.setString(1, "ColorRun Marseille");
                    pstmt.setString(2, "Course colorée sur la plage de Marseille");
                    pstmt.setTimestamp(3, java.sql.Timestamp.valueOf("2025-08-05 08:00:00"));
                    pstmt.setString(4, "Marseille");
                    pstmt.setInt(5, 7);
                    pstmt.setInt(6, 1200);
                    pstmt.setDouble(7, 30.0);
                    pstmt.setBoolean(8, true);
                    pstmt.setString(9, "Association C");
                    pstmt.executeUpdate();
                }
            }
        }
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
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM course WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
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