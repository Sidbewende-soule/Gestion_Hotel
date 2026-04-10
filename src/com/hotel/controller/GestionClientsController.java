package com.hotel.controller;

import com.hotel.dao.ClientDAO;
import com.hotel.dao.RetourDAO;
import com.hotel.model.Client;
import com.hotel.model.Retour;

import javafx.beans.property.SimpleStringProperty;
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

@SuppressWarnings("unused")
public class GestionClientsController implements Initializable {

    // ── Champs injectés depuis le FXML ────────────────────────────────────────

    @FXML
    private TableView<Client> tableClients;
    @FXML
    private TableColumn<Client, Integer> colId;
    @FXML
    private TableColumn<Client, String> colNom;
    @FXML
    private TableColumn<Client, String> colPrenom;
    @FXML
    private TableColumn<Client, String> colTelephone;
    @FXML
    private TableColumn<Client, String> colEmail;
    @FXML
    private TableColumn<Client, String> colAdresse;
    @FXML
    private TableColumn<Client, String> colCarteFidelite;
    @FXML
    private TableColumn<Client, Void> colActions;

    @FXML
    private TextField txtRecherche;
    @FXML
    private Label lblNombreClients;
    @FXML
    private Label lblStatut;

    @FXML
    private VBox panneauDetail;
    @FXML
    private Label lblDetailNom;
    @FXML
    private Label lblDetailTel;
    @FXML
    private Label lblDetailEmail;
    @FXML
    private Label lblDetailAdresse;
    @FXML
    private Label lblDetailFidelite;
    @FXML
    private ListView<String> listAvis;

    // ── DAOs ──────────────────────────────────────────────────────────────────

    private final ClientDAO clientDAO = new ClientDAO();
    private final RetourDAO retourDAO = new RetourDAO();

    // ── Données ───────────────────────────────────────────────────────────────

    private ObservableList<Client> listeClients = FXCollections.observableArrayList();
    private Client clientSelectionne;

    // ════════════════════════════════════════════════════════════════════════
    // Initialisation
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        chargerClients();
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colCarteFidelite.setCellValueFactory(new PropertyValueFactory<>("numeroCarteFidelite"));

        // Colonne Actions : boutons Modifier et Supprimer
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnModif = new Button("✏️");
            private final Button btnSuppr = new Button("🗑️");
            private final HBox box = new HBox(6, btnModif, btnSuppr);

            {
                btnModif.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white;"
                        + "-fx-background-radius: 4; -fx-cursor: hand;");
                btnSuppr.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white;"
                        + "-fx-background-radius: 4; -fx-cursor: hand;");

                btnModif.setOnAction(e -> {
                    clientSelectionne = getTableView().getItems().get(getIndex());
                    ouvrirFormulaireModification();
                });
                btnSuppr.setOnAction(e -> {
                    clientSelectionne = getTableView().getItems().get(getIndex());
                    supprimerClient();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableClients.setItems(listeClients);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Chargement des données
    // ════════════════════════════════════════════════════════════════════════

    private void chargerClients() {
        listeClients.setAll(clientDAO.getTousLesClients());
        mettreAJourCompteur();
        setStatut("Clients chargés avec succès.");
    }

    private void mettreAJourCompteur() {
        lblNombreClients.setText(listeClients.size() + " client(s)");
    }

    // ════════════════════════════════════════════════════════════════════════
    // Actions FXML
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void rechercherClient() {
        String motCle = txtRecherche.getText().trim();
        if (motCle.isEmpty()) {
            chargerClients();
        } else {
            List<Client> resultats = clientDAO.rechercherClients(motCle);
            listeClients.setAll(resultats);
            mettreAJourCompteur();
            setStatut(resultats.size() + " résultat(s) pour « " + motCle + " ».");
        }
    }

    @FXML
    private void reinitialiserRecherche() {
        txtRecherche.clear();
        chargerClients();
    }

    @FXML
    private void onClientSelectionne() {
        Client client = tableClients.getSelectionModel().getSelectedItem();
        if (client == null)
            return;
        clientSelectionne = client;
        afficherDetail(client);
    }

    @FXML
    public void ouvrirFormulaireAjout() {
        ouvrirFormulaire(null);
    }

    @FXML
    public void ouvrirFormulaireModification() {
        if (clientSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un client à modifier.");
            return;
        }
        ouvrirFormulaire(clientSelectionne);
    }

    @FXML
    public void supprimerClient() {
        if (clientSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un client à supprimer.");
            return;
        }
        if (clientDAO.aDesReservations(clientSelectionne.getIdClient())) {
            afficherAlerte(Alert.AlertType.ERROR, "Suppression impossible",
                    "Ce client possède des réservations. Supprimez-les d'abord.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le client ?");
        confirm.setContentText("Voulez-vous vraiment supprimer "
                + clientSelectionne.getNomComplet() + " ?");
        Optional<ButtonType> reponse = confirm.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            if (clientDAO.supprimerClient(clientSelectionne.getIdClient())) {
                chargerClients();
                panneauDetail.setVisible(false);
                clientSelectionne = null;
                setStatut("Client supprimé avec succès.");
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                        "La suppression a échoué. Vérifiez la connexion.");
            }
        }
    }

    @FXML
    public void ouvrirFormulaireAvis() {
        if (clientSelectionne == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/view/FormulaireAvis.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/hotel/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Ajouter un avis");
            stage.initModality(Modality.APPLICATION_MODAL);
            FormulaireAvisController ctrl = loader.getController();
            ctrl.setClient(clientSelectionne);
            ctrl.setOnSauvegardeCallback(() -> afficherDetail(clientSelectionne));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Méthodes privées utilitaires
    // ════════════════════════════════════════════════════════════════════════

    private void ouvrirFormulaire(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/view/FormulaireClient.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/hotel/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(client == null ? "Nouveau client" : "Modifier le client");
            stage.initModality(Modality.APPLICATION_MODAL);
            FormulaireClientController ctrl = loader.getController();
            if (client != null)
                ctrl.remplirFormulaire(client);
            ctrl.setOnSauvegardeCallback(this::chargerClients);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void afficherDetail(Client client) {
        panneauDetail.setVisible(true);
        lblDetailNom.setText("👤 " + client.getNomComplet());
        lblDetailTel.setText("📞 " + (client.getTelephone() != null ? client.getTelephone() : "—"));
        lblDetailEmail.setText("✉️ " + (client.getEmail() != null ? client.getEmail() : "—"));
        lblDetailAdresse.setText("📍 " + (client.getAdresse() != null ? client.getAdresse() : "—"));
        lblDetailFidelite.setText("🎖️ Carte : "
                + (client.getNumeroCarteFidelite() != null ? client.getNumeroCarteFidelite() : "Aucune"));

        List<Retour> retours = retourDAO.getRetoursParClient(client.getIdClient());
        ObservableList<String> avisAffichage = FXCollections.observableArrayList();
        for (Retour r : retours) {
            String etoiles = "★".repeat(r.getSatisfaction()) + "☆".repeat(5 - r.getSatisfaction());
            avisAffichage.add(etoiles + "  " + r.getDateRetour() + "\n" + r.getCommentaire());
        }
        if (avisAffichage.isEmpty())
            avisAffichage.add("Aucun avis pour ce client.");
        listAvis.setItems(avisAffichage);
    }

    private void setStatut(String message) {
        lblStatut.setText(message);
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
