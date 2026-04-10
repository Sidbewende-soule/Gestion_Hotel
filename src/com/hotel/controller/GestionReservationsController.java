package com.hotel.controller;

import com.hotel.dao.ChambreDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.model.DemandeSpeciale;
import com.hotel.model.Reservation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur pour l'écran de gestion des réservations (GestionReservations.fxml).
 * Permet de visualiser, modifier et annuler les réservations existantes.
 */
public class GestionReservationsController {

    // --- Colonnes du tableau ---
    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String> colClient;
    @FXML private TableColumn<Reservation, String> colChambre;
    @FXML private TableColumn<Reservation, String> colDateDebut;
    @FXML private TableColumn<Reservation, String> colDateFin;
    @FXML private TableColumn<Reservation, String> colStatut;
    
    // --- Champs du formulaire de modification rapide ---
    @FXML private javafx.scene.layout.GridPane formulaireModif;
    @FXML private TextField editDateDebut;
    @FXML private TextField editDateFin;
    @FXML private ComboBox<String> cmbNumeroChambre;
    @FXML private ListView<CheckBox> listViewOptions; // Liste dynamique des demandes spéciales
    @FXML private TextArea editNotes;

    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;

    private ReservationDAO reservationDAO = new ReservationDAO();
    private ChambreDAO chambreDAO = new ChambreDAO();

    /**
     * Initialisation du contrôleur JavaFX.
     */
    @FXML
    public void initialize() {
        // Mapping des colonnes avec les propriétés de l'objet Reservation
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        colChambre.setCellValueFactory(new PropertyValueFactory<>("numeroChambre"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Chargement des chambres éligibles pour une éventuelle modification
        cmbNumeroChambre.setItems(FXCollections.observableArrayList(chambreDAO.listerChambresDisponiblesAvecType()));

        // Listener de sélection : remplit les champs d'édition dès qu'une ligne est cliquée
        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editDateDebut.setText(newVal.getDateDebut().toString());
                editDateFin.setText(newVal.getDateFin().toString());
                cmbNumeroChambre.setValue(newVal.getNumeroChambre()); 
                editNotes.setText(newVal.getNotes());
                chargerOptions(newVal.getId()); // Synchronise les CheckBoxes des options
            } else {
                listViewOptions.getItems().clear();
                editNotes.clear();
            }
        });

        // Les boutons ne sont actifs que si une réservation est sélectionnée
        btnModifier.disableProperty().bind(tableReservations.getSelectionModel().selectedItemProperty().isNull());
        btnAnnuler.disableProperty().bind(tableReservations.getSelectionModel().selectedItemProperty().isNull());

        chargerDonnees();
    }

    /**
     * Construit une liste de CheckBoxes représentant toutes les demandes spéciales disponibles,
     * en cochant celles déjà liées à la réservation sélectionnée.
     */
    private void chargerOptions(int idReservation) {
        listViewOptions.getItems().clear();
        List<DemandeSpeciale> all = reservationDAO.listerToutesLesDemandes();
        List<Integer> selected = reservationDAO.getOptionsPourReservation(idReservation);

        for (DemandeSpeciale d : all) {
            CheckBox cb = new CheckBox(d.getNomDemande());
            cb.setUserData(d.getId()); // Stockage de l'ID technique dans l'objet UI
            if (selected.contains(d.getId())) {
                cb.setSelected(true);
            }
            listViewOptions.getItems().add(cb);
        }
    }

    /**
     * Peuple le tableau principal.
     */
    @FXML
    public void chargerDonnees() {
        tableReservations.setItems(FXCollections.observableArrayList(reservationDAO.listerToutesLesReservations()));
    }

    /**
     * Applique les modifications effectuées dans le panneau latéral.
     */
    @FXML
    public void modifierReservation() {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (cmbNumeroChambre.getValue() == null) {
                    afficherAlerte(Alert.AlertType.WARNING, "Sélection", "Veuillez choisir une chambre.");
                    return;
                }
                
                // Parsing du numéro de chambre (format "101 - Simple")
                String numChambre = cmbNumeroChambre.getValue().split(" - ")[0];
                
                // Mise à jour de l'objet modèle
                selected.setDateDebut(LocalDate.parse(editDateDebut.getText()));
                selected.setDateFin(LocalDate.parse(editDateFin.getText()));
                selected.setIdChambre(reservationDAO.trouverIdParNumero(numChambre));
                selected.setNotes(editNotes.getText());
                
                // Persistence en base
                if (reservationDAO.modifierReservation(selected)) {
                    // Mise à jour des demandes spéciales (Suppression globale puis ré-insertion des cochées)
                    reservationDAO.supprimerDemandesAssociees(selected.getId());
                    for (CheckBox cb : listViewOptions.getItems()) {
                        if (cb.isSelected()) {
                            reservationDAO.ajouterDemandeALaReservation(selected.getId(), (int) cb.getUserData());
                        }
                    }
                    afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Mise à jour réussie.");
                    chargerDonnees(); 
                }
            } catch (Exception e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur de saisie", "Vérifiez le format des dates (AAAA-MM-JJ).");
            }
        }
    }

    /**
     * Change le statut de la réservation en 'ANNULEE'.
     */
    @FXML
    public void annulerReservation() {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Demande de confirmation par défaut
            if (reservationDAO.annulerReservation(selected.getId())) {
                chargerDonnees();
                afficherAlerte(Alert.AlertType.INFORMATION, "Annulation", "La réservation a été annulée avec succès.");
            }
        }
    }

    /**
     * Utilitaire d'affichage des dialogues.
     */
    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}