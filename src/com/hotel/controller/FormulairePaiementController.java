package com.hotel.controller;

import com.hotel.dao.FactureDAO;
import com.hotel.dao.PaiementDAO;
import com.hotel.model.Facture;
import com.hotel.model.Paiement;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class FormulairePaiementController implements Initializable {

    @FXML
    private Label lblInfoFacture;
    @FXML
    private Label lblResteAPayer;
    @FXML
    private DatePicker datePaiement;
    @FXML
    private TextField txtMontant;
    @FXML
    private ComboBox<String> comboModePaiement;
    @FXML
    private Label lblErreur;

    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final FactureDAO factureDAO = new FactureDAO();

    private Facture factureCible;
    private double resteAPayer;
    private Runnable onSauvegardeCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboModePaiement.setItems(
                FXCollections.observableArrayList("ESPECES", "CARTE", "MOBILE_MONEY"));
        comboModePaiement.setValue("ESPECES");
        datePaiement.setValue(LocalDate.now());
    }

    // ════════════════════════════════════════════════════════════════════════
    // API publique
    // ════════════════════════════════════════════════════════════════════════

    public void setFacture(Facture facture) {
        this.factureCible = facture;
        double dejaPaye = factureDAO.getMontantDejaPaye(facture.getIdFacture());
        resteAPayer = facture.getMontantTotal() - dejaPaye;

        lblInfoFacture.setText("Facture #" + facture.getIdFacture()
                + "  —  " + facture.getNomClient()
                + "  —  Total : " + String.format("%,.0f FCFA", facture.getMontantTotal()));
        lblResteAPayer.setText("Reste à payer : " + String.format("%,.0f FCFA", resteAPayer));

        // Pré-remplir avec le montant restant
        txtMontant.setText(String.format("%.0f", resteAPayer));
    }

    public void setOnSauvegardeCallback(Runnable callback) {
        this.onSauvegardeCallback = callback;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Actions FXML
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void enregistrer() {
        if (!validerChamps())
            return;

        double montantSaisi = Double.parseDouble(txtMontant.getText().trim());

        // Avertir si le montant dépasse le restant
        if (montantSaisi > resteAPayer) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Montant supérieur");
            alert.setHeaderText("Le montant saisi dépasse le restant dû.");
            alert.setContentText("Restant : " + String.format("%,.0f FCFA", resteAPayer)
                    + "\nSaisi : " + String.format("%,.0f FCFA", montantSaisi)
                    + "\n\nVoulez-vous continuer quand même ?");
            if (alert.showAndWait().filter(b -> b == ButtonType.OK).isEmpty())
                return;
        }

        Paiement paiement = new Paiement();
        paiement.setDatePaiement(datePaiement.getValue());
        paiement.setMontant(montantSaisi);
        paiement.setModePaiement(comboModePaiement.getValue());
        paiement.setIdFacture(factureCible.getIdFacture());

        // Le trigger SQL met à jour automatiquement le statut de la facture
        if (paiementDAO.ajouterPaiement(paiement)) {
            if (onSauvegardeCallback != null)
                onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de l'enregistrement du paiement.");
        }
    }

    @FXML
    private void annuler() {
        fermerFenetre();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Utilitaires privés
    // ════════════════════════════════════════════════════════════════════════

    private boolean validerChamps() {
        if (datePaiement.getValue() == null) {
            afficherErreur("La date de paiement est obligatoire.");
            return false;
        }
        try {
            double montant = Double.parseDouble(txtMontant.getText().trim());
            if (montant <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherErreur("Le montant doit être un nombre positif.");
            txtMontant.requestFocus();
            return false;
        }
        if (comboModePaiement.getValue() == null) {
            afficherErreur("Veuillez choisir un mode de paiement.");
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
        ((Stage) txtMontant.getScene().getWindow()).close();
    }
}
