package com.hotel.dao;

import com.hotel.model.Paiement;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table `paiement`.
 * Note : le trigger `update_statut_facture` de la base met à jour
 * automatiquement le statut de la facture après chaque INSERT.
 */
public class PaiementDAO {

    private final Connection connection;

    public PaiementDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ════════════════════════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Enregistre un paiement.
     * Le trigger SQL met automatiquement à jour le statut de la facture.
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
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next())
                    paiement.setIdPaiement(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[PaiementDAO] ajouterPaiement : " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    // READ
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retourne tous les paiements d'une facture donnée.
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
            System.err.println("[PaiementDAO] getPaiementsParFacture : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Retourne tous les paiements (tous clients confondus).
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
            System.err.println("[PaiementDAO] getTousLesPaiements : " + e.getMessage());
        }
        return liste;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════════════════════════

    public boolean supprimerPaiement(int idPaiement) {
        String sql = "DELETE FROM paiement WHERE id_paiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPaiement);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PaiementDAO] supprimerPaiement : " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Utilitaire privé
    // ════════════════════════════════════════════════════════════════════════

    private Paiement mapResultSet(ResultSet rs) throws SQLException {
        return new Paiement(
                rs.getInt("id_paiement"),
                rs.getDate("date_paiement").toLocalDate(),
                rs.getDouble("montant"),
                rs.getString("mode_paiement"),
                rs.getInt("id_facture"));
    }
}