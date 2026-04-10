package com.hotel.controller;

import com.hotel.dao.ChambreDAO;
import com.hotel.model.Chambre;
import com.hotel.model.Employe;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChambreController {
    
    @FXML private TableView<Chambre> tableChambres;
    @FXML private TableColumn<Chambre, String> colNumero, colEtat, colPersonnel;
    @FXML private TableColumn<Chambre, Number> colPrix, colType;
    @FXML private TextField txtNumero, txtPrix;
    @FXML private ComboBox<String> cmbType, cmbEtat;
    @FXML private ComboBox<Employe> cmbEmployes;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnValider;

    private ChambreDAO chambreDAO = new ChambreDAO();

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(d -> d.getValue().numeroProperty());
        colEtat.setCellValueFactory(d -> d.getValue().etatProperty());
        colPrix.setCellValueFactory(d -> d.getValue().prixParNuitProperty());
        colType.setCellValueFactory(d -> d.getValue().idTypeProperty());
        colPersonnel.setCellValueFactory(d -> d.getValue().nomPersonnelProperty());

        colType.setCellFactory(column -> new TableCell<Chambre, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else {
                    switch (item.intValue()) {
                        case 1: setText("Simple"); break;
                        case 2: setText("Double"); break;
                        case 3: setText("Suite"); break;
                        default: setText("Inconnu");
                    }
                }
            }
        });

        cmbType.getItems().addAll("Simple", "Double", "Suite");
        cmbEtat.getItems().addAll("DISPONIBLE", "EN_NETTOYAGE", "EN_MAINTENANCE");

        // AUTOMATISATION DU PRIX
        cmbType.setOnAction(e -> {
            String type = cmbType.getValue();
            if (type != null) {
                switch (type) {
                    case "Simple": txtPrix.setText("25000"); break;
                    case "Double": txtPrix.setText("40000"); break;
                    case "Suite":  txtPrix.setText("85000"); break;
                    default:       txtPrix.setText("0");
                }
            }
        });

        cmbEtat.setOnAction(e -> filtrerEmployesParEtat(cmbEtat.getValue()));

        tableChambres.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNumero.setText(newVal.getNumero());
                txtPrix.setText(String.valueOf(newVal.getPrixParNuit()));
                cmbType.getSelectionModel().select(newVal.getIdType() - 1);
                cmbEtat.getSelectionModel().select(newVal.getEtat());
            }
        });

        btnAjouter.setOnAction(e -> onAjouterClicked());
        btnModifier.setOnAction(e -> onModifierClicked());
        btnSupprimer.setOnAction(e -> onSupprimerClicked());
        btnValider.setOnAction(e -> onValiderClicked());
        
        rafraichirTable();
    }
    
    // ... reste de tes méthodes onAjouter, onModifier, etc. restent inchangées ...
    public void onAjouterClicked() {
        try {
            int idType = cmbType.getSelectionModel().getSelectedIndex() + 1;
            chambreDAO.ajouterChambre(txtNumero.getText(), "DISPONIBLE", Double.parseDouble(txtPrix.getText()), idType);
            rafraichirTable();
        } catch (Exception e) { afficherAlerte("Erreur", "Vérifiez vos saisies."); }
    }

    public void onModifierClicked() {
        Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int idType = cmbType.getSelectionModel().getSelectedIndex() + 1;
            chambreDAO.modifierChambre(selected.getIdChambre(), txtNumero.getText(), cmbEtat.getValue(), Double.parseDouble(txtPrix.getText()), idType);
            rafraichirTable();
        } else {
            afficherAlerte("Erreur", "Sélectionnez une chambre à modifier.");
        }
    }

    public void onSupprimerClicked() {
        Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            chambreDAO.supprimerChambre(selected.getIdChambre());
            rafraichirTable();
        } else {
            afficherAlerte("Erreur", "Sélectionnez une chambre à supprimer.");
        }
    }

    public void onValiderClicked() {
        Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
        String etat = cmbEtat.getValue();
        Employe emp = cmbEmployes.getSelectionModel().getSelectedItem();
        if (selected != null && etat != null) {
            if (etat.equals("DISPONIBLE")) { chambreDAO.libererChambre(selected.getIdChambre()); }
            else if (emp != null) { chambreDAO.mettreAJourStatut(selected.getIdChambre(), etat, emp.getId()); }
            else { afficherAlerte("Erreur", "Veuillez choisir un employé."); return; }
            rafraichirTable();
        }
    }

    private void filtrerEmployesParEtat(String etat) {
        List<Employe> tous = chambreDAO.listerEmployes();
        if ("EN_NETTOYAGE".equals(etat) || "EN_MAINTENANCE".equals(etat)) {
            // On affiche tous les employés pour laisser le choix total à l'utilisateur
            // (La table n'ayant pas forcément les ENUM exacts 'NETTOYEUR')
            cmbEmployes.setItems(FXCollections.observableArrayList(tous));
        } else {
            cmbEmployes.setItems(FXCollections.observableArrayList());
            cmbEmployes.setValue(null);
        }
    }

    private void rafraichirTable() { tableChambres.setItems(FXCollections.observableArrayList(chambreDAO.listerToutesLesChambres())); }
    
    private void afficherAlerte(String t, String m) { 
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(m); a.show(); 
    }
}