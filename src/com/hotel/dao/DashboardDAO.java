package com.hotel.dao;

import com.hotel.model.ReservationResume;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO dédié aux statistiques du tableau de bord.
 */
public class DashboardDAO {

    private final Connection connection;

    public DashboardDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ════════════════════════════════════════════════════════════════════════
    // KPI — Réservations actives
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retourne le nombre de réservations actuellement actives
     * (date_debut <= aujourd'hui <= date_fin).
     */
    public int getNombreReservationsActives() {
        String sql = "SELECT COUNT(*) FROM reservation "
                   + "WHERE date_debut <= CURDATE() AND date_fin >= CURDATE()";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getNombreReservationsActives : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retourne le nombre total de réservations (tous statuts).
     */
    public int getNombreTotalReservations() {
        String sql = "SELECT COUNT(*) FROM reservation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getNombreTotalReservations : " + e.getMessage());
        }
        return 0;
    }

    // ════════════════════════════════════════════════════════════════════════
    // KPI — Chambres et taux d'occupation
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retourne le nombre total de chambres dans l'hôtel.
     */
    public int getNombreChambresTotal() {
        String sql = "SELECT COUNT(*) FROM chambre";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getNombreChambresTotal : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retourne le nombre de chambres occupées aujourd'hui.
     */
    public int getNombreChambresOccupees() {
        String sql = "SELECT COUNT(DISTINCT id_chambre) FROM reservation "
                   + "WHERE date_debut <= CURDATE() AND date_fin >= CURDATE()";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getNombreChambresOccupees : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retourne le taux d'occupation en pourcentage (0.0 – 100.0).
     */
    public double getTauxOccupation() {
        int total = getNombreChambresTotal();
        if (total == 0) return 0.0;
        return (getNombreChambresOccupees() * 100.0) / total;
    }

    // ════════════════════════════════════════════════════════════════════════
    // KPI — Revenus
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Total des revenus encaissés (factures au statut PAYE).
     */
    public double getTotalRevenus() {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM facture WHERE statut_paiement = 'PAYE'";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getTotalRevenus : " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Revenus mensuels pour une année donnée.
     * Retourne une Map ordonnée mois (1-12) → montant.
     */
    public Map<Integer, Double> getRevenusParMois(int annee) {
        Map<Integer, Double> map = new LinkedHashMap<>();
        // Initialiser les 12 mois à 0
        for (int m = 1; m <= 12; m++) map.put(m, 0.0);

        String sql = "SELECT MONTH(date_emission) AS mois, COALESCE(SUM(montant_total), 0) AS total "
                   + "FROM facture "
                   + "WHERE YEAR(date_emission) = ? AND statut_paiement = 'PAYE' "
                   + "GROUP BY MONTH(date_emission) "
                   + "ORDER BY mois";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("mois"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getRevenusParMois : " + e.getMessage());
        }
        return map;
    }

    // ════════════════════════════════════════════════════════════════════════
    // KPI — Satisfaction
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Note de satisfaction moyenne (sur 5) depuis la table `retour`.
     */
    public double getNoteSatisfactionMoyenne() {
        String sql = "SELECT COALESCE(AVG(satisfaction), 0) FROM retour";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getNoteSatisfactionMoyenne : " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Nombre total d'avis clients.
     */
    public int getNombreAvis() {
        String sql = "SELECT COUNT(*) FROM retour";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getNombreAvis : " + e.getMessage());
        }
        return 0;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Réservations récentes
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retourne les N dernières réservations avec les infos client et chambre.
     */
    public List<ReservationResume> getReservationsRecentes(int limit) {
        List<ReservationResume> liste = new ArrayList<>();
        String sql = "SELECT r.id_reservation, c.prenom, c.nom, ch.numero, tc.libelle AS type_chambre, "
                   + "r.date_debut, r.date_fin, r.statut "
                   + "FROM reservation r "
                   + "JOIN client c ON r.id_client = c.id_client "
                   + "JOIN chambre ch ON r.id_chambre = ch.id_chambre "
                   + "JOIN type_chambre tc ON ch.id_type = tc.id_type "
                   + "ORDER BY r.id_reservation DESC "
                   + "LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ReservationResume rr = new ReservationResume(
                    rs.getInt("id_reservation"),
                    rs.getString("prenom") + " " + rs.getString("nom"),
                    "Ch." + rs.getString("numero") + " — " + rs.getString("type_chambre"),
                    rs.getDate("date_debut") != null ? rs.getDate("date_debut").toLocalDate() : null,
                    rs.getDate("date_fin")   != null ? rs.getDate("date_fin").toLocalDate()   : null,
                    rs.getString("statut")
                );
                liste.add(rr);
            }
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] getReservationsRecentes : " + e.getMessage());
        }
        return liste;
    }
}
