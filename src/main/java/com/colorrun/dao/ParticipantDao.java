package com.colorrun.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.colorrun.model.Participant;

public class ParticipantDao {
    private static final String DB_URL = "jdbc:h2:file:./colorrun;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public List<Participant> findByCourseId(int courseId) {
        List<Participant> participants = new ArrayList<>();
        String sql = "SELECT p.*, u.first_name, u.last_name FROM participants p " +
                    "JOIN users u ON p.user_id = u.id " +
                    "WHERE p.course_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Participant participant = new Participant();
                participant.setId(rs.getInt("id"));
                participant.setCourseId(rs.getInt("course_id"));
                participant.setUserId(rs.getInt("user_id"));
                participant.setNom(rs.getString("last_name"));
                participant.setPrenom(rs.getString("first_name"));
                participants.add(participant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    public boolean isUserRegistered(int courseId, int userId) {
        String sql = "SELECT COUNT(*) FROM participants WHERE course_id = ? AND user_id = ?";
        System.out.println("ParticipantDao.isUserRegistered() - Début");
        System.out.println("ParticipantDao.isUserRegistered() - SQL: " + sql);
        System.out.println("ParticipantDao.isUserRegistered() - CourseId: " + courseId);
        System.out.println("ParticipantDao.isUserRegistered() - UserId: " + userId);
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean isRegistered = rs.getInt(1) > 0;
                System.out.println("ParticipantDao.isUserRegistered() - Résultat: " + isRegistered);
                return isRegistered;
            }
        } catch (SQLException e) {
            System.out.println("ParticipantDao.isUserRegistered() - Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Participant add(Participant participant) {
        String sql = "INSERT INTO participants (course_id, user_id) VALUES (?, ?)";
        System.out.println("ParticipantDao.add() - Début");
        System.out.println("ParticipantDao.add() - SQL: " + sql);
        System.out.println("ParticipantDao.add() - CourseId: " + participant.getCourseId());
        System.out.println("ParticipantDao.add() - UserId: " + participant.getUserId());

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, participant.getCourseId());
            stmt.setInt(2, participant.getUserId());
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        participant.setId(generatedKeys.getInt(1));
                    }
                }
            }
            System.out.println("ParticipantDao.add() - Lignes affectées: " + rowsAffected);
            return participant;
        } catch (SQLException e) {
            System.out.println("ParticipantDao.add() - Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 