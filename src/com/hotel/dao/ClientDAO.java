package com.hotel.dao;

import com.hotel.model.Client;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des Clients.
 * Fournit les méthodes CRUD (Create, Read, Update, Delete) pour la table `client`.
 */
public class ClientDAO {

    private final Connection connection;

    /**
     * Initialise la connexion via le Singleton DatabaseConnection.
     */
    public ClientDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Insère un nouveau client en base de données.
     * @param client L'objet client à persister
     * @return true si l'opération a réussi
     */
    public boolean ajouterClient(Client client) {
        String sql = "INSERT INTO client (nom, prenom, telephone, email, adresse, numero_carte_fidelite) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getTelephone());
            ps.setString(4, client.getEmail());
            ps.setString(5, client.getAdresse());
            ps.setString(6, client.getNumeroCarteFidelite());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Récupération de l'ID généré automatiquement
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) client.setIdClient(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Erreur ajouterClient : " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère la liste de tous les clients enregistrés.
     * @return Liste d'objets Client triée par nom
     */
    public List<Client> getTousLesClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT id_client, nom, prenom, telephone, email, adresse, "
                   + "numero_carte_fidelite FROM client ORDER BY nom, prenom";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) clients.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Erreur getTousLesClients : " + e.getMessage());
        }
        return clients;
    }

    /**
     * Recherche un client par son ID unique.
     * @param idClient L'ID à rechercher
     * @return L'objet Client ou null si non trouvé
     */
    public Client getClientParId(int idClient) {
        String sql = "SELECT id_client, nom, prenom, telephone, email, adresse, "
                   + "numero_carte_fidelite FROM client WHERE id_client = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Erreur getClientParId : " + e.getMessage());
        }
        return null;
    }

    /**
     * Recherche textuelle multicritère (nom, prénom ou email).
     * @param motCle Le texte à rechercher
     * @return Liste des clients correspondants
     */
    public List<Client> rechercherClients(String motCle) {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT id_client, nom, prenom, telephone, email, adresse, "
                   + "numero_carte_fidelite FROM client "
                   + "WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ? "
                   + "ORDER BY nom, prenom";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String p = "%" + motCle.toLowerCase() + "%";
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) clients.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Erreur rechercherClients : " + e.getMessage());
        }
        return clients;
    }

    /**
     * Met à jour les informations d'un client existant.
     * @param client L'objet client avec les nouvelles valeurs
     * @return true si la mise à jour a réussi
     */
    public boolean modifierClient(Client client) {
        String sql = "UPDATE client SET nom=?, prenom=?, telephone=?, email=?, "
                   + "adresse=?, numero_carte_fidelite=? WHERE id_client=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getTelephone());
            ps.setString(4, client.getEmail());
            ps.setString(5, client.getAdresse());
            ps.setString(6, client.getNumeroCarteFidelite());
            ps.setInt(7, client.getIdClient());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Erreur modifierClient : " + e.getMessage());
        }
        return false;
    }

    /**
     * Supprime un client définitivement (si autorisé par les contraintes d'intégrité).
     */
    public boolean supprimerClient(int idClient) {
        String sql = "DELETE FROM client WHERE id_client = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Erreur supprimerClient : " + e.getMessage());
        }
        return false;
    }

    /**
     * Vérifie si un client possède des réservations actives ou passées.
     * Utile avant une suppression pour éviter les erreurs de clé étrangère.
     */
    public boolean aDesReservations(int idClient) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_client = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[ClientDAO] Erreur aDesReservations : " + e.getMessage());
        }
        return false;
    }

    /**
     * Utilitaire pour transformer une ligne ResultSet en objet Client.
     */
    private Client mapResultSet(ResultSet rs) throws SQLException {
        return new Client(
            rs.getInt("id_client"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("telephone"),
            rs.getString("email"),
            rs.getString("adresse"),
            rs.getString("numero_carte_fidelite")
        );
    }
}