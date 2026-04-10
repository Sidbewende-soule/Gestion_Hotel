package com.hotel.controller;

import com.hotel.dao.ClientDAO;
import com.hotel.model.Client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Contrôleur pour le formulaire d'ajout ou modification d'un client (FormulaireClient.fxml).
 */
public class FormulaireClientController {

    @FXML private Label     lblTitreFormulaire; // "Nouveau client" ou "Modifier client"
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtCarteFidelite;
    @FXML private Label     lblErreur; // Zone d'erreur contextuelle
    @FXML private javafx.scene.control.Button btnEnregistrer;

    private final ClientDAO clientDAO = new ClientDAO();
    private Client  clientEnCours; // Si null -> Ajout, sinon -> Modification
    private Runnable onSauvegardeCallback; // Action à exécuter après succès

    /**
     * Pré-remplit les champs pour une modification.
     * @param client L'objet client à modifier
     */
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

    /**
     * Définit le retour d'appel après enregistrement réussi.
     */
    public void setOnSauvegardeCallback(Runnable callback) {
        this.onSauvegardeCallback = callback;
    }

    /**
     * Valide et persiste les données du client (Ajout ou Maj).
     */
    @FXML
    private void enregistrer() {
        if (!validerChamps()) return; // Vérification des champs obligatoires

        boolean succes;
        if (clientEnCours == null) {
            // Mode Création
            Client nouveau = new Client();
            remplirDepuisChamps(nouveau);
            succes = clientDAO.ajouterClient(nouveau);
        } else {
            // Mode Modification
            remplirDepuisChamps(clientEnCours);
            succes = clientDAO.modifierClient(clientEnCours);
        }

        if (succes) {
            // Notification du succès au contrôleur parent
            if (onSauvegardeCallback != null) onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de la sauvegarde. Vérifiez la connexion à la base.");
        }
    }

    /**
     * Ferme le dialogue sans enregistrer.
     */
    @FXML
    private void annuler() {
        fermerFenetre();
    }

    /**
     * Contrôle la validité des champs textuels.
     */
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

    /**
     * Transfère le contenu des champs UI vers l'objet modèle.
     */
    private void remplirDepuisChamps(Client client) {
        client.setNom(txtNom.getText().trim());
        client.setPrenom(txtPrenom.getText().trim());
        client.setTelephone(txtTelephone.getText().trim());
        client.setEmail(txtEmail.getText().trim());
        client.setAdresse(txtAdresse.getText().trim());
        client.setNumeroCarteFidelite(txtCarteFidelite.getText().trim());
    }

    /**
     * Affiche un message d'alerte rouge.
     */
    private void afficherErreur(String message) {
        lblErreur.setText("⚠ " + message);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }

    /**
     * Cache le message d'alerte.
     */
    private void masquerErreur() {
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);
    }

    /**
     * Ferme la fenêtre pop-up actuelle.
     */
    private void fermerFenetre() {
        ((Stage) txtNom.getScene().getWindow()).close();
    }
}

