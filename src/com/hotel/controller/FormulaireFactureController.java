package com.hotel.controller;

import com.hotel.dao.FactureDAO;
import com.hotel.model.Facture;
import com.hotel.utils.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class FormulaireFactureController implements Initializable {

    @FXML
    private Label lblTitre;
    @FXML
    private ComboBox<String> comboReservation;
    @FXML
    private DatePicker dateEmission;
    @FXML
    private TextField txtMontantTotal;
    @FXML
    private ComboBox<String> comboStatut;
    @FXML
    private Label lblErreur;

    private final FactureDAO factureDAO = new FactureDAO();
    private Facture factureEnCours;
    private Runnable onSauvegardeCallback;

    // Map pour lier le label affiché à l'id_reservation
    private final Map<String, Integer> reservationsMap = new LinkedHashMap<>();
    // Map pour stocker le montant suggéré par réservation
    private final Map<String, Double> montantsMap = new LinkedHashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboStatut.setItems(FXCollections.observableArrayList("NON_PAYE", "PARTIEL", "PAYE"));
        comboStatut.setValue("NON_PAYE");
        dateEmission.setValue(LocalDate.now());
        chargerReservations();
        
        comboReservation.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && montantsMap.containsKey(newVal)) {
                txtMontantTotal.setText(String.valueOf(montantsMap.get(newVal)));
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // Chargement des réservations disponibles
    // ════════════════════════════════════════════════════════════════════════

    private void chargerReservations() {
        reservationsMap.clear();
        montantsMap.clear();
        String sql = "SELECT r.id_reservation, c.nom, c.prenom, ch.numero, r.date_debut, r.date_fin, ch.prix_par_nuit "
                + "FROM reservation r "
                + "JOIN client c ON r.id_client = c.id_client "
                + "JOIN chambre ch ON r.id_chambre = ch.id_chambre "
                + "WHERE r.id_reservation NOT IN (SELECT id_reservation FROM facture) "
                + "ORDER BY r.id_reservation DESC";
        try (Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String label = "Rés. #" + rs.getInt("id_reservation")
                        + " — " + rs.getString("prenom") + " " + rs.getString("nom")
                        + " (Ch. " + rs.getString("numero") + ")";
                reservationsMap.put(label, rs.getInt("id_reservation"));
                
                // Calcul du montant : nombre de jours * prix par nuit
                java.sql.Date dateDebut = rs.getDate("date_debut");
                java.sql.Date dateFin = rs.getDate("date_fin");
                double prixParNuit = rs.getDouble("prix_par_nuit");
                long nbJours = 1;
                if (dateDebut != null && dateFin != null) {
                    nbJours = java.time.temporal.ChronoUnit.DAYS.between(dateDebut.toLocalDate(), dateFin.toLocalDate());
                    if (nbJours <= 0) nbJours = 1; // Minimum 1 nuit
                }
                double montantSuggere = nbJours * prixParNuit;
                montantsMap.put(label, montantSuggere);
            }
        } catch (SQLException e) {
            System.err.println("[FormulaireFacture] chargerReservations : " + e.getMessage());
        }
        comboReservation.setItems(FXCollections.observableArrayList(reservationsMap.keySet()));
    }

    // ════════════════════════════════════════════════════════════════════════
    // API publique
    // ════════════════════════════════════════════════════════════════════════

    public void remplirFormulaire(Facture facture) {
        this.factureEnCours = facture;
        lblTitre.setText("Modifier la facture");
        // En mode modification on ne change pas la réservation
        comboReservation.setDisable(true);
        comboReservation.setValue("Réservation #" + facture.getIdReservation());
        dateEmission.setValue(facture.getDateEmission());
        txtMontantTotal.setText(String.valueOf(facture.getMontantTotal()));
        comboStatut.setValue(facture.getStatutPaiement());
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

        boolean succes;
        if (factureEnCours == null) {
            // Ajout
            Facture nouvelle = new Facture();
            nouvelle.setDateEmission(dateEmission.getValue());
            nouvelle.setMontantTotal(Double.parseDouble(txtMontantTotal.getText().trim()));
            nouvelle.setStatutPaiement(comboStatut.getValue());
            nouvelle.setIdReservation(reservationsMap.get(comboReservation.getValue()));
            succes = factureDAO.ajouterFacture(nouvelle);
        } else {
            // Modification
            factureEnCours.setDateEmission(dateEmission.getValue());
            factureEnCours.setMontantTotal(Double.parseDouble(txtMontantTotal.getText().trim()));
            factureEnCours.setStatutPaiement(comboStatut.getValue());
            succes = factureDAO.modifierStatut(
                    factureEnCours.getIdFacture(), comboStatut.getValue());
        }

        if (succes) {
            if (onSauvegardeCallback != null)
                onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de la sauvegarde.");
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
        if (factureEnCours == null && comboReservation.getValue() == null) {
            afficherErreur("Veuillez choisir une réservation.");
            return false;
        }
        if (dateEmission.getValue() == null) {
            afficherErreur("La date d'émission est obligatoire.");
            return false;
        }
        try {
            double montant = Double.parseDouble(txtMontantTotal.getText().trim());
            if (montant <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherErreur("Le montant doit être un nombre positif.");
            txtMontantTotal.requestFocus();
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
        ((Stage) txtMontantTotal.getScene().getWindow()).close();
    }
}
