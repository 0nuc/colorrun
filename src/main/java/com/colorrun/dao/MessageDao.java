package com.colorrun.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.colorrun.model.Message;

public class MessageDao {

    private static final String DB_URL = "jdbc:h2:file:./colorrun2;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public List<Message> findByCourseId(int courseId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.first_name, u.last_name FROM messages m " +
                    "JOIN users u ON m.user_id = u.id " +
                    "WHERE m.course_id = ? " +
                    "ORDER BY m.date_heure DESC";
        
        System.out.println("MessageDao.findByCourseId() - Début pour courseId: " + courseId);
        System.out.println("MessageDao.findByCourseId() - SQL: " + sql);
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setCourseId(rs.getInt("course_id"));
                    message.setUserId(rs.getInt("user_id"));
                    message.setContenu(rs.getString("contenu"));
                    message.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                    message.setAuteur(rs.getString("first_name") + " " + rs.getString("last_name"));
                    messages.add(message);
                    
                    System.out.println("MessageDao.findByCourseId() - Message " + count + ":");
                    System.out.println("  - ID: " + message.getId());
                    System.out.println("  - Contenu: " + message.getContenu());
                    System.out.println("  - Auteur: " + message.getAuteur());
                    System.out.println("  - Date: " + message.getDateHeure());
                }
                System.out.println("MessageDao.findByCourseId() - Total messages trouvés: " + count);
            }
        } catch (SQLException e) {
            System.out.println("MessageDao.findByCourseId() - ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    public void create(Message message) {
        String sql = "INSERT INTO messages (course_id, user_id, contenu, date_heure) VALUES (?, ?, ?, ?)";
        System.out.println("MessageDao.create() - Début");
        System.out.println("MessageDao.create() - SQL: " + sql);
        System.out.println("MessageDao.create() - CourseId: " + message.getCourseId());
        System.out.println("MessageDao.create() - UserId: " + message.getUserId());
        System.out.println("MessageDao.create() - Contenu: " + message.getContenu());
        System.out.println("MessageDao.create() - DateHeure: " + message.getDateHeure());
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getCourseId());
            ps.setInt(2, message.getUserId());
            ps.setString(3, message.getContenu());
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(message.getDateHeure()));
            ps.executeUpdate();
            System.out.println("MessageDao.create() - Message inséré avec succès");
        } catch (SQLException e) {
            System.out.println("MessageDao.create() - ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean delete(int messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";
        System.out.println("MessageDao.delete() - Début pour messageId: " + messageId);
        System.out.println("MessageDao.delete() - SQL: " + sql);
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("MessageDao.delete() - Lignes affectées: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("MessageDao.delete() - ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Message findById(int messageId) {
        String sql = "SELECT m.*, u.first_name, u.last_name FROM messages m " +
                    "JOIN users u ON m.user_id = u.id " +
                    "WHERE m.id = ?";
        
        System.out.println("MessageDao.findById() - Début pour messageId: " + messageId);
        System.out.println("MessageDao.findById() - SQL: " + sql);
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setCourseId(rs.getInt("course_id"));
                    message.setUserId(rs.getInt("user_id"));
                    message.setContenu(rs.getString("contenu"));
                    message.setDateHeure(rs.getTimestamp("date_heure").toLocalDateTime());
                    message.setAuteur(rs.getString("first_name") + " " + rs.getString("last_name"));
                    
                    System.out.println("MessageDao.findById() - Message trouvé:");
                    System.out.println("  - ID: " + message.getId());
                    System.out.println("  - CourseId: " + message.getCourseId());
                    System.out.println("  - Contenu: " + message.getContenu());
                    System.out.println("  - Auteur: " + message.getAuteur());
                    
                    return message;
                }
            }
        } catch (SQLException e) {
            System.out.println("MessageDao.findById() - ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
} 