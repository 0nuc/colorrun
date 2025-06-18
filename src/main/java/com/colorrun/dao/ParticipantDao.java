package com.colorrun.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.colorrun.model.User;

public class ParticipantDao {
    private static final String DB_URL = "jdbc:h2:file:./colorrun;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public List<User> findParticipantsByCourseId(int courseId) {
        List<User> participants = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                "JOIN participants p ON u.id = p.user_id " +
                "WHERE p.course_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                participants.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    public boolean isUserParticipating(int userId, int courseId) {
        String sql = "SELECT COUNT(*) FROM participants WHERE user_id = ? AND course_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addParticipant(int courseId, int userId) {
        String sql = "INSERT INTO participants (course_id, user_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}