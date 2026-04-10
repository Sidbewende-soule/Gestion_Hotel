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

/**
 * Contrôleur pour l'enregistrement d'un nouveau paiement (FormulairePaiement.fxml).
 * Permet de saisir un montant partiel ou total associé à une facture.
 */
public class FormulairePaiementController implements Initializable {

    @FXML private Label     lblInfoFacture; // Résumé textuel de la facture
    @FXML private Label     lblResteAPayer; // Calcul dynamique du solde
    @FXML private DatePicker datePaiement;
    @FXML private TextField txtMontant;
    @FXML private ComboBox<String> comboModePaiement;
    @FXML private Label     lblErreur;

    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final FactureDAO factureDAO = new FactureDAO();

    private Facture factureCible; // Facture associée
    private double resteAPayer;   // Solde théorique
    private Runnable onSauvegardeCallback; // Action post-enregistrement

    /**
     * Initialisation par défaut.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboModePaiement.setItems(
                FXCollections.observableArrayList("ESPECES", "CARTE", "MOBILE_MONEY"));
        comboModePaiement.setValue("ESPECES");
        datePaiement.setValue(LocalDate.now());
    }

    /**
     * Reçoit la facture concernée et calcule le montant déjà versé.
     */
    public void setFacture(Facture facture) {
        this.factureCible = facture;
        // Appel au DAO pour sommer tous les paiements passés liés à cette facture
        double dejaPaye = factureDAO.getMontantDejaPaye(facture.getIdFacture());
        resteAPayer = facture.getMontantTotal() - dejaPaye;

        lblInfoFacture.setText("Facture #" + facture.getIdFacture()
                + "  —  " + facture.getNomClient()
                + "  —  Total : " + String.format("%,.0f FCFA", facture.getMontantTotal()));
        lblResteAPayer.setText("Reste à payer : " + String.format("%,.0f FCFA", resteAPayer));

        // Pré-remplir le champ de saisie avec le solde exact par défaut
        txtMontant.setText(String.format("%.0f", resteAPayer));
    }

    /**
     * Définit le callback de rafraîchissement.
     */
    public void setOnSauvegardeCallback(Runnable callback) {
        this.onSauvegardeCallback = callback;
    }

    /**
     * Valide et enregistre le paiement en base.
     */
    @FXML
    private void enregistrer() {
        if (!validerChamps()) return;

        double montantSaisi = Double.parseDouble(txtMontant.getText().trim());

        // Alerte de sécurité : si l'utilisateur saisit plus que ce qui est dû
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

        // Création de l'objet métier Paiement
        Paiement paiement = new Paiement();
        paiement.setDatePaiement(datePaiement.getValue());
        paiement.setMontant(montantSaisi);
        paiement.setModePaiement(comboModePaiement.getValue());
        paiement.setIdFacture(factureCible.getIdFacture());

        // NOTE : Le trigger SQL 'update_statut_facture' en base de données mettra à jour 
        // automatiquement le statut de la facture (NON_PAYE, PARTIEL, PAYE) lors de l'insertion.
        if (paiementDAO.ajouterPaiement(paiement)) {
            if (onSauvegardeCallback != null) onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de l'enregistrement du paiement.");
        }
    }

    /**
     * Ferme le dialogue.
     */
    @FXML
    private void annuler() {
        fermerFenetre();
    }

    /**
     * Contrôle la validité des données saisies (numérique, positif, non nul).
     */
    private boolean validerChamps() {
        if (datePaiement.getValue() == null) {
            afficherErreur("La date de paiement est obligatoire.");
            return false;
        }
        try {
            double montant = Double.parseDouble(txtMontant.getText().trim());
            if (montant <= 0) throw new NumberFormatException();
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

    /**
     * Affiche un message d'alerte.
     */
    private void afficherErreur(String msg) {
        lblErreur.setText("⚠ " + msg);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }

    /**
     * Cache l'alerte.
     */
    private void masquerErreur() {
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);
    }

    /**
     * Ferme l'écran pop-up.
     */
    private void fermerFenetre() {
        ((Stage) txtMontant.getScene().getWindow()).close();
    }
}

