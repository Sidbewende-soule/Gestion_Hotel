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

/**
 * Contrôleur pour la création d'une nouvelle réservation (Reservation.fxml).
 * Gère le filtrage des chambres disponibles, la vérification des chevauchements de dates
 * et l'affectation des demandes spéciales.
 */
public class ReservationController implements Initializable {

    // --- Composants FXML ---
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> cmbChambre; 
    @FXML private ListView<CheckBox> listViewDemandes; // Liste des options (Petit dej, etc.)
    @FXML private ComboBox<String> cmbClient;
    @FXML private TextArea txtNotes;

    // --- DAOs ---
    private ReservationDAO reservationDAO = new ReservationDAO();
    private ChambreDAO chambreDAO = new ChambreDAO();
    private com.hotel.dao.ClientDAO clientDAO = new com.hotel.dao.ClientDAO();

    /**
     * Utilitaire interne pour rendre l'ID type lisible.
     */
    private String convertirIdTypeEnNom(int idType) {
        switch (idType) {
            case 1: return "Simple";
            case 2: return "Double";
            case 3: return "Suite";
            default: return "Inconnu";
        }
    }

    /**
     * Initialisation du formulaire : chargement des données de référence.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbChambre.getItems().clear();
        listViewDemandes.getItems().clear();
        
        // 1. Chargement des chambres "libres" (Statut DISPONIBLE)
        List<Chambre> listeDisponibles = chambreDAO.listerChambresDisponibles();
        for (Chambre c : listeDisponibles) {
            String nomType = convertirIdTypeEnNom(c.getIdType());
            String display = c.getNumero() + " (" + nomType + ")";
            cmbChambre.getItems().add(display);
        }

        // 2. Chargement de la liste des clients inscrits
        cmbClient.getItems().clear();
        List<Client> listeClients = clientDAO.getTousLesClients();
        for (Client cli : listeClients) {
            // Format ID - Nom Prénom pour faciliter le parsing ultérieur
            cmbClient.getItems().add(cli.getIdClient() + " - " + cli.getNom() + " " + cli.getPrenom());
        }

        // 3. Chargement des options supplémentaires sous forme de CheckBoxes
        List<DemandeSpeciale> listeDemandes = reservationDAO.listerToutesLesDemandes();
        for (DemandeSpeciale d : listeDemandes) {
            CheckBox cb = new CheckBox(d.getNomDemande());
            cb.setUserData(d.getId()); // On garde l'ID technique en mémoire
            listViewDemandes.getItems().add(cb);
        }
    }

    /**
     * Ouvre la vue de consultation/modification globale des réservations.
     */
    @FXML
    public void ouvrirGestionReservations() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/hotel/view/GestionReservations.fxml"));
            Parent root = fxmlLoader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Archives & Gestion");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Navigation", "Erreur lors de l'accès à la gestion.");
        }
    }

    /**
     * Procède à la validation, au contrôle de disponibilité et à l'enregistrement en base.
     */
    @FXML
    public void validerReservation() {
        try {
            // Vérification basique des champs obligatoires
            if (cmbChambre.getValue() == null || cmbClient.getValue() == null || dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
                afficherAlerte(Alert.AlertType.WARNING, "Formulaire incomplet", "Veuillez renseigner toutes les informations.");
                return;
            }

            // Récupération de l'ID de la chambre choisie
            String display = cmbChambre.getValue();
            String numChambre = display.contains(" ") ? display.split(" ")[0] : display;
            int idChambre = reservationDAO.trouverIdParNumero(numChambre);
            
            // Récupération des dates
            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();
            
            // Récupération de l'ID client (depuis le format "ID - Nom...")
            String displayClient = cmbClient.getValue();
            int idClient = Integer.parseInt(displayClient.split(" - ")[0]);

            // --- Contrôles de disponibilité critiques ---
            
            // 1. État de service (Nettoyage, Maintenance ?)
            String etat = reservationDAO.getEtatChambre(idChambre);
            if (!"DISPONIBLE".equalsIgnoreCase(etat)) {
                afficherAlerte(Alert.AlertType.ERROR, "Statut invalide", "La chambre est actuellement en " + etat);
                return;
            }

            // 2. Conflit de calendrier (Chevauchement avec une autre réservation)
            if (reservationDAO.estChambreOccupee(idChambre, debut, fin, 0)) {
                afficherAlerte(Alert.AlertType.ERROR, "Conflit de dates", "Cette chambre est déjà prise pour la période choisie.");
                return;
            }

            // Création de l'objet métier
            String notes = txtNotes.getText();
            Reservation res = new Reservation(0, debut, fin, "EN_ATTENTE", idClient, idChambre, notes);
            
            // Persistence
            if (reservationDAO.ajouterReservation(res)) {
                // Enregistrement des demandes spéciales sélectionnées
                for (CheckBox cb : listViewDemandes.getItems()) {
                    if (cb.isSelected()) {
                        reservationDAO.ajouterDemandeALaReservation(res.getId(), (int) cb.getUserData());
                    }
                }
                
                afficherAlerte(Alert.AlertType.INFORMATION, "Confirmé", "Réservation enregistrée avec succès !");
                resetFormulaire();
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur DB", "L'enregistrement a échoué.");
            }
        } catch (Exception e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur système", "Une erreur inattendue est survenue : " + e.getMessage());
        }
    }

    /**
     * Réinitialise les champs après un succès.
     */
    private void resetFormulaire() {
        cmbClient.setValue(null);
        cmbChambre.setValue(null);
        txtNotes.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        initialize(null, null); // Recharger les chambres (le statut d'une vient de changer)
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}