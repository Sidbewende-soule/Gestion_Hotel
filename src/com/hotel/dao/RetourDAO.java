package com.hotel.dao;

import com.hotel.model.Retour;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des Avis/Retours clients.
 */
public class RetourDAO {

    private final Connection connection;

    /**
     * Constructeur : récupère la connexion active de la base de données.
     */
    public RetourDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Ajoute un nouvel avis client en base de données.
     * @param retour L'objet Retour contenant les données à insérer
     * @return true si l'ajout a réussi
     */
    public boolean ajouterRetour(Retour retour) {
        String sql = "INSERT INTO retour (date_retour, commentaire, satisfaction, id_client) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(retour.getDateRetour()));
            ps.setString(2, retour.getCommentaire());
            ps.setInt(3, retour.getSatisfaction());
            ps.setInt(4, retour.getIdClient());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Récupération de l'ID auto-incrémenté généré par MySQL
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next())
                    retour.setIdRetour(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[RetourDAO] Erreur ajouterRetour : " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère la liste des avis d'un client spécifique, triés par date décroissante.
     * @param idClient L'identifiant du client
     * @return Liste d'objets Retour
     */
    public List<Retour> getRetoursParClient(int idClient) {
        List<Retour> liste = new ArrayList<>();
        String sql = "SELECT r.id_retour, r.date_retour, r.commentaire, r.satisfaction, "
                + "r.id_client, c.nom, c.prenom "
                + "FROM retour r JOIN client c ON r.id_client = c.id_client "
                + "WHERE r.id_client = ? ORDER BY r.date_retour DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[RetourDAO] Erreur getRetoursParClient : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Supprime un avis de la base de données.
     * @param idRetour L'ID de l'avis à supprimer
     * @return true si la suppression a réussi
     */
    public boolean supprimerRetour(int idRetour) {
        String sql = "DELETE FROM retour WHERE id_retour = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idRetour);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RetourDAO] Erreur supprimerRetour : " + e.getMessage());
        }
        return false;
    }

    /**
     * Utilitaire pour transformer une ligne de résultat SQL en objet Java Retour.
     */
    private Retour mapResultSet(ResultSet rs) throws SQLException {
        Retour r = new Retour(
                rs.getInt("id_retour"),
                rs.getDate("date_retour").toLocalDate(),
                rs.getString("commentaire"),
                rs.getInt("satisfaction"),
                rs.getInt("id_client"));
        // On remplit le champ transient pour l'affichage UI
        r.setNomClient(rs.getString("prenom") + " " + rs.getString("nom"));
        return r;
    }
}