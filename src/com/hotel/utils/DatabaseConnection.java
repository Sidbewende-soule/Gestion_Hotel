package com.hotel.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaire pour gérer la connexion à la base de données MySQL.
 * Utilise le pattern Singleton pour garantir une seule connexion partagée.
 */
public class DatabaseConnection {

    // Paramètres de connexion (URL, utilisateur, mot de passe)
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_hotel"
            + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Laisser vide par défaut pour XAMPP/WAMP

    private static DatabaseConnection instance; // Unique instance du Singleton
    private static Connection connection; // Objet de connexion JDBC

    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     * Charge le driver et établit la connexion initiale.
     */
    private DatabaseConnection() {
        try {
            // Chargement du driver JDBC MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Tentative de connexion
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connexion établie avec succès.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Impossible de se connecter à la base : " + e.getMessage());
        }
    }

    /**
     * Fournit l'unique instance de DatabaseConnection.
     * Recrée la connexion si celle-ci a été fermée ou est nulle.
     * @return L'instance unique gérant la connexion
     */
    public static synchronized DatabaseConnection getInstance() {
        try {
            if (instance == null || connection == null || connection.isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Retourne l'objet Connection actif.
     * @return L'objet de connexion SQL
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Ferme proprement la connexion à la base de données.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connexion fermée.");
            } catch (SQLException e) {
                System.err.println("[DB] Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }
}