package com.colorrun.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlLoader {
    
    /**
     * Charge et exécute un fichier SQL depuis les ressources
     * @param conn La connexion à la base de données
     * @param filePath Le chemin vers le fichier SQL dans les ressources
     * @throws SQLException En cas d'erreur SQL
     * @throws IOException En cas d'erreur de lecture du fichier
     */
    public static void executeSqlFile(Connection conn, String filePath) throws SQLException, IOException {
        System.out.println("Exécution du fichier SQL: " + filePath);
        
        try (InputStream is = SqlLoader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Ignorer les commentaires et lignes vides
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                
                sql.append(line).append(" ");
                
                // Si la ligne se termine par ';', exécuter la requête
                if (line.trim().endsWith(";")) {
                    String query = sql.toString().trim();
                    if (!query.isEmpty()) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(query);
                            System.out.println("Requête exécutée avec succès: " + query.substring(0, Math.min(50, query.length())) + "...");
                        } catch (SQLException e) {
                            // Ignorer toutes les erreurs (doublons, contraintes, etc.)
                            System.out.println("Données déjà existantes, ignoré: " + query.substring(0, Math.min(50, query.length())) + "...");
                        }
                    }
                    sql.setLength(0); // Reset pour la prochaine requête
                }
            }
        }
    }
    
    /**
     * Charge les requêtes SQL depuis un fichier de ressources
     * @param resourcePath Le chemin vers le fichier SQL
     * @return Liste des requêtes SQL
     * @throws IOException En cas d'erreur de lecture
     */
    public static List<String> loadSqlStatements(String resourcePath) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        
        try (InputStream is = SqlLoader.class.getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignore les lignes de commentaires
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                
                currentStatement.append(line).append(" ");
                
                // Si la ligne se termine par ';', c'est la fin d'une requête
                if (line.trim().endsWith(";")) {
                    statements.add(currentStatement.toString().trim());
                    currentStatement = new StringBuilder();
                }
            }
            
            // Ajoute la dernière requête si elle n'a pas de point-virgule
            if (currentStatement.length() > 0) {
                statements.add(currentStatement.toString().trim());
            }
        }
        
        return statements;
    }
    
    /**
     * Vérifie si une table existe dans la base de données
     * @param connection La connexion à la base de données
     * @param tableName Le nom de la table à vérifier
     * @return true si la table existe, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (var rs = connection.getMetaData().getTables(null, null, tableName.toUpperCase(), null)) {
            return rs.next();
        }
    }
    
    /**
     * Vérifie si une table est vide
     * @param connection La connexion à la base de données
     * @param tableName Le nom de la table à vérifier
     * @return true si la table est vide, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public static boolean tableIsEmpty(Connection connection, String tableName) throws SQLException {
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }
}