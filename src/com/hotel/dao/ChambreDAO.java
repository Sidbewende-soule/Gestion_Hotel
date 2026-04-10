package com.hotel.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.hotel.model.Chambre;
import com.hotel.model.Employe;
import com.hotel.utils.DatabaseConnection;

public class ChambreDAO {
    private Connection connection;

    public ChambreDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // --- Ancienne méthode conservée par sécurité ---
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
            e.printStackTrace();
        }
        return liste;
    }

    // --- NOUVELLE MÉTHODE POUR LA COMBOBOX (Filtrée et formatée) ---
    public List<String> listerChambresDisponiblesAvecType() {
        List<String> liste = new ArrayList<>();

        // Bloc de débogage ajouté
        System.out.println("DEBUG : Vérification des états dans la table...");
        String sqlCheck = "SELECT numero, etat FROM chambre";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sqlCheck)) {
            while (rs.next()) {
                System.out
                        .println("Chambre " + rs.getString("numero") + " est en état : '" + rs.getString("etat") + "'");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Requête principale
        String sql = "SELECT c.numero, t.libelle FROM chambre c " +
                "JOIN type_chambre t ON c.id_type = t.id_type " +
                "WHERE c.etat = 'DISPONIBLE'";

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(rs.getString("numero") + " - " + rs.getString("libelle"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du listage : " + e.getMessage());
            e.printStackTrace();
        }
        return liste;
    }

    public boolean ajouterChambre(String numero, String etat, double prix, int idType) {
        String sql = "INSERT INTO chambre (numero, etat, prix_par_nuit, id_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, numero);
            pst.setString(2, etat);
            pst.setDouble(3, prix);
            pst.setInt(4, idType);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Chambre> listerToutesLesChambres() {
        List<Chambre> liste = new ArrayList<>();
        String sql = "SELECT c.*, e.nom as nom_employe FROM chambre c " +
                "LEFT JOIN employe e ON c.id_employe = e.id_employe";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Chambre ch = new Chambre(rs.getInt("id_chambre"), rs.getString("numero"),
                        rs.getString("etat"), rs.getDouble("prix_par_nuit"), rs.getInt("id_type"));

                ch.nomPersonnelProperty()
                        .set(rs.getString("nom_employe") != null ? rs.getString("nom_employe") : "Aucun");
                liste.add(ch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public List<Employe> listerEmployes() {
        List<Employe> liste = new ArrayList<>();
        String sql = "SELECT id_employe, nom, role FROM employe";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Employe(rs.getInt("id_employe"), rs.getString("nom"), rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

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
            e.printStackTrace();
            return false;
        }
    }

    public boolean libererChambre(int idChambre) {
        String sql = "UPDATE chambre SET etat = 'DISPONIBLE', id_employe = NULL WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idChambre);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimerChambre(int id) {
        String sql = "DELETE FROM chambre WHERE id_chambre = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}