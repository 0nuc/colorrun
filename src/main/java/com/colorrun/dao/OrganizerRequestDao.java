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

    private static final String DB_URL = "jdbc:h2:file:./colorrun;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void create(OrganizerRequest request) {
        String sql = "INSERT INTO organizer_requests (user_id, motivation, status) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, request.getUserId());
            ps.setString(2, request.getMotivation());
            ps.setString(3, request.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
} 