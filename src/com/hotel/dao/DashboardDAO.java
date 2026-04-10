package com.hotel.dao;

import com.hotel.model.ReservationResume;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object spécialisé pour le calcul des indicateurs de performance (KPI).
 * Regroupe les requêtes d'agrégation SQL pour le tableau de bord.
 */
public class DashboardDAO {

    private final Connection connection;

    /**
     * Récupère l'instance unique de connexion.
     */
    public DashboardDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Compte les réservations en cours à la date d'aujourd'hui.
     */
    public int getNombreReservationsActives() {
        String sql = "SELECT COUNT(*) FROM reservation "
                   + "WHERE date_debut <= CURDATE() AND date_fin >= CURDATE()";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erreur getNombreReservationsActives : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Compte le volume total de réservations historique.
     */
    public int getNombreTotalReservations() {
        String sql = "SELECT COUNT(*) FROM reservation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erreur getNombreTotalReservations : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retourne la capacité totale de l'hôtel en nombre de chambres.
     */
    public int getNombreChambresTotal() {
        String sql = "SELECT COUNT(*) FROM chambre";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erreur getNombreChambresTotal : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retourne le nombre de chambres physiquement occupées ce jour.
     */
    public int getNombreChambresOccupees() {
        String sql = "SELECT COUNT(DISTINCT id_chambre) FROM reservation "
                   + "WHERE date_debut <= CURDATE() AND date_fin >= CURDATE()";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erreur getNombreChambresOccupees : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Calcul métier du taux d'occupation (Occupation / Capacité).
     */
    public double getTauxOccupation() {
        int total = getNombreChambresTotal();
        if (total == 0) return 0.0;
        return (getNombreChambresOccupees() * 100.0) / total;
    }

    /**
     * Somme des revenus générés par les factures payées.
     */
    public double getTotalRevenus() {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM facture WHERE statut_paiement = 'PAYE'";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erreur getTotalRevenus : " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Récupère la répartition du chiffre d'affaires par mois pour l'année en cours.
     * @param annee L'année de référence (ex: 2024)
     * @return Map ordonnée (Janvier -> Décembre) avec les montants
     */
    public Map<Integer, Double> getRevenusParMois(int annee) {
        Map<Integer, Double> map = new LinkedHashMap<>();
        // Initialisation de la map pour garantir que tous les mois sont présents à 0
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
            System.err.println("[DashboardDAO] Erreur getRevenusParMois : " + e.getMessage());
        }
        return map;
    }

    /**
     * Calcul de la satisfaction client moyenne.
     */
    public double getNoteSatisfactionMoyenne() {
        String sql = "SELECT COALESCE(AVG(satisfaction), 0) FROM retour";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erreur getNoteSatisfactionMoyenne : " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Volume d'avis clients collectés.
     */
    public int getNombreAvis() {
        String sql = "SELECT COUNT(*) FROM retour";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Erreur getNombreAvis : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Récupère les dernières réservations pour le tableau récapitulatif du Dashboard.
     * @param limit Nombre maximum de lignes à retourner
     */
    public List<ReservationResume> getReservationsRecentes(int limit) {
        List<ReservationResume> liste = new ArrayList<>();
        // Jointure pour avoir des libellés lisibles par l'humain au lieu des IDs techniques
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
            System.err.println("[DashboardDAO] Erreur getReservationsRecentes : " + e.getMessage());
        }
        return liste;
    }
}

