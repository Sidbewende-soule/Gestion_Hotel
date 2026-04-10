package com.hotel.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.hotel.utils.DatabaseConnection;

/**
 * Data Access Object pour les types de chambres (Simple, Double, Suite, etc.).
 */
public class TypeChambreDAO {
	private Connection connection;

    /**
     * Constructeur : récupère la connexion active.
     */
    public TypeChambreDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Récupère la liste des libellés de tous les types de chambre.
     * Utile pour remplir les menus déroulants (ComboBox) dans les formulaires.
     * @return Liste de chaînes de caractères (les libellés)
     */
    public List<String> getLibellesTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT libelle FROM type_chambre";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                types.add(rs.getString("libelle"));
            }
        } catch (SQLException e) {
            System.err.println("[TypeChambreDAO] Erreur SQL : " + e.getMessage());
        }
        return types;
    }
}

