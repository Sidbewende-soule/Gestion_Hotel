package com.hotel.controller;

import com.hotel.dao.ReservationDAO;
import com.hotel.dao.ChambreDAO;
import com.hotel.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    // --- Composants FXML ---
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> cmbChambre; 
    @FXML private ListView<CheckBox> listViewDemandes;
    @FXML private ComboBox<String> cmbClient;
    @FXML private TextArea txtNotes;

    // --- DAO ---
    private ReservationDAO reservationDAO = new ReservationDAO();
    private ChambreDAO chambreDAO = new ChambreDAO();
    private com.hotel.dao.ClientDAO clientDAO = new com.hotel.dao.ClientDAO();

    private String convertirIdTypeEnNom(int idType) {
        switch (idType) {
            case 1: return "Simple";
            case 2: return "Double";
            case 3: return "Suite";
            default: return "Inconnu";
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbChambre.getItems().clear();
        listViewDemandes.getItems().clear();
        
        // CORRECTION : On ne liste désormais QUE les chambres disponibles
        List<Chambre> listeDisponibles = chambreDAO.listerChambresDisponibles();
        
        for (Chambre c : listeDisponibles) {
            String nomType = convertirIdTypeEnNom(c.getIdType());
            String display = c.getNumero() + " (" + nomType + ")";
            cmbChambre.getItems().add(display);
        }

        // Chargement des clients
        cmbClient.getItems().clear();
        List<Client> listeClients = clientDAO.getTousLesClients();
        for (Client cli : listeClients) {
            cmbClient.getItems().add(cli.getIdClient() + " - " + cli.getNom() + " " + cli.getPrenom());
        }

        // Chargement des demandes spéciales
        List<DemandeSpeciale> listeDemandes = reservationDAO.listerToutesLesDemandes();
        for (DemandeSpeciale d : listeDemandes) {
            CheckBox cb = new CheckBox(d.getNomDemande());
            cb.setUserData(d.getId());
            listViewDemandes.getItems().add(cb);
        }
    }

    @FXML
    public void ouvrirGestionReservations() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/hotel/view/GestionReservations.fxml"));
            Parent root = fxmlLoader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestion des réservations");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de gestion.");
        }
    }

    @FXML
    public void validerReservation() {
        try {
            if (cmbChambre.getValue() == null || cmbClient.getValue() == null || dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
                afficherAlerte(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
                return;
            }

            String display = cmbChambre.getValue();
            String numChambre = display.contains(" ") ? display.split(" ")[0] : display;
            
            int idChambre = reservationDAO.trouverIdParNumero(numChambre);
            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();
            
            String displayClient = cmbClient.getValue();
            int idClient = Integer.parseInt(displayClient.split(" - ")[0]);

            if (idChambre == -1) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "La chambre " + numChambre + " est introuvable.");
                return;
            }
            
            String etat = reservationDAO.getEtatChambre(idChambre);
            if (!"DISPONIBLE".equalsIgnoreCase(etat)) {
                afficherAlerte(Alert.AlertType.ERROR, "Indisponible", "La chambre " + numChambre + " est actuellement : " + etat + ".");
                return;
            }

            if (reservationDAO.estChambreOccupee(idChambre, debut, fin, 0)) {
                afficherAlerte(Alert.AlertType.ERROR, "Indisponible", "La chambre " + numChambre + " est déjà réservée sur cette période.");
                return;
            }

            String notes = txtNotes.getText();

            Reservation res = new Reservation(0, debut, fin, "EN_ATTENTE", idClient, idChambre, notes);
            if (reservationDAO.ajouterReservation(res)) {
                for (CheckBox cb : listViewDemandes.getItems()) {
                    if (cb.isSelected()) {
                        reservationDAO.ajouterDemandeALaReservation(res.getId(), (int) cb.getUserData());
                    }
                }
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Réservation de la chambre " + numChambre + " enregistrée.");
                
                // On vide COMPLÈTEMENT le formulaire
                cmbClient.setValue(null);
                txtNotes.clear();
                dateDebutPicker.setValue(null);
                dateFinPicker.setValue(null);
                
                initialize(null, null);
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement.");
            }
        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "L'ID Client doit être un nombre.");
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur système", e.getMessage());
        }
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}