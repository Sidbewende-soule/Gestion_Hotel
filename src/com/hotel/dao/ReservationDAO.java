package com.hotel.dao;

import com.hotel.model.*;
import com.hotel.utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private Connection connection;

    public ReservationDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean existe(String table, String colonne, int id) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + colonne + " = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // --- NOUVEAU : Récupère la liste des IDs des options cochées pour une réservation ---
    public List<Integer> getOptionsPourReservation(int idReservation) {
        List<Integer> listeOptions = new ArrayList<>();
        String sql = "SELECT id_demande FROM reservation_demande WHERE id_reservation = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idReservation);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                listeOptions.add(rs.getInt("id_demande"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return listeOptions;
    }

    // --- NOUVEAU : Récupère l'état textuel exact de la chambre ---
    public String getEtatChambre(int idChambre) {
        String sql = "SELECT etat FROM chambre WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idChambre);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getString("etat");
        } catch (SQLException e) { e.printStackTrace(); }
        return "INCONNUE";
    }

    // --- NOUVEAU : Vérifie le chevauchement des réservations ---
    public boolean estChambreOccupee(int idChambre, LocalDate debut, LocalDate fin, int idReservationExclue) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_chambre = ? " +
                     "AND id_reservation != ? " + 
                     "AND date_debut <= ? AND date_fin >= ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idChambre);
            pst.setInt(2, idReservationExclue);
            pst.setDate(3, Date.valueOf(fin));
            pst.setDate(4, Date.valueOf(debut));
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int trouverIdParNumero(String numero) {
        String sql = "SELECT id_chambre FROM chambre WHERE numero = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, numero);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("id_chambre");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean ajouterReservation(Reservation r) {
        if (!existe("client", "id_client", r.getIdClient())) return false;
        if (!existe("chambre", "id_chambre", r.getIdChambre())) return false;

        String sql = "INSERT INTO reservation (date_debut, date_fin, statut, id_client, id_chambre, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setDate(1, Date.valueOf(r.getDateDebut()));
            pst.setDate(2, Date.valueOf(r.getDateFin()));
            pst.setString(3, r.getStatut());
            pst.setInt(4, r.getIdClient());
            pst.setInt(5, r.getIdChambre());
            pst.setString(6, r.getNotes());
            if (pst.executeUpdate() > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) { r.setId(generatedKeys.getInt(1)); return true; }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void ajouterDemandeALaReservation(int idReservation, int idDemande) {
        String sql = "INSERT INTO reservation_demande (id_reservation, id_demande, statut) VALUES (?, ?, 'EN_ATTENTE')";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idReservation);
            pst.setInt(2, idDemande);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void supprimerDemandesAssociees(int idReservation) {
        String sql = "DELETE FROM reservation_demande WHERE id_reservation = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idReservation);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<DemandeSpeciale> listerToutesLesDemandes() {
        List<DemandeSpeciale> liste = new ArrayList<>();
        String sql = "SELECT * FROM demande_speciale";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                liste.add(new DemandeSpeciale(rs.getInt("id_demande"), rs.getString("nom_demande"), rs.getString("description")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public List<Reservation> listerToutesLesReservations() {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT r.*, c.nom, ch.numero " +
                     "FROM reservation r " +
                     "JOIN client c ON r.id_client = c.id_client " +
                     "JOIN chambre ch ON r.id_chambre = ch.id_chambre";
        
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Reservation r = new Reservation(
                    rs.getInt("id_reservation"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getString("statut"),
                    rs.getInt("id_client"),
                    rs.getInt("id_chambre"),
                    rs.getString("notes")
                );
                r.setNomClient(rs.getString("nom"));
                r.setNumeroChambre(rs.getString("numero"));
                liste.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public boolean annulerReservation(int idReservation) {
        int idChambre = -1;
        String sqlSelect = "SELECT id_chambre FROM reservation WHERE id_reservation = ?";
        
        try (PreparedStatement pstSelect = connection.prepareStatement(sqlSelect)) {
            pstSelect.setInt(1, idReservation);
            ResultSet rs = pstSelect.executeQuery();
            if (rs.next()) idChambre = rs.getInt("id_chambre");
        } catch (SQLException e) { e.printStackTrace(); }

        supprimerDemandesAssociees(idReservation);
        String sqlDelete = "DELETE FROM reservation WHERE id_reservation = ?";
        
        try (PreparedStatement pstDelete = connection.prepareStatement(sqlDelete)) {
            pstDelete.setInt(1, idReservation);
            if (pstDelete.executeUpdate() > 0) {
                if (idChambre != -1) libererChambre(idChambre);
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private void libererChambre(int idChambre) {
        String sql = "UPDATE chambre SET etat = 'DISPONIBLE', id_employe = NULL WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idChambre);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean modifierReservation(Reservation r) {
        String sql = "UPDATE reservation SET date_debut = ?, date_fin = ?, id_chambre = ?, notes = ? WHERE id_reservation = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(r.getDateDebut()));
            pst.setDate(2, Date.valueOf(r.getDateFin()));
            pst.setInt(3, r.getIdChambre());
            pst.setString(4, r.getNotes());
            pst.setInt(5, r.getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}