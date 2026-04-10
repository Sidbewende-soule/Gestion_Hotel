package com.hotel.controller;

import com.hotel.dao.RetourDAO;
import com.hotel.model.Client;
import com.hotel.model.Retour;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le formulaire de saisie d'un avis client (FormulaireAvis.fxml).
 */
public class FormulaireAvisController implements Initializable {

    @FXML private Label             lblNomClient;       // Affiche le nom du client concerné
    @FXML private ComboBox<Integer> comboSatisfaction; // Note de 1 à 5
    @FXML private TextArea          txtCommentaire;    // Avis textuel
    @FXML private Label             lblErreur;          // Feedback utilisateur

    private final RetourDAO retourDAO = new RetourDAO();
    private Client   clientCible; // Le client pour lequel on saisit l'avis
    private Runnable onSauvegardeCallback; // Action à exécuter après succès (ex: rafraîchir liste)

    /**
     * Initialise les valeurs par défaut du formulaire.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation de la plage de satisfaction de 1 à 5
        comboSatisfaction.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        comboSatisfaction.setValue(5); // Valeur par défaut
    }

    /**
     * Injecte les données du client depuis le contrôleur parent.
     */
    public void setClient(Client client) {
        this.clientCible = client;
        lblNomClient.setText("Client : " + client.getNomComplet());
    }

    /**
     * Permet de définir une action à déclencher après la sauvegarde réussie.
     */
    public void setOnSauvegardeCallback(Runnable callback) {
        this.onSauvegardeCallback = callback;
    }

    /**
     * Enregistre l'avis en base de données.
     */
    @FXML
    private void enregistrer() {
        if (!validerChamps()) return; // Vérification de la saisie

        // Création de l'objet Retour
        Retour retour = new Retour();
        retour.setDateRetour(LocalDate.now());
        retour.setCommentaire(txtCommentaire.getText().trim());
        retour.setSatisfaction(comboSatisfaction.getValue());
        retour.setIdClient(clientCible.getIdClient());

        // Insertion via DAO
        if (retourDAO.ajouterRetour(retour)) {
            if (onSauvegardeCallback != null) onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de l'enregistrement de l'avis.");
        }
    }

    /**
     * Ferme le formulaire sans enregistrer.
     */
    @FXML
    private void annuler() {
        fermerFenetre();
    }

    /**
     * Vérifie que le commentaire n'est pas vide et qu'une note est choisie.
     */
    private boolean validerChamps() {
        if (comboSatisfaction.getValue() == null) {
            afficherErreur("Veuillez choisir une note de satisfaction.");
            return false;
        }
        if (txtCommentaire.getText().trim().isEmpty()) {
            afficherErreur("Le commentaire est obligatoire.");
            txtCommentaire.requestFocus();
            return false;
        }
        masquerErreur();
        return true;
    }

    /**
     * Affiche visuellement une erreur dans le formulaire.
     */
    private void afficherErreur(String msg) {
        lblErreur.setText("⚠ " + msg);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }

    /**
     * Cache le message d'erreur.
     */
    private void masquerErreur() {
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);
    }

    /**
     * Ferme la fenêtre pop-up courante.
     */
    private void fermerFenetre() {
        ((Stage) txtCommentaire.getScene().getWindow()).close();
    }
}

