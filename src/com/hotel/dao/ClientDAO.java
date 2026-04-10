package com.hotel.dao;

import com.hotel.model.Client;
import com.hotel.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    private final Connection connection;

    public ClientDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

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
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) client.setIdClient(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ClientDAO] ajouterClient : " + e.getMessage());
        }
        return false;
    }

    public List<Client> getTousLesClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT id_client, nom, prenom, telephone, email, adresse, "
                   + "numero_carte_fidelite FROM client ORDER BY nom, prenom";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) clients.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("[ClientDAO] getTousLesClients : " + e.getMessage());
        }
        return clients;
    }

    public Client getClientParId(int idClient) {
        String sql = "SELECT id_client, nom, prenom, telephone, email, adresse, "
                   + "numero_carte_fidelite FROM client WHERE id_client = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[ClientDAO] getClientParId : " + e.getMessage());
        }
        return null;
    }

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
            System.err.println("[ClientDAO] rechercherClients : " + e.getMessage());
        }
        return clients;
    }

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
            System.err.println("[ClientDAO] modifierClient : " + e.getMessage());
        }
        return false;
    }

    public boolean supprimerClient(int idClient) {
        String sql = "DELETE FROM client WHERE id_client = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ClientDAO] supprimerClient : " + e.getMessage());
        }
        return false;
    }

    public boolean aDesReservations(int idClient) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE id_client = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[ClientDAO] aDesReservations : " + e.getMessage());
        }
        return false;
    }

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