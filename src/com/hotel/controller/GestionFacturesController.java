package com.hotel.controller;

import com.hotel.dao.FactureDAO;
import com.hotel.dao.PaiementDAO;
import com.hotel.model.Facture;
import com.hotel.model.Paiement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestionFacturesController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML
    private TableView<Facture> tableFactures;
    @FXML
    private TableColumn<Facture, Integer> colIdFacture;
    @FXML
    private TableColumn<Facture, String> colClient;
    @FXML
    private TableColumn<Facture, String> colChambre;
    @FXML
    private TableColumn<Facture, String> colDateEmission;
    @FXML
    private TableColumn<Facture, Double> colMontantTotal;
    @FXML
    private TableColumn<Facture, String> colStatut;
    @FXML
    private TableColumn<Facture, Void> colActionsFacture;

    @FXML
    private ComboBox<String> comboFiltreStatut;
    @FXML
    private Label lblTotalFacture;
    @FXML
    private Label lblTotalEncaisse;
    @FXML
    private Label lblResteAPayer;
    @FXML
    private Label lblStatut;

    @FXML
    private VBox panneauDetail;
    @FXML
    private Label lblDetailClient;
    @FXML
    private Label lblDetailChambre;
    @FXML
    private Label lblDetailDate;
    @FXML
    private Label lblDetailMontant;
    @FXML
    private Label lblDetailStatut;
    @FXML
    private Label lblDetailRestant;
    @FXML
    private ListView<String> listPaiements;

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final FactureDAO factureDAO = new FactureDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();

    // ── État ──────────────────────────────────────────────────────────────────
    private ObservableList<Facture> listeFactures = FXCollections.observableArrayList();
    private Facture factureSelectionnee;

    // ════════════════════════════════════════════════════════════════════════
    // Initialisation
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        configurerComboFiltre();
        chargerFactures();
    }

    private void configurerColonnes() {
        colIdFacture.setCellValueFactory(new PropertyValueFactory<>("idFacture"));
        colDateEmission.setCellValueFactory(new PropertyValueFactory<>("dateEmission"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));

        // Colonnes transientes (issus de JOIN)
        colClient.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomClient()));
        colChambre.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNumerosChambre()));

        // Colorer la colonne Statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                switch (item) {
                    case "PAYE" -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case "PARTIEL" -> setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
                    case "NON_PAYE" -> setStyle("-fx-text-fill: #B71C1C; -fx-font-weight: bold;");
                    default -> setStyle("");
                }
            }
        });

        // Formater le montant en FCFA
        colMontantTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f FCFA", item));
            }
        });

        // Colonne Actions
        colActionsFacture.setCellFactory(col -> new TableCell<>() {
            private final Button btnPayer = new Button("💳");
            private final Button btnModif = new Button("✏️");
            private final Button btnSuppr = new Button("🗑️");
            private final HBox box = new HBox(5, btnPayer, btnModif, btnSuppr);

            {
                btnPayer.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white;"
                        + "-fx-background-radius: 4; -fx-cursor: hand;");
                btnModif.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white;"
                        + "-fx-background-radius: 4; -fx-cursor: hand;");
                btnSuppr.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white;"
                        + "-fx-background-radius: 4; -fx-cursor: hand;");

                btnPayer.setOnAction(e -> {
                    factureSelectionnee = getTableView().getItems().get(getIndex());
                    ouvrirFormulairePaiement();
                });
                btnModif.setOnAction(e -> {
                    factureSelectionnee = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification();
                });
                btnSuppr.setOnAction(e -> {
                    factureSelectionnee = getTableView().getItems().get(getIndex());
                    supprimerFacture();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableFactures.setItems(listeFactures);
    }

    private void configurerComboFiltre() {
        comboFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "NON_PAYE", "PARTIEL", "PAYE"));
        comboFiltreStatut.setValue("Tous");
    }

    // ════════════════════════════════════════════════════════════════════════
    // Chargement
    // ════════════════════════════════════════════════════════════════════════

    private void chargerFactures() {
        listeFactures.setAll(factureDAO.getToutesLesFactures());
        mettreAJourResume();
        setStatut("Factures chargées avec succès.");
    }

    private void mettreAJourResume() {
        double totalFacture = listeFactures.stream().mapToDouble(Facture::getMontantTotal).sum();
        double totalEncaisse = factureDAO.getTotalRevenusPaies();
        double reste = totalFacture - totalEncaisse;
        lblTotalFacture.setText(String.format("%,.0f FCFA", totalFacture));
        lblTotalEncaisse.setText(String.format("%,.0f FCFA", totalEncaisse));
        lblResteAPayer.setText(String.format("%,.0f FCFA", reste));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Actions FXML
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void filtrerParStatut() {
        String statut = comboFiltreStatut.getValue();
        if (statut == null || statut.equals("Tous")) {
            listeFactures.setAll(factureDAO.getToutesLesFactures());
        } else {
            listeFactures.setAll(factureDAO.getFacturesParStatut(statut));
        }
        setStatut("Filtre appliqué : " + statut);
    }

    @FXML
    private void reinitialiserFiltre() {
        comboFiltreStatut.setValue("Tous");
        chargerFactures();
    }

    @FXML
    private void onFactureSelectionnee() {
        Facture f = tableFactures.getSelectionModel().getSelectedItem();
        if (f == null)
            return;
        factureSelectionnee = f;
        afficherDetail(f);
    }

    @FXML
    public void ouvrirFormulaireAjout() {
        ouvrirFormulaire(null);
    }

    @FXML
    public void ouvrirFormulaireModification() {
        if (factureSelectionnee == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner une facture à modifier.");
            return;
        }
        ouvrirFormulaire(factureSelectionnee);
    }

    @FXML
    public void ouvrirFormulairePaiement() {
        if (factureSelectionnee == null)
            return;
        if (factureSelectionnee.getStatutPaiement().equals("PAYE")) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Facture soldée",
                    "Cette facture est déjà entièrement payée.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/view/FormulairePaiement.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Enregistrer un paiement");
            stage.initModality(Modality.APPLICATION_MODAL);
            FormulairePaiementController ctrl = loader.getController();
            ctrl.setFacture(factureSelectionnee);
            ctrl.setOnSauvegardeCallback(() -> {
                chargerFactures();
                // Recharger la facture mise à jour pour le panneau de détail
                Facture maj = factureDAO.getFactureParId(factureSelectionnee.getIdFacture());
                if (maj != null) {
                    factureSelectionnee = maj;
                    afficherDetail(maj);
                }
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerFacture() {
        if (factureSelectionnee == null)
            return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la facture ?");
        confirm.setContentText("Attention : les paiements associés seront aussi supprimés.");
        Optional<ButtonType> rep = confirm.showAndWait();
        if (rep.isPresent() && rep.get() == ButtonType.OK) {
            if (factureDAO.supprimerFacture(factureSelectionnee.getIdFacture())) {
                chargerFactures();
                panneauDetail.setVisible(false);
                factureSelectionnee = null;
                setStatut("Facture supprimée.");
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Suppression échouée.");
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Utilitaires privés
    // ════════════════════════════════════════════════════════════════════════

    private void ouvrirFormulaire(Facture facture) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/view/FormulaireFacture.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(facture == null ? "Nouvelle facture" : "Modifier la facture");
            stage.initModality(Modality.APPLICATION_MODAL);
            FormulaireFactureController ctrl = loader.getController();
            if (facture != null)
                ctrl.remplirFormulaire(facture);
            ctrl.setOnSauvegardeCallback(this::chargerFactures);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherDetail(Facture f) {
        panneauDetail.setVisible(true);
        lblDetailClient.setText("👤 " + f.getNomClient());
        lblDetailChambre.setText("🛏️ " + f.getNumerosChambre());
        lblDetailDate.setText("📅 Émise le : " + f.getDateEmission());
        lblDetailMontant.setText("💰 Montant : " + String.format("%,.0f FCFA", f.getMontantTotal()));

        String statut = f.getStatutPaiement();
        lblDetailStatut.setText("📌 Statut : " + statut);
        lblDetailStatut.setStyle(switch (statut) {
            case "PAYE" -> "-fx-text-fill: #2E7D32; -fx-font-weight: bold;";
            case "PARTIEL" -> "-fx-text-fill: #E65100; -fx-font-weight: bold;";
            default -> "-fx-text-fill: #B71C1C; -fx-font-weight: bold;";
        });

        double dejaPaye = factureDAO.getMontantDejaPaye(f.getIdFacture());
        double restant = f.getMontantTotal() - dejaPaye;
        lblDetailRestant.setText("⚠️ Restant : " + String.format("%,.0f FCFA", restant));

        // Historique paiements
        List<Paiement> paiements = paiementDAO.getPaiementsParFacture(f.getIdFacture());
        ObservableList<String> lignes = FXCollections.observableArrayList();
        for (Paiement p : paiements) {
            lignes.add(p.getDatePaiement() + "  |  "
                    + String.format("%,.0f FCFA", p.getMontant())
                    + "  |  " + p.getModePaiement());
        }
        if (lignes.isEmpty())
            lignes.add("Aucun paiement enregistré.");
        listPaiements.setItems(lignes);
    }

    private void setStatut(String msg) {
        lblStatut.setText(msg);
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
