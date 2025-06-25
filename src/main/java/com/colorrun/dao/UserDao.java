package com.colorrun.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.colorrun.model.User;
import com.colorrun.util.DatabaseInitializer;

public class UserDao {
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

    public UserDao() {
        // Initialiser la base de données une seule fois
        DatabaseInitializer.getInstance().initializeDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void update(User user) {
        try (Connection conn = getConnection()) {
            // Vérifie d'abord si la colonne verification_token existe
            boolean hasVerificationToken = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "USERS", "VERIFICATION_TOKEN")) {
                hasVerificationToken = rs.next();
            }
            
            String sql;
            if (hasVerificationToken) {
                sql = "UPDATE users SET first_name=?, last_name=?, verification_token=? WHERE id=?";
            } else {
                sql = "UPDATE users SET first_name=?, last_name=? WHERE id=?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
                if (hasVerificationToken) {
            ps.setString(3, user.getVerificationToken());
            ps.setInt(4, user.getId());
                } else {
                    ps.setInt(3, user.getId());
                }
            ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User findByEmail(String email) {
        System.out.println("Recherche de l'utilisateur avec l'email: " + email);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email);
            System.out.println("Exécution de la requête SQL: " + ps.toString());
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                try { user.setPhone(rs.getString("phone")); } catch (SQLException ignore) {}
                try { user.setVerificationToken(rs.getString("verification_token")); } catch (SQLException ignore) {}
                    try { user.setVerified(rs.getBoolean("verified")); } catch (SQLException ignore) {}
                    System.out.println("Utilisateur trouvé: " + user.getEmail() + " avec l'ID: " + user.getId() + " et le rôle: " + user.getRole());
                return user;
                } else {
                    System.out.println("Aucun utilisateur trouvé pour l'email: " + email);
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save(User user) {
        try (Connection conn = getConnection()) {
            // Vérifier si la colonne verified existe
            boolean verifiedColumnExists = false;
            try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, "USERS", "VERIFIED")) {
                verifiedColumnExists = rs.next();
            }
            
            String sql;
            if (verifiedColumnExists) {
                sql = "INSERT INTO users(first_name, last_name, email, password, role, verified, verification_token) VALUES (?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "INSERT INTO users(first_name, last_name, email, password, role, verification_token) VALUES (?, ?, ?, ?, ?, ?)";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
                
                if (verifiedColumnExists) {
                    ps.setBoolean(6, user.isVerified());
                    ps.setString(7, user.getVerificationToken());
                } else {
                    ps.setString(6, user.getVerificationToken());
                }
                
            ps.executeUpdate();
            }
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
                // Champs optionnels
                try { u.setAddress(rs.getString("address")); } catch (SQLException ignore) {}
                try { u.setPostalCode(rs.getString("postal_code")); } catch (SQLException ignore) {}
                try { u.setCity(rs.getString("city")); } catch (SQLException ignore) {}
                try { u.setNewsletter(rs.getBoolean("newsletter")); } catch (SQLException ignore) {}
                try { u.setProfilePicture(rs.getString("profile_picture")); } catch (SQLException ignore) {}
                try { u.setPhone(rs.getString("phone")); } catch (SQLException ignore) {}
                try { u.setVerificationToken(rs.getString("verification_token")); } catch (SQLException ignore) {}
                try { u.setVerified(rs.getBoolean("verified")); } catch (SQLException ignore) {}
                return Optional.of(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserRole(int userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY last_name, first_name";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User findByVerificationToken(String token) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE verification_token = ?")) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setVerificationToken(rs.getString("verification_token"));
                try { user.setVerified(rs.getBoolean("verified")); } catch (SQLException ignore) {}
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUserWithToken(User user) {
        String sql = "UPDATE users SET verification_token = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getVerificationToken());
            ps.setInt(2, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearVerificationToken(int userId) {
        String sql = "UPDATE users SET verification_token = NULL WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void verifyUser(int userId) {
        String sql = "UPDATE users SET verified = TRUE WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int userId) {
        try (Connection conn = getConnection()) {
            // Désactiver l'auto-commit pour gérer la transaction
            conn.setAutoCommit(false);
            try {
                // Supprimer d'abord les messages de l'utilisateur
                String deleteMessagesSql = "DELETE FROM messages WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteMessagesSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                
                // Supprimer les participations de l'utilisateur
                String deleteParticipantsSql = "DELETE FROM participants WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteParticipantsSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                
                // Supprimer les demandes d'organisateur de l'utilisateur
                String deleteRequestsSql = "DELETE FROM organizer_requests WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteRequestsSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                
                // Enfin, supprimer l'utilisateur
                String deleteUserSql = "DELETE FROM users WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteUserSql)) {
                    ps.setInt(1, userId);
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
}