package com.hotel.controller;

import com.hotel.MainApp;
import com.hotel.dao.UtilisateurDAO;
import com.hotel.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Contrôleur pour la vue de connexion (Login.fxml).
 * Gère la saisie des identifiants et le passage vers l'application principale.
 */
public class LoginController {

    @FXML
    private TextField txtEmail;      // Champ de saisie du nom d'utilisateur
    @FXML
    private PasswordField txtPassword; // Champ de saisie du mot de passe
    @FXML
    private Label lblErreur;          // Label d'affichage des messages d'erreur

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /**
     * Initialisation automatique par JavaFX.
     */
    @FXML
    public void initialize() {
        // Raccourci clavier : Appuyer sur Entrée dans le champ mot de passe déclenche la connexion
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    /**
     * Action déclenchée par le bouton "Se connecter" ou la touche Entrée.
     */
    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // Validation basique des champs vides
        if (email.isEmpty() || password.isEmpty()) {
            afficherErreur("⚠ Veuillez remplir tous les champs.");
            return;
        }

        // Appel au DAO pour vérifier les identifiants en base
        Utilisateur user = utilisateurDAO.authentifier(email, password);

        if (user != null) {
            // Succès : on cache les erreurs et on bascule vers l'interface principale
            lblErreur.setVisible(false);
            lblErreur.setManaged(false);

            try {
                // MainApp.loginSuccess() gère la navigation vers le tableau de bord
                MainApp.getInstance().loginSuccess(user);
            } catch (Exception e) {
                System.err.println("[LoginController] Erreur fatale : " + e.getMessage());
                afficherErreur("⚠ Erreur système lors du chargement de l'interface.");
            }
        } else {
            // Échec : identifiants incorrects
            afficherErreur("⚠ Identifiants incorrects.");
        }
    }

    /**
     * Utilitaire pour afficher un message d'erreur visuel.
     */
    private void afficherErreur(String message) {
        lblErreur.setText(message);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }
}

