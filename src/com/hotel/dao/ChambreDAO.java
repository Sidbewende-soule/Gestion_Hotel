package com.hotel.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.hotel.model.Chambre;
import com.hotel.model.Employe;
import com.hotel.utils.DatabaseConnection;

/**
 * Data Access Object gérant les opérations sur les Chambres.
 */
public class ChambreDAO {
    private Connection connection;

    /**
     * Initialise la connexion active.
     */
    public ChambreDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Récupère la liste des chambres ayant le statut 'DISPONIBLE'.
     * @return Liste d'objets Chambre
     */
    public List<Chambre> listerChambresDisponibles() {
        List<Chambre> liste = new ArrayList<>();
        String sql = "SELECT * FROM chambre WHERE etat = 'DISPONIBLE'";

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Chambre ch = new Chambre(
                        rs.getInt("id_chambre"),
                        rs.getString("numero"),
                        rs.getString("etat"),
                        rs.getDouble("prix_par_nuit"),
                        rs.getInt("id_type"));
                liste.add(ch);
            }
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur listerChambresDisponibles : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Variante pour remplir une ComboBox affichant "Numéro - Type".
     * @return Liste de chaînes formattées
     */
    public List<String> listerChambresDisponiblesAvecType() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT c.numero, t.libelle FROM chambre c " +
                "JOIN type_chambre t ON c.id_type = t.id_type " +
                "WHERE c.etat = 'DISPONIBLE'";

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(rs.getString("numero") + " - " + rs.getString("libelle"));
            }
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur listerChambresDisponiblesAvecType : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Ajoute une nouvelle chambre en base de données.
     */
    public boolean ajouterChambre(String numero, String etat, double prix, int idType) {
        String sql = "INSERT INTO chambre (numero, etat, prix_par_nuit, id_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, numero);
            pst.setString(2, etat);
            pst.setDouble(3, prix);
            pst.setInt(4, idType);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur ajouterChambre : " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère toutes les chambres avec le nom de l'employé assigné (si présent).
     * @return Liste d'objets Chambre avec jointures
     */
    public List<Chambre> listerToutesLesChambres() {
        List<Chambre> liste = new ArrayList<>();
        // Jointure avec la table employe pour obtenir le nom du personnel assigné
        String sql = "SELECT c.*, e.nom as nom_employe FROM chambre c " +
                "LEFT JOIN employe e ON c.id_employe = e.id_employe";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Chambre ch = new Chambre(rs.getInt("id_chambre"), rs.getString("numero"),
                        rs.getString("etat"), rs.getDouble("prix_par_nuit"), rs.getInt("id_type"));

                // On injecte le nom de l'employé dans la propriété du modèle pour affichage
                ch.nomPersonnelProperty()
                        .set(rs.getString("nom_employe") != null ? rs.getString("nom_employe") : "Aucun");
                liste.add(ch);
            }
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur listerToutesLesChambres : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère la liste de tout le personnel (pour les formulaires d'assignation).
     */
    public List<Employe> listerEmployes() {
        List<Employe> liste = new ArrayList<>();
        String sql = "SELECT id_employe, nom, role FROM employe";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Employe(rs.getInt("id_employe"), rs.getString("nom"), rs.getString("role")));
            }
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur listerEmployes : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Met à jour l'état d'une chambre et son personnel assigné.
     */
    public boolean mettreAJourStatut(int idChambre, String nouvelEtat, Integer idEmploye) {
        String sql = "UPDATE chambre SET etat = ?, id_employe = ? WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, nouvelEtat);
            if (idEmploye != null) {
                pst.setInt(2, idEmploye);
            } else {
                pst.setNull(2, Types.INTEGER);
            }
            pst.setInt(3, idChambre);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur mettreAJourStatut : " + e.getMessage());
            return false;
        }
    }

    /**
     * Remet l'état de la chambre à 'DISPONIBLE' et retire l'employé assigné.
     */
    public boolean libererChambre(int idChambre) {
        String sql = "UPDATE chambre SET etat = 'DISPONIBLE', id_employe = NULL WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idChambre);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur libererChambre : " + e.getMessage());
            return false;
        }
    }

    /**
     * Modifie les informations structurelles d'une chambre.
     */
    public boolean modifierChambre(int id, String numero, String etat, double prix, int idType) {
        String sql = "UPDATE chambre SET numero = ?, etat = ?, prix_par_nuit = ?, id_type = ? WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, numero);
            pst.setString(2, etat);
            pst.setDouble(3, prix);
            pst.setInt(4, idType);
            pst.setInt(5, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur modifierChambre : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime une chambre de la base.
     */
    public boolean supprimerChambre(int id) {
        String sql = "DELETE FROM chambre WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChambreDAO] Erreur supprimerChambre : " + e.getMessage());
            return false;
        }
    }
}