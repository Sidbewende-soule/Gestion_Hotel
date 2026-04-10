package com.hotel.dao;

import com.hotel.model.Paiement;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des Paiements.
 * Note : la base de données possède un trigger `update_statut_facture` qui met à jour
 * automatiquement l'état de la facture correspondante après chaque insertion de paiement.
 */
public class PaiementDAO {

    private final Connection connection;

    /**
     * Initialise la connexion active.
     */
    public PaiementDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Enregistre un nouveau paiement lié à une facture.
     * @param paiement L'objet paiement contenant le montant et le mode
     * @return true si l'insertion a réussi
     */
    public boolean ajouterPaiement(Paiement paiement) {
        String sql = "INSERT INTO paiement (date_paiement, montant, mode_paiement, id_facture) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(paiement.getDatePaiement()));
            ps.setDouble(2, paiement.getMontant());
            ps.setString(3, paiement.getModePaiement());
            ps.setInt(4, paiement.getIdFacture());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Récupération de l'ID généré
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next())
                    paiement.setIdPaiement(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[PaiementDAO] Erreur ajouterPaiement : " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère l'historique des paiements d'une facture.
     * @param idFacture L'identifiant de la facture
     * @return Liste ordonnée de paiements (du plus récent au plus ancien)
     */
    public List<Paiement> getPaiementsParFacture(int idFacture) {
        List<Paiement> liste = new ArrayList<>();
        String sql = "SELECT id_paiement, date_paiement, montant, mode_paiement, id_facture "
                + "FROM paiement WHERE id_facture = ? ORDER BY date_paiement DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[PaiementDAO] Erreur getPaiementsParFacture : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère tous les paiements enregistrés dans le système.
     */
    public List<Paiement> getTousLesPaiements() {
        List<Paiement> liste = new ArrayList<>();
        String sql = "SELECT id_paiement, date_paiement, montant, mode_paiement, id_facture "
                + "FROM paiement ORDER BY date_paiement DESC";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[PaiementDAO] Erreur getTousLesPaiements : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Supprime un paiement de la base.
     * Attention : Cela peut rendre le statut de la facture incohérent si le trigger SQL ne gère pas les DELETE.
     */
    public boolean supprimerPaiement(int idPaiement) {
        String sql = "DELETE FROM paiement WHERE id_paiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPaiement);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PaiementDAO] Erreur supprimerPaiement : " + e.getMessage());
        }
        return false;
    }

    /**
     * Utilitaire de mapping ResultSet vers objet Paiement.
     */
    private Paiement mapResultSet(ResultSet rs) throws SQLException {
        return new Paiement(
                rs.getInt("id_paiement"),
                rs.getDate("date_paiement").toLocalDate(),
                rs.getDouble("montant"),
                rs.getString("mode_paiement"),
                rs.getInt("id_facture"));
    }
}