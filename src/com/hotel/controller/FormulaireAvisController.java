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

public class FormulaireAvisController implements Initializable {

    @FXML private Label             lblNomClient;
    @FXML private ComboBox<Integer> comboSatisfaction;
    @FXML private TextArea          txtCommentaire;
    @FXML private Label             lblErreur;

    private final RetourDAO retourDAO = new RetourDAO();
    private Client   clientCible;
    private Runnable onSauvegardeCallback;

    // ════════════════════════════════════════════════════════════════════════
    //  Initialisation
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboSatisfaction.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        comboSatisfaction.setValue(5);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  API publique
    // ════════════════════════════════════════════════════════════════════════

    public void setClient(Client client) {
        this.clientCible = client;
        lblNomClient.setText("Client : " + client.getNomComplet());
    }

    public void setOnSauvegardeCallback(Runnable callback) {
        this.onSauvegardeCallback = callback;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Actions FXML
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void enregistrer() {
        if (!validerChamps()) return;

        Retour retour = new Retour();
        retour.setDateRetour(LocalDate.now());
        retour.setCommentaire(txtCommentaire.getText().trim());
        retour.setSatisfaction(comboSatisfaction.getValue());
        retour.setIdClient(clientCible.getIdClient());

        if (retourDAO.ajouterRetour(retour)) {
            if (onSauvegardeCallback != null) onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de l'enregistrement de l'avis.");
        }
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Utilitaires privés
    // ════════════════════════════════════════════════════════════════════════

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

    private void afficherErreur(String msg) {
        lblErreur.setText("⚠ " + msg);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }

    private void masquerErreur() {
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);
    }

    private void fermerFenetre() {
        ((Stage) txtCommentaire.getScene().getWindow()).close();
    }
}
