package com.colorrun.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.colorrun.model.OrganizerRequest;

public class OrganizerRequestDao {

    private static final String DB_URL = "jdbc:h2:file:./colorrun2;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void create(OrganizerRequest request) {
        String sql = "INSERT INTO organizer_requests (user_id, motivation, status) VALUES (?, ?, ?)";
        System.out.println("OrganizerRequestDao.create() - Début");
        System.out.println("OrganizerRequestDao.create() - UserId: " + request.getUserId());
        System.out.println("OrganizerRequestDao.create() - Motivation: " + request.getMotivation());
        System.out.println("OrganizerRequestDao.create() - Status: " + request.getStatus());
        
        try (Connection conn = getConnection()) {
            // Vérifier d'abord que l'utilisateur existe
            String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkUserSql)) {
                checkPs.setInt(1, request.getUserId());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("OrganizerRequestDao.create() - ERREUR: Utilisateur avec l'ID " + request.getUserId() + " n'existe pas dans la table users");
                        throw new RuntimeException("Utilisateur avec l'ID " + request.getUserId() + " n'existe pas");
                    }
                }
            }
            System.out.println("OrganizerRequestDao.create() - Utilisateur " + request.getUserId() + " trouvé en base");
            
            // Insérer la demande
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, request.getUserId());
                ps.setString(2, request.getMotivation());
                ps.setString(3, request.getStatus());
                ps.executeUpdate();
                System.out.println("OrganizerRequestDao.create() - Demande créée avec succès");
            }
        } catch (SQLException e) {
            System.out.println("OrganizerRequestDao.create() - ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de la demande d'organisateur", e);
        }
    }

    public List<OrganizerRequest> findAll() {
        List<OrganizerRequest> requests = new ArrayList<>();
        // Jointure pour récupérer aussi les infos de l'utilisateur
        String sql = "SELECT r.*, u.first_name, u.last_name, u.email FROM organizer_requests r JOIN users u ON r.user_id = u.id ORDER BY r.request_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                OrganizerRequest request = new OrganizerRequest();
                request.setId(rs.getInt("id"));
                request.setUserId(rs.getInt("user_id"));
                request.setMotivation(rs.getString("motivation"));
                request.setStatus(rs.getString("status"));
                request.setRequestDate(rs.getTimestamp("request_date").toLocalDateTime());
                // Infos de l'utilisateur
                request.setUserFirstName(rs.getString("first_name"));
                request.setUserLastName(rs.getString("last_name"));
                request.setUserEmail(rs.getString("email"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public void updateStatus(int requestId, String status) {
        String sql = "UPDATE organizer_requests SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(OrganizerRequest request) {
        create(request);
    }

    public OrganizerRequest findById(int id) {
        String sql = "SELECT r.*, u.first_name, u.last_name, u.email FROM organizer_requests r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "WHERE r.id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrganizerRequest request = new OrganizerRequest();
                    request.setId(rs.getInt("id"));
                    request.setUserId(rs.getInt("user_id"));
                    request.setMotivation(rs.getString("motivation"));
                    request.setStatus(rs.getString("status"));
                    request.setRequestDate(rs.getTimestamp("request_date").toLocalDateTime());
                    request.setUserFirstName(rs.getString("first_name"));
                    request.setUserLastName(rs.getString("last_name"));
                    request.setUserEmail(rs.getString("email"));
                    return request;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(OrganizerRequest request) {
        String sql = "UPDATE organizer_requests SET motivation = ?, status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, request.getMotivation());
            ps.setString(2, request.getStatus());
            ps.setInt(3, request.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM organizer_requests WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPendingRequest(int userId) {
        String sql = "SELECT COUNT(*) FROM organizer_requests WHERE user_id = ? AND status = 'EN_ATTENTE'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 