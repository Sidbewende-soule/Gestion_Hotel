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

/**
 * Contrôleur pour la gestion de la base client (GestionClients.fxml).
 * Permet de lister, rechercher, modifier les clients et consulter leurs avis.
 */
@SuppressWarnings("unused")
public class GestionClientsController implements Initializable {

    // --- Composants FXML : Liste et Recherche ---
    @FXML private TableView<Client> tableClients;
    @FXML private TableColumn<Client, Integer> colId;
    @FXML private TableColumn<Client, String> colNom;
    @FXML private TableColumn<Client, String> colPrenom;
    @FXML private TableColumn<Client, String> colTelephone;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private TableColumn<Client, String> colAdresse;
    @FXML private TableColumn<Client, String> colCarteFidelite;
    @FXML private TableColumn<Client, Void> colActions; // Colonne pour les boutons d'action
    @FXML private TextField txtRecherche;
    @FXML private Label lblNombreClients;
    @FXML private Label lblStatut; // Texte d'information en bas de page

    // --- Composants FXML : Panneau de détails (à droite) ---
    @FXML private VBox panneauDetail;
    @FXML private Label lblDetailNom;
    @FXML private Label lblDetailTel;
    @FXML private Label lblDetailEmail;
    @FXML private Label lblDetailAdresse;
    @FXML private Label lblDetailFidelite;
    @FXML private ListView<String> listAvis; // Historique des feedbacks du client

    // --- DAOs ---
    private final ClientDAO clientDAO = new ClientDAO();
    private final RetourDAO retourDAO = new RetourDAO();

    // --- Données internes ---
    private ObservableList<Client> listeClients = FXCollections.observableArrayList();
    private Client clientSelectionne;

    /**
     * Initialisation du contrôleur.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes(); // Setup du tableau
        chargerClients();    // Chargement initial
    }

    /**
     * Définit la structure des colonnes et injecte les boutons d'action (Modifier/Supprimer).
     */
    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colCarteFidelite.setCellValueFactory(new PropertyValueFactory<>("numeroCarteFidelite"));

        // Création dynamique de boutons Modifier et Supprimer pour chaque ligne
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnModif = new Button("✏️");
            private final Button btnSuppr = new Button("🗑️");
            private final HBox box = new HBox(6, btnModif, btnSuppr);

            {
                btnModif.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
                btnSuppr.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");

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

    /**
     * Récupère la totalité des clients depuis la base de données.
     */
    private void chargerClients() {
        listeClients.setAll(clientDAO.getTousLesClients());
        mettreAJourCompteur();
        setStatut("Clients chargés avec succès.");
    }

    /**
     * Affiche le nombre total de clients dans le libellé du haut.
     */
    private void mettreAJourCompteur() {
        lblNombreClients.setText(listeClients.size() + " client(s)");
    }

    /**
     * Filtre la liste des clients selon les mots-clés saisis.
     */
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

    /**
     * Efface le champ de recherche et recharge tout.
     */
    @FXML
    private void reinitialiserRecherche() {
        txtRecherche.clear();
        chargerClients();
    }

    /**
     * Déclenchée lors d'un clic sur une ligne du tableau.
     */
    @FXML
    private void onClientSelectionne() {
        Client client = tableClients.getSelectionModel().getSelectedItem();
        if (client == null) return;
        clientSelectionne = client;
        afficherDetail(client); // Met à jour le panneau latéral
    }

    /**
     * Ouvre la fenêtre pop-up pour créer un nouveau client.
     */
    @FXML
    public void ouvrirFormulaireAjout() {
        ouvrirFormulaire(null);
    }

    /**
     * Ouvre la fenêtre pop-up pour modifier le client sélectionné.
     */
    @FXML
    public void ouvrirFormulaireModification() {
        if (clientSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un client à modifier.");
            return;
        }
        ouvrirFormulaire(clientSelectionne);
    }

    /**
     * Supprime le client après confirmation si aucune réservation n'est liée.
     */
    @FXML
    public void supprimerClient() {
        if (clientSelectionne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un client à supprimer.");
            return;
        }
        
        // Sécurité : on empêche la suppression si l'intégrité référentielle est menacée
        if (clientDAO.aDesReservations(clientSelectionne.getIdClient())) {
            afficherAlerte(Alert.AlertType.ERROR, "Suppression impossible", "Ce client possède des réservations actives.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le client ?");
        confirm.setContentText("Action irréversible pour " + clientSelectionne.getNomComplet());
        
        Optional<ButtonType> reponse = confirm.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            if (clientDAO.supprimerClient(clientSelectionne.getIdClient())) {
                chargerClients();
                panneauDetail.setVisible(false);
                clientSelectionne = null;
                setStatut("Client supprimé.");
            } else {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "La suppression a échoué.");
            }
        }
    }

    /**
     * Ouvre le formulaire de saisie d'avis pour le client sélectionné.
     */
    @FXML
    public void ouvrirFormulaireAvis() {
        if (clientSelectionne == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/view/FormulaireAvis.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/hotel/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Ajouter un commentaire client");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            FormulaireAvisController ctrl = loader.getController();
            ctrl.setClient(clientSelectionne);
            // On rafraîchit le panneau de détails au retour pour voir le nouvel avis
            ctrl.setOnSauvegardeCallback(() -> afficherDetail(clientSelectionne));
            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("[GestionClientsController] Impossible d'ouvrir FormulaireAvis : " + e.getMessage());
        }
    }

    /**
     * Charge une vue modale de formulaire (Ajout ou Edition).
     */
    private void ouvrirFormulaire(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/view/FormulaireClient.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/hotel/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(client == null ? "Nouveau client" : "Modification client");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            FormulaireClientController ctrl = loader.getController();
            if (client != null) ctrl.remplirFormulaire(client);
            ctrl.setOnSauvegardeCallback(this::chargerClients);
            stage.showAndWait();
        } catch (IOException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur UI", "Erreur lors de l'ouverture du formulaire.");
        }
    }

    /**
     * Met à jour le panneau latéral avec les infos détaillés et l'historique des avis.
     */
    private void afficherDetail(Client client) {
        panneauDetail.setVisible(true);
        lblDetailNom.setText("👤 " + client.getNomComplet());
        lblDetailTel.setText("📞 " + (client.getTelephone() != null ? client.getTelephone() : "—"));
        lblDetailEmail.setText("✉️ " + (client.getEmail() != null ? client.getEmail() : "—"));
        lblDetailAdresse.setText("📍 " + (client.getAdresse() != null ? client.getAdresse() : "—"));
        lblDetailFidelite.setText("🎖️ Carte : " + (client.getNumeroCarteFidelite() != null ? client.getNumeroCarteFidelite() : "Aucune"));

        // Récupération des feedbacks
        List<Retour> retours = retourDAO.getRetoursParClient(client.getIdClient());
        ObservableList<String> avisAffichage = FXCollections.observableArrayList();
        for (Retour r : retours) {
            // Rendu visuel de la note avec des caractères étoiles
            String etoiles = "★".repeat(r.getSatisfaction()) + "☆".repeat(5 - r.getSatisfaction());
            avisAffichage.add(etoiles + "  " + r.getDateRetour() + "\n" + r.getCommentaire());
        }
        if (avisAffichage.isEmpty()) avisAffichage.add("Aucun avis pour ce client.");
        listAvis.setItems(avisAffichage);
    }

    /**
     * Affiche un message d'état temporaire.
     */
    private void setStatut(String message) {
        lblStatut.setText(message);
    }

    /**
     * Utilitaire de boîte de dialogue.
     */
    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

