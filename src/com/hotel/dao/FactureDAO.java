package com.hotel.dao;

import com.hotel.model.Facture;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des Factures.
 * Permet de lister les facturations et de suivre les états de paiement.
 */
public class FactureDAO {

    private final Connection connection;

    /**
     * Initialise la connexion active.
     */
    public FactureDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Crée une nouvelle facture en base de données.
     * @param facture L'objet facture à insérer
     * @return true si l'ajout a réussi
     */
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
                // Récupération de l'ID auto-généré
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next())
                    facture.setIdFacture(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FactureDAO] Erreur ajouterFacture : " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère la liste exhaustive des factures avec les informations client et chambre.
     * @return Liste d'objets Facture triée par date
     */
    public List<Facture> getToutesLesFactures() {
        List<Facture> liste = new ArrayList<>();
        // Jointures multiples pour récupérer les informations de présentation (Nom, Numéro chambre)
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
            System.err.println("[FactureDAO] Erreur getToutesLesFactures : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère les factures selon leur état (PAYE, NON_PAYE, etc.).
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
            System.err.println("[FactureDAO] Erreur getFacturesParStatut : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Recherche une facture précise par son identifiant.
     */
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
            System.err.println("[FactureDAO] Erreur getFactureParId : " + e.getMessage());
        }
        return null;
    }

    /**
     * Calcule dynamiquement le montant total des paiements déjà enregistrés pour cette facture.
     */
    public double getMontantDejaPaye(int idFacture) {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM paiement WHERE id_facture = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[FactureDAO] Erreur getMontantDejaPaye : " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Agrégat pour le Dashboard : Somme des montants de toutes les factures payées.
     */
    public double getTotalRevenusPaies() {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM facture WHERE statut_paiement = 'PAYE'";
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[FactureDAO] Erreur getTotalRevenusPaies : " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Met à jour uniquement le statut de paiement d'une facture.
     */
    public boolean modifierStatut(int idFacture, String nouveauStatut) {
        String sql = "UPDATE facture SET statut_paiement = ? WHERE id_facture = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, idFacture);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FactureDAO] Erreur modifierStatut : " + e.getMessage());
        }
        return false;
    }

    /**
     * Supprime une facture.
     */
    public boolean supprimerFacture(int idFacture) {
        String sql = "DELETE FROM facture WHERE id_facture = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FactureDAO] Erreur supprimerFacture : " + e.getMessage());
        }
        return false;
    }

    /**
     * Transforme une ligne SQL ResultSet en objet Facture avec données client/chambre.
     */
    private Facture mapResultSet(ResultSet rs) throws SQLException {
        Facture f = new Facture(
                rs.getInt("id_facture"),
                rs.getDate("date_emission").toLocalDate(),
                rs.getDouble("montant_total"),
                rs.getString("statut_paiement"),
                rs.getInt("id_reservation"));
        // Remplissage des champs transient pour l'affichage
        f.setNomClient(rs.getString("prenom") + " " + rs.getString("nom"));
        f.setNumerosChambre("Chambre " + rs.getString("numero"));
        return f;
    }
}

