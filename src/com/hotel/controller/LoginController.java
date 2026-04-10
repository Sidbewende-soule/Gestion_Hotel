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

public class LoginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblErreur;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        // Appuyer sur Entrée pour se connecter
        txtPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblErreur.setText("⚠ Veuillez remplir tous les champs.");
            lblErreur.setVisible(true);
            lblErreur.setManaged(true);
            return;
        }

        Utilisateur user = utilisateurDAO.authentifier(email, password);

        if (user != null) {
            lblErreur.setVisible(false);
            lblErreur.setManaged(false);

            try {
                // On passe l'utilisateur à MainApp pour charger la vue correspondante
                MainApp.getInstance().loginSuccess(user);
            } catch (Exception e) {
                e.printStackTrace();
                lblErreur.setText("⚠ Erreur système lors de l'ouverture.");
                lblErreur.setVisible(true);
                lblErreur.setManaged(true);
            }
        } else {
            lblErreur.setText("⚠ Identifiants incorrects.");
            lblErreur.setVisible(true);
            lblErreur.setManaged(true);
        }
    }
}
