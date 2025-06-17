package com.colorrun.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.colorrun.model.User;

public class UserDao {
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

    public UserDao() {
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

    public User findByEmail(String email) {
        System.out.println("Recherche de l'utilisateur avec l'email: " + email);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email);
            System.out.println("Exécution de la requête SQL: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                System.out.println("Utilisateur trouvé: " + user.getEmail() + " avec le rôle: " + user.getRole());
                return user;
            }
            System.out.println("Aucun utilisateur trouvé pour l'email: " + email);
        } catch (SQLException e) {
            System.out.println("Erreur SQL lors de la recherche de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void save(User user) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addUser(User u) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, u.getFirstName());
            ps.setString(2, u.getLastName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setString(5, u.getRole());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Optional<User> findByEmailAndPassword(String email, String pass) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM users WHERE email = ? AND password = ?")) {
            ps.setString(1, email.trim());
            ps.setString(2, pass.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                return Optional.of(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Table des utilisateurs
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL
                );
                
                CREATE TABLE IF NOT EXISTS course (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nom VARCHAR(100) NOT NULL,
                    description TEXT,
                    date_heure TIMESTAMP NOT NULL,
                    lieu VARCHAR(100) NOT NULL,
                    distance INT NOT NULL,
                    max_participants INT NOT NULL,
                    prix DECIMAL(10,2) NOT NULL,
                    avec_obstacles BOOLEAN DEFAULT FALSE,
                    cause_soutenue VARCHAR(200)
                );
                
                CREATE TABLE IF NOT EXISTS messages (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    course_id INT NOT NULL,
                    user_id INT NOT NULL,
                    contenu TEXT NOT NULL,
                    date_heure TIMESTAMP NOT NULL,
                    FOREIGN KEY (course_id) REFERENCES course(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                
                CREATE TABLE IF NOT EXISTS participants (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    course_id INT NOT NULL,
                    user_id INT NOT NULL,
                    FOREIGN KEY (course_id) REFERENCES course(id),
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    UNIQUE (course_id, user_id)
                );
            """);
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
} 