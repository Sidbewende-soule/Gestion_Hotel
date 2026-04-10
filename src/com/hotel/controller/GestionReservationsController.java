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

public class GestionReservationsController {

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String> colClient;
    @FXML private TableColumn<Reservation, String> colChambre;
    @FXML private TableColumn<Reservation, String> colDateDebut;
    @FXML private TableColumn<Reservation, String> colDateFin;
    @FXML private TableColumn<Reservation, String> colStatut;
    
    @FXML private javafx.scene.layout.GridPane formulaireModif;
    @FXML private TextField editDateDebut;
    @FXML private TextField editDateFin;
    @FXML private ComboBox<String> cmbNumeroChambre;
    @FXML private ListView<CheckBox> listViewOptions;
    @FXML private TextArea editNotes;

    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;

    private ReservationDAO reservationDAO = new ReservationDAO();
    private ChambreDAO chambreDAO = new ChambreDAO();

    @FXML
    public void initialize() {
        // Configuration colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
        colChambre.setCellValueFactory(new PropertyValueFactory<>("numeroChambre"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        cmbNumeroChambre.setItems(FXCollections.observableArrayList(chambreDAO.listerChambresDisponiblesAvecType()));

        // Listener de sélection
        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editDateDebut.setText(newVal.getDateDebut().toString());
                editDateFin.setText(newVal.getDateFin().toString());
                cmbNumeroChambre.setValue(newVal.getNumeroChambre()); 
                editNotes.setText(newVal.getNotes());
                chargerOptions(newVal.getId());
            } else {
                // Nettoyage si aucune sélection
                listViewOptions.getItems().clear();
                editNotes.clear();
            }
        });

        btnModifier.disableProperty().bind(tableReservations.getSelectionModel().selectedItemProperty().isNull());
        btnAnnuler.disableProperty().bind(tableReservations.getSelectionModel().selectedItemProperty().isNull());

        chargerDonnees();
    }

    private void chargerOptions(int idReservation) {
        listViewOptions.getItems().clear();
        List<DemandeSpeciale> all = reservationDAO.listerToutesLesDemandes();
        List<Integer> selected = reservationDAO.getOptionsPourReservation(idReservation);

        for (DemandeSpeciale d : all) {
            CheckBox cb = new CheckBox(d.getNomDemande());
            cb.setUserData(d.getId());
            if (selected.contains(d.getId())) {
                cb.setSelected(true);
            }
            listViewOptions.getItems().add(cb);
        }
    }

    @FXML
    public void chargerDonnees() {
        tableReservations.setItems(FXCollections.observableArrayList(reservationDAO.listerToutesLesReservations()));
    }

    @FXML
    public void modifierReservation() {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (cmbNumeroChambre.getValue() == null) {
                    afficherAlerte(Alert.AlertType.WARNING, "Sélection", "Veuillez choisir une chambre.");
                    return;
                }
                String numChambre = cmbNumeroChambre.getValue().split(" - ")[0];
                selected.setDateDebut(LocalDate.parse(editDateDebut.getText()));
                selected.setDateFin(LocalDate.parse(editDateFin.getText()));
                selected.setIdChambre(reservationDAO.trouverIdParNumero(numChambre));
                selected.setNotes(editNotes.getText()); // <---
                
                if (reservationDAO.modifierReservation(selected)) {
                    reservationDAO.supprimerDemandesAssociees(selected.getId());
                    for (CheckBox cb : listViewOptions.getItems()) {
                        if (cb.isSelected()) {
                            reservationDAO.ajouterDemandeALaReservation(selected.getId(), (int) cb.getUserData());
                        }
                    }
                    afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Mise à jour réussie.");
                    chargerDonnees(); // Rafraîchissement auto après modif
                }
            } catch (Exception e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Données invalides.");
            }
        }
    }

    @FXML
    public void annulerReservation() {
        Reservation selected = tableReservations.getSelectionModel().getSelectedItem();
        if (selected != null && reservationDAO.annulerReservation(selected.getId())) {
            chargerDonnees();
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