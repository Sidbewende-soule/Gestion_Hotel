package com.hotel.dao;

import com.hotel.model.Utilisateur;
import com.hotel.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object pour la gestion des Utilisateurs.
 * Gère principalement la vérification des identifiants (Login).
 */
public class UtilisateurDAO {

    private final Connection connection;

    /**
     * Initialise la connexion à la base de données.
     */
    public UtilisateurDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Vérifie les identifiants en base de données et retourne l'utilisateur si trouvé.
     * @param username Le nom d'utilisateur (ou email)
     * @param password Le mot de passe saisi
     * @return Un objet Utilisateur si succès, sinon null
     */
    public Utilisateur authentifier(String username, String password) {
        // --- Mode de secours (Offline/Test) ---
        // Si la base est inaccessible, on permet des comptes de test par défaut
        if (connection == null) {
            System.err.println("[AUTH] Mode hors-ligne activé pour test. Utilisation de comptes fictifs.");
            if ("admin".equals(username) && "admin".equals(password)) {
                return new Utilisateur(1, "Administrateur", "admin", "admin", "Admin");
            } else if ("reception".equals(username) && "reception".equals(password)) {
                return new Utilisateur(2, "Réceptionniste", "reception", "reception", "Receptionniste");
            } else if ("direction".equals(username) && "direction".equals(password)) {
                return new Utilisateur(3, "Directeur", "direction", "direction", "Direction");
            }
            return null;
        }

        // --- Authentification via Base de Données ---
        String sql = "SELECT * FROM utilisateur WHERE nom_utilisateur = ? AND mot_de_passe = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Construction de l'objet utilisateur à partir des colonnes SQL
                    return new Utilisateur(
                            rs.getInt("id_utilisateur"),
                            rs.getString("nom") + " " + rs.getString("prenom"),
                            rs.getString("nom_utilisateur"),
                            rs.getString("mot_de_passe"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[UtilisateurDAO] Erreur d'authentification SQL : " + e.getMessage());
        }
        return null;
    }
}

