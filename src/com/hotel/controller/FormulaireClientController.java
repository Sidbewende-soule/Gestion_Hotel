package com.hotel.controller;

import com.hotel.dao.ClientDAO;
import com.hotel.model.Client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormulaireClientController {

    @FXML private Label     lblTitreFormulaire;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtCarteFidelite;
    @FXML private Label     lblErreur;
    @FXML private javafx.scene.control.Button btnEnregistrer;

    private final ClientDAO clientDAO = new ClientDAO();
    private Client  clientEnCours;
    private Runnable onSauvegardeCallback;

    // ════════════════════════════════════════════════════════════════════════
    //  API publique
    // ════════════════════════════════════════════════════════════════════════

    public void remplirFormulaire(Client client) {
        this.clientEnCours = client;
        lblTitreFormulaire.setText("Modifier le client");
        txtNom.setText(client.getNom());
        txtPrenom.setText(client.getPrenom());
        txtTelephone.setText(client.getTelephone());
        txtEmail.setText(client.getEmail());
        txtAdresse.setText(client.getAdresse());
        txtCarteFidelite.setText(client.getNumeroCarteFidelite());
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

        boolean succes;
        if (clientEnCours == null) {
            Client nouveau = new Client();
            remplirDepuisChamps(nouveau);
            succes = clientDAO.ajouterClient(nouveau);
        } else {
            remplirDepuisChamps(clientEnCours);
            succes = clientDAO.modifierClient(clientEnCours);
        }

        if (succes) {
            if (onSauvegardeCallback != null) onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de la sauvegarde. Vérifiez la connexion à la base.");
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
        if (txtNom.getText().trim().isEmpty()) {
            afficherErreur("Le champ « Nom » est obligatoire.");
            txtNom.requestFocus();
            return false;
        }
        if (txtPrenom.getText().trim().isEmpty()) {
            afficherErreur("Le champ « Prénom » est obligatoire.");
            txtPrenom.requestFocus();
            return false;
        }
        masquerErreur();
        return true;
    }

    private void remplirDepuisChamps(Client client) {
        client.setNom(txtNom.getText().trim());
        client.setPrenom(txtPrenom.getText().trim());
        client.setTelephone(txtTelephone.getText().trim());
        client.setEmail(txtEmail.getText().trim());
        client.setAdresse(txtAdresse.getText().trim());
        client.setNumeroCarteFidelite(txtCarteFidelite.getText().trim());
    }

    private void afficherErreur(String message) {
        lblErreur.setText("⚠ " + message);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }

    private void masquerErreur() {
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);
    }

    private void fermerFenetre() {
        ((Stage) txtNom.getScene().getWindow()).close();
    }
}
