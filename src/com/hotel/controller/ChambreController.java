package com.hotel.controller;

import com.hotel.dao.ChambreDAO;
import com.hotel.model.Chambre;
import com.hotel.model.Employe;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des chambres (Chambre.fxml).
 * Gère l'affichage, l'ajout, la modification et l'assignation du personnel aux chambres.
 */
public class ChambreController {
    
    @FXML private TableView<Chambre> tableChambres;
    @FXML private TableColumn<Chambre, String> colNumero, colEtat, colPersonnel;
    @FXML private TableColumn<Chambre, Number> colPrix, colType;
    @FXML private TextField txtNumero, txtPrix;
    @FXML private ComboBox<String> cmbType, cmbEtat;
    @FXML private ComboBox<Employe> cmbEmployes; // Liste du personnel disponible
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnValider;

    private ChambreDAO chambreDAO = new ChambreDAO();

    /**
     * Initialisation de la vue et configuration des colonnes du tableau.
     */
    @FXML
    public void initialize() {
        // Liaison des colonnes avec les propriétés de l'objet Chambre
        colNumero.setCellValueFactory(d -> d.getValue().numeroProperty());
        colEtat.setCellValueFactory(d -> d.getValue().etatProperty());
        colPrix.setCellValueFactory(d -> d.getValue().prixParNuitProperty());
        colType.setCellValueFactory(d -> d.getValue().idTypeProperty());
        colPersonnel.setCellValueFactory(d -> d.getValue().nomPersonnelProperty());

        // Traduction visuelle de l'ID type (1, 2, 3) en libellé (Simple, Double, Suite)
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

        // Initialisation des options des ComboBox
        cmbType.getItems().addAll("Simple", "Double", "Suite");
        cmbEtat.getItems().addAll("DISPONIBLE", "EN_NETTOYAGE", "EN_MAINTENANCE");

        // AUTOMATISATION DU PRIX : Suggère un prix selon le type sélectionné
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

        // Filtrage dynamique du personnel selon l'état choisi (ex: Nettoyage -> liste des nettoyeurs)
        cmbEtat.setOnAction(e -> filtrerEmployesParEtat(cmbEtat.getValue()));

        // Remplissage automatique des champs lors d'une sélection dans le tableau
        tableChambres.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNumero.setText(newVal.getNumero());
                txtPrix.setText(String.valueOf(newVal.getPrixParNuit()));
                cmbType.getSelectionModel().select(newVal.getIdType() - 1);
                cmbEtat.getSelectionModel().select(newVal.getEtat());
            }
        });

        // Liaison des événements boutons
        btnAjouter.setOnAction(e -> onAjouterClicked());
        btnModifier.setOnAction(e -> onModifierClicked());
        btnSupprimer.setOnAction(e -> onSupprimerClicked());
        btnValider.setOnAction(e -> onValiderClicked());
        
        rafraichirTable();
    }
    
    /**
     * Enregistre une nouvelle chambre en base.
     */
    public void onAjouterClicked() {
        try {
            int idType = cmbType.getSelectionModel().getSelectedIndex() + 1;
            chambreDAO.ajouterChambre(txtNumero.getText(), "DISPONIBLE", Double.parseDouble(txtPrix.getText()), idType);
            rafraichirTable();
        } catch (Exception e) { afficherAlerte("Erreur", "Vérifiez vos saisies."); }
    }

    /**
     * Met à jour les informations structurelles de la chambre sélectionnée.
     */
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

    /**
     * Supprime définitivement une chambre.
     */
    public void onSupprimerClicked() {
        Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            chambreDAO.supprimerChambre(selected.getIdChambre());
            rafraichirTable();
        } else {
            afficherAlerte("Erreur", "Sélectionnez une chambre à supprimer.");
        }
    }

    /**
     * Valide le changement d'état (ex: Disponible -> Nettoyage) et l'assignation du personnel.
     */
    public void onValiderClicked() {
        Chambre selected = tableChambres.getSelectionModel().getSelectedItem();
        String etat = cmbEtat.getValue();
        Employe emp = cmbEmployes.getSelectionModel().getSelectedItem();
        if (selected != null && etat != null) {
            if (etat.equals("DISPONIBLE")) {
                // Si elle redevient disponible, on retire l'employé assigné
                chambreDAO.libererChambre(selected.getIdChambre()); 
            } else if (emp != null) {
                // Sinon on assigne le personnel choisi
                chambreDAO.mettreAJourStatut(selected.getIdChambre(), etat, emp.getId()); 
            } else {
                afficherAlerte("Erreur", "Veuillez choisir un employé."); 
                return; 
            }
            rafraichirTable();
        }
    }

    /**
     * Charge le personnel disponible pour l'assignation.
     */
    private void filtrerEmployesParEtat(String etat) {
        List<Employe> tous = chambreDAO.listerEmployes();
        if ("EN_NETTOYAGE".equals(etat) || "EN_MAINTENANCE".equals(etat)) {
            // Dans cette version, on laisse le choix parmi tout le personnel pour plus de flexibilité
            cmbEmployes.setItems(FXCollections.observableArrayList(tous));
        } else {
            cmbEmployes.setItems(FXCollections.observableArrayList());
            cmbEmployes.setValue(null);
        }
    }

    /**
     * Recharge les données depuis la base et rafraîchit l'affichage.
     */
    private void rafraichirTable() { 
        tableChambres.setItems(FXCollections.observableArrayList(chambreDAO.listerToutesLesChambres())); 
    }
    
    /**
     * Utilitaire d'affichage d'erreurs.
     */
    private void afficherAlerte(String t, String m) { 
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(m); a.show(); 
    }
}