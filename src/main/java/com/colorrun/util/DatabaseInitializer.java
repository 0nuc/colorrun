package com.colorrun.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:h2:file:./colorrun2;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";
    
    private static DatabaseInitializer instance;
    private static boolean initialized = false;
    
    private DatabaseInitializer() {}
    
    public static synchronized DatabaseInitializer getInstance() {
        if (instance == null) {
            instance = new DatabaseInitializer();
        }
        return instance;
    }
    
    public synchronized void initializeDatabase() {
        if (initialized) {
            return; // Déjà initialisé
        }
        
        try {
            System.out.println("=== INITIALISATION DE LA BASE DE DONNÉES ===");
            
            try (Connection conn = getConnection()) {
                if (!SqlLoader.tableExists(conn, "USERS")) {
                    // Les tables n'existent pas, on les crée
                    SqlLoader.executeSqlFile(conn, "sql/schema.sql");
                    SqlLoader.executeSqlFile(conn, "sql/data.sql");
                    System.out.println("=== BASE DE DONNÉES CRÉÉE ===");
                } else {
                    // Les tables existent, on ajoute les colonnes manquantes
                    addMissingColumns(conn);
                }
                
                // Force l'ajout de la colonne verification_token
                forceAddVerificationTokenColumn(conn);
                
                // Force l'ajout de la colonne verified
                forceAddVerifiedColumn(conn);
                
                // Marquer tous les comptes existants comme vérifiés
                markExistingAccountsAsVerified(conn);
                
                // Vérifier et corriger les utilisateurs orphelins
                checkAndFixOrphanUsers(conn);
                
                // TOUJOURS nettoyer les doublons de courses
                cleanupDuplicateCourses(conn);
            }
            
            initialized = true;
            System.out.println("=== BASE DE DONNÉES INITIALISÉE ===");
            
        } catch (SQLException | java.io.IOException e) {
            System.err.println("Erreur lors de l'initialisation de la base :");
            e.printStackTrace();
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
    
    private void addMissingColumns(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Ajouter les colonnes manquantes si elles n'existent pas
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN verification_token VARCHAR(255)");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN address VARCHAR(255)");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN postal_code VARCHAR(20)");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN city VARCHAR(100)");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN newsletter BOOLEAN DEFAULT FALSE");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN profile_picture VARCHAR(255)");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN phone VARCHAR(30)");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN verified BOOLEAN DEFAULT FALSE");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
        }
    }
    
    private void cleanupDuplicateCourses(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Compter le nombre de courses
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM course");
            rs.next();
            int count = rs.getInt(1);
            
            System.out.println("=== NETTOYAGE DES DOUBLONS DE COURSES ===");
            System.out.println("Nombre de courses avant nettoyage: " + count);
            
            // Supprimer seulement les VRAIS doublons (même nom, lieu, date)
            // Garder seulement la première occurrence de chaque course unique
            String deleteDuplicatesSQL = 
                "DELETE FROM course WHERE id NOT IN (" +
                "SELECT MIN(id) FROM course " +
                "GROUP BY nom, lieu, date_heure)";
            
            int deletedRows = stmt.executeUpdate(deleteDuplicatesSQL);
            
            if (deletedRows > 0) {
                System.out.println("Nombre de doublons supprimés: " + deletedRows);
                
                // Nettoyer les participants orphelins (qui référencent des courses supprimées)
                String deleteOrphanParticipantsSQL = 
                    "DELETE FROM participants WHERE course_id NOT IN (SELECT id FROM course)";
                int orphanParticipantsDeleted = stmt.executeUpdate(deleteOrphanParticipantsSQL);
                if (orphanParticipantsDeleted > 0) {
                    System.out.println("Participants orphelins supprimés: " + orphanParticipantsDeleted);
                }
                
                // Nettoyer les messages orphelins
                String deleteOrphanMessagesSQL = 
                    "DELETE FROM messages WHERE course_id NOT IN (SELECT id FROM course)";
                int orphanMessagesDeleted = stmt.executeUpdate(deleteOrphanMessagesSQL);
                if (orphanMessagesDeleted > 0) {
                    System.out.println("Messages orphelins supprimés: " + orphanMessagesDeleted);
                }
                
                // Réinitialiser l'auto-increment de la table course
                try {
                    stmt.execute("ALTER TABLE course ALTER COLUMN id RESTART WITH 1");
                    System.out.println("Auto-increment de la table course réinitialisé");
                } catch (SQLException e) {
                    System.out.println("Impossible de réinitialiser l'auto-increment: " + e.getMessage());
                }
                
                // Recompter
                rs = stmt.executeQuery("SELECT COUNT(*) FROM course");
                rs.next();
                int newCount = rs.getInt(1);
                System.out.println("Nombre de courses après nettoyage: " + newCount);
            } else {
                System.out.println("Aucun doublon trouvé, toutes les courses sont uniques");
            }
            System.out.println("=== NETTOYAGE TERMINÉ ===");
        }
    }
    
    private void forceAddVerificationTokenColumn(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Ajouter la colonne verification_token si elle n'existe pas
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN verification_token VARCHAR(255)");
                System.out.println("Colonne verification_token ajoutée");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
        }
    }
    
    private void forceAddVerifiedColumn(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Ajouter la colonne verified si elle n'existe pas
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN verified BOOLEAN DEFAULT FALSE");
                System.out.println("Colonne verified ajoutée");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore
            }
        }
    }
    
    private void markExistingAccountsAsVerified(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Marquer tous les comptes existants comme vérifiés
            stmt.execute("UPDATE users SET verified = TRUE");
            System.out.println("Tous les comptes existants sont maintenant vérifiés");
        }
    }
    
    private void checkAndFixOrphanUsers(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Lister tous les utilisateurs avec leurs IDs
            try (ResultSet rs = stmt.executeQuery("SELECT id, email, first_name, last_name FROM users ORDER BY id")) {
                System.out.println("=== UTILISATEURS EN BASE ===");
                int userCount = 0;
                while (rs.next()) {
                    userCount++;
                    int id = rs.getInt("id");
                    String email = rs.getString("email");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    System.out.println("ID: " + id + " - " + firstName + " " + lastName + " (" + email + ")");
                }
                
                // Réinitialiser l'auto-increment seulement s'il n'y a pas d'utilisateurs
                if (userCount == 0) {
                    try {
                        stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
                        System.out.println("Auto-increment de la table users réinitialisé (table vide)");
                    } catch (SQLException e) {
                        System.out.println("Impossible de réinitialiser l'auto-increment users: " + e.getMessage());
                    }
                } else {
                    System.out.println("Auto-increment de la table users conservé (" + userCount + " utilisateurs existants)");
                }
            }
        }
    }
} 