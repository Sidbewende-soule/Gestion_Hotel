package com.hotel.dao;

import com.hotel.model.Facture;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table `facture`.
 */
public class FactureDAO {

    private final Connection connection;

    public FactureDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ════════════════════════════════════════════════════════════════════════
    // CREATE
    // ════════════════════════════════════════════════════════════════════════

    public boolean ajouterFacture(Facture facture) {
        String sql = "INSERT INTO facture (date_emission, montant_total, statut_paiement, id_reservation) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(facture.getDateEmission()));
            ps.setDouble(2, facture.getMontantTotal());
            ps.setString(3, facture.getStatutPaiement());
            ps.setInt(4, facture.getIdReservation());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next())
                    facture.setIdFacture(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FactureDAO] ajouterFacture : " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    // READ
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retourne toutes les factures avec le nom du client (via JOIN).
     */
    public List<Facture> getToutesLesFactures() {
        List<Facture> liste = new ArrayList<>();
        String sql = "SELECT f.id_facture, f.date_emission, f.montant_total, "
                + "f.statut_paiement, f.id_reservation, "
                + "c.nom, c.prenom, ch.numero "
                + "FROM facture f "
                + "JOIN reservation r ON f.id_reservation = r.id_reservation "
                + "JOIN client c ON r.id_client = c.id_client "
                + "JOIN chambre ch ON r.id_chambre = ch.id_chambre "
                + "ORDER BY f.date_emission DESC";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[FactureDAO] getToutesLesFactures : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Retourne les factures filtrées par statut de paiement.
     * 
     * @param statut NON_PAYE, PARTIEL ou PAYE
     */
    public List<Facture> getFacturesParStatut(String statut) {
        List<Facture> liste = new ArrayList<>();
        String sql = "SELECT f.id_facture, f.date_emission, f.montant_total, "
                + "f.statut_paiement, f.id_reservation, "
                + "c.nom, c.prenom, ch.numero "
                + "FROM facture f "
                + "JOIN reservation r ON f.id_reservation = r.id_reservation "
                + "JOIN client c ON r.id_client = c.id_client "
                + "JOIN chambre ch ON r.id_chambre = ch.id_chambre "
                + "WHERE f.statut_paiement = ? "
                + "ORDER BY f.date_emission DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[FactureDAO] getFacturesParStatut : " + e.getMessage());
        }
        return liste;
    }

    public Facture getFactureParId(int idFacture) {
        String sql = "SELECT f.id_facture, f.date_emission, f.montant_total, "
                + "f.statut_paiement, f.id_reservation, "
                + "c.nom, c.prenom, ch.numero "
                + "FROM facture f "
                + "JOIN reservation r ON f.id_reservation = r.id_reservation "
                + "JOIN client c ON r.id_client = c.id_client "
                + "JOIN chambre ch ON r.id_chambre = ch.id_chambre "
                + "WHERE f.id_facture = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[FactureDAO] getFactureParId : " + e.getMessage());
        }
        return null;
    }

    /**
     * Calcule le total déjà payé pour une facture donnée.
     */
    public double getMontantDejaPaye(int idFacture) {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM paiement WHERE id_facture = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[FactureDAO] getMontantDejaPaye : " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Calcule le total de toutes les factures payées (pour le dashboard).
     */
    public double getTotalRevenusPaies() {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM facture WHERE statut_paiement = 'PAYE'";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[FactureDAO] getTotalRevenusPaies : " + e.getMessage());
        }
        return 0.0;
    }

    // ════════════════════════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════════════════════════

    public boolean modifierStatut(int idFacture, String nouveauStatut) {
        String sql = "UPDATE facture SET statut_paiement = ? WHERE id_facture = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, idFacture);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FactureDAO] modifierStatut : " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════════════════════════

    public boolean supprimerFacture(int idFacture) {
        String sql = "DELETE FROM facture WHERE id_facture = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FactureDAO] supprimerFacture : " + e.getMessage());
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Utilitaire privé
    // ════════════════════════════════════════════════════════════════════════

    private Facture mapResultSet(ResultSet rs) throws SQLException {
        Facture f = new Facture(
                rs.getInt("id_facture"),
                rs.getDate("date_emission").toLocalDate(),
                rs.getDouble("montant_total"),
                rs.getString("statut_paiement"),
                rs.getInt("id_reservation"));
        f.setNomClient(rs.getString("prenom") + " " + rs.getString("nom"));
        f.setNumerosChambre("Chambre " + rs.getString("numero"));
        return f;
    }
}
