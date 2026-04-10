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

/**
 * Contrôleur pour la gestion des factures (GestionFactures.fxml).
 * Gère la consultation, le filtrage, et l'enregistrement des paiements.
 */
public class GestionFacturesController implements Initializable {

    // --- Composants de la Table ---
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

    // --- Filtres et Résumé ---
    @FXML
    private ComboBox<String> comboFiltreStatut;
    @FXML
    private Label lblTotalFacture; // Somme de toutes les factures affichées
    @FXML
    private Label lblTotalEncaisse; // Somme des paiements perçus
    @FXML
    private Label lblResteAPayer; // Différence (créance)
    @FXML
    private Label lblStatut; // Feedback utilisateur

    // --- Panneau de Détails (à droite) ---
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
    private ListView<String> listPaiements; // Liste chronologique des versements

    // --- DAOs ---
    private final FactureDAO factureDAO = new FactureDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();

    // --- Données ---
    private ObservableList<Facture> listeFactures = FXCollections.observableArrayList();
    private Facture factureSelectionnee;

    /**
     * Initialisation de la vue.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        configurerComboFiltre();
        chargerFactures();
    }

    /**
     * Configure le rendu des colonnes (formatage monétaire, couleurs de statut).
     */
    private void configurerColonnes() {
        colIdFacture.setCellValueFactory(new PropertyValueFactory<>("idFacture"));
        colDateEmission.setCellValueFactory(new PropertyValueFactory<>("dateEmission"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));

        // Liaison avec les attributs transients calculés via les JOIN SQL
        colClient.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomClient()));
        colChambre.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNumerosChambre()));

        // Application de couleurs contextuelles sur le statut
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
                    case "PAYE" -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;"); // Vert
                    case "PARTIEL" -> setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;"); // Orange
                    case "NON_PAYE" -> setStyle("-fx-text-fill: #B71C1C; -fx-font-weight: bold;"); // Rouge
                    default -> setStyle("");
                }
            }
        });

        // Formatage du montant en devise locale (FCFA)
        colMontantTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f FCFA", item));
            }
        });

        // Définition des boutons d'action (Payer, Modifier, Supprimer)
        colActionsFacture.setCellFactory(col -> new TableCell<>() {
            private final Button btnPayer = new Button("💳");
            private final Button btnModif = new Button("✏️");
            private final Button btnSuppr = new Button("🗑️");
            private final HBox box = new HBox(5, btnPayer, btnModif, btnSuppr);

            {
                btnPayer.setStyle(
                        "-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
                btnModif.setStyle(
                        "-fx-background-color: #FFA726; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
                btnSuppr.setStyle(
                        "-fx-background-color: #EF5350; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");

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

    /**
     * Initialise la combo de filtrage par état.
     */
    private void configurerComboFiltre() {
        comboFiltreStatut.setItems(FXCollections.observableArrayList("Tous", "NON_PAYE", "PARTIEL", "PAYE"));
        comboFiltreStatut.setValue("Tous");
    }

    /**
     * Charge toutes les factures depuis la base de données.
     */
    private void chargerFactures() {
        listeFactures.setAll(factureDAO.getToutesLesFactures());
        mettreAJourResume();
        setStatut("Prêt.");
    }

    /**
     * Calcule et affiche les indicateurs financiers globaux (CA théorique vs CA
     * encaissé).
     */
    private void mettreAJourResume() {
        double totalFacture = listeFactures.stream().mapToDouble(Facture::getMontantTotal).sum();
        double totalEncaisse = factureDAO.getTotalRevenusPaies();
        double reste = totalFacture - totalEncaisse;

        lblTotalFacture.setText(String.format("%,.0f FCFA", totalFacture));
        lblTotalEncaisse.setText(String.format("%,.0f FCFA", totalEncaisse));
        lblResteAPayer.setText(String.format("%,.0f FCFA", reste));
    }

    /**
     * Applique le filtre de statut choisi dans la ComboBox.
     */
    @FXML
    private void filtrerParStatut() {
        String statut = comboFiltreStatut.getValue();
        if (statut == null || statut.equals("Tous")) {
            chargerFactures();
        } else {
            listeFactures.setAll(factureDAO.getFacturesParStatut(statut));
            setStatut("Affichage : " + statut);
        }
    }

    /**
     * Annule les filtres.
     */
    @FXML
    private void reinitialiserFiltre() {
        comboFiltreStatut.setValue("Tous");
        chargerFactures();
    }

    /**
     * Déclenchée lors de la sélection d'une facture.
     */
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
            afficherAlerte(Alert.AlertType.WARNING, "Sélection requise", "Choisissez une facture.");
            return;
        }
        ouvrirFormulaire(factureSelectionnee);
    }

    /**
     * Ouvre l'interface de saisie d'un nouveau paiement pour la facture
     * sélectionnée.
     */
    @FXML
    public void ouvrirFormulairePaiement() {
        if (factureSelectionnee == null)
            return;

        // On n'autorise pas de paiement sur une facture déjà soldée
        if (factureSelectionnee.getStatutPaiement().equals("PAYE")) {
            afficherAlerte(Alert.AlertType.INFORMATION, "Facture soldée", "Cette facture est déjà payée.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/view/FormulairePaiement.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Nouveau Paiement");
            stage.initModality(Modality.APPLICATION_MODAL);

            FormulairePaiementController ctrl = loader.getController();
            ctrl.setFacture(factureSelectionnee);
            ctrl.setOnSauvegardeCallback(() -> {
                chargerFactures(); // Rafraîchit la liste
                // On met aussi à jour le panneau de détails
                Facture maj = factureDAO.getFactureParId(factureSelectionnee.getIdFacture());
                if (maj != null) {
                    factureSelectionnee = maj;
                    afficherDetail(maj);
                }
            });
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("[GestionFacturesController] Erreur UI : " + e.getMessage());
        }
    }

    /**
     * Supprime la facture (cascade possible sur les paiements selon la DB).
     */
    @FXML
    public void supprimerFacture() {
        if (factureSelectionnee == null)
            return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la facture #" + factureSelectionnee.getIdFacture() + " ?");
        confirm.setContentText("Attention : les paiements liés seront également impactés.");

        Optional<ButtonType> rep = confirm.showAndWait();
        if (rep.isPresent() && rep.get() == ButtonType.OK) {
            if (factureDAO.supprimerFacture(factureSelectionnee.getIdFacture())) {
                chargerFactures();
                panneauDetail.setVisible(false);
                factureSelectionnee = null;
                setStatut("Facture supprimée.");
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Action impossible.");
            }
        }
    }

    /**
     * Charge le dialogue de création/édition.
     */
    private void ouvrirFormulaire(Facture facture) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/view/FormulaireFacture.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(facture == null ? "Nouveau Document" : "Modification");
            stage.initModality(Modality.APPLICATION_MODAL);

            FormulaireFactureController ctrl = loader.getController();
            if (facture != null)
                ctrl.remplirFormulaire(facture);
            ctrl.setOnSauvegardeCallback(this::chargerClients); // Nom de méthode trompeur (devrait chargerFactures)
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("[GestionFacturesController] Erreur UI : " + e.getMessage());
        }
    }

    /**
     * Affiche les détails d'une facture et l'historique complet des versements
     * reçus.
     */
    private void afficherDetail(Facture f) {
        panneauDetail.setVisible(true);
        lblDetailClient.setText("👤 " + f.getNomClient());
        lblDetailChambre.setText("🛏️ " + f.getNumerosChambre());
        lblDetailDate.setText("📅 Émise le : " + f.getDateEmission());
        lblDetailMontant.setText("💰 Montant : " + String.format("%,.0f FCFA", f.getMontantTotal()));

        // Mise en forme du statut dans le détail
        String statut = f.getStatutPaiement();
        lblDetailStatut.setText("📌 Statut : " + statut);
        lblDetailStatut.setStyle(switch (statut) {
            case "PAYE" -> "-fx-text-fill: #2E7D32; -fx-font-weight: bold;";
            case "PARTIEL" -> "-fx-text-fill: #E65100; -fx-font-weight: bold;";
            default -> "-fx-text-fill: #B71C1C; -fx-font-weight: bold;";
        });

        // Calcul du solde restant dû
        double dejaPaye = factureDAO.getMontantDejaPaye(f.getIdFacture());
        double restant = f.getMontantTotal() - dejaPaye;
        lblDetailRestant.setText("⚠️ Reste à verser : " + String.format("%,.0f FCFA", restant));

        // Récupération de l'historique des paiements via PaiementDAO
        List<Paiement> paiements = paiementDAO.getPaiementsParFacture(f.getIdFacture());
        ObservableList<String> lignes = FXCollections.observableArrayList();
        for (Paiement p : paiements) {
            lignes.add(p.getDatePaiement() + "  |  " + String.format("%,.0f FCFA", p.getMontant()) + "  |  "
                    + p.getModePaiement());
        }
        if (lignes.isEmpty())
            lignes.add("Aucun versement enregistré.");
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
