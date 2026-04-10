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

/**
 * Contrôleur pour le formulaire de création ou modification d'une facture (FormulaireFacture.fxml).
 * Gère le calcul automatique du montant en fonction de la durée du séjour et de la chambre.
 */
public class FormulaireFactureController implements Initializable {

    @FXML private Label     lblTitre;           // Titre dynamique du formulaire
    @FXML private ComboBox<String> comboReservation; // Liste des réservations sans facture
    @FXML private DatePicker dateEmission;
    @FXML private TextField txtMontantTotal;
    @FXML private ComboBox<String> comboStatut;
    @FXML private Label     lblErreur;

    private final FactureDAO factureDAO = new FactureDAO();
    private Facture factureEnCours;
    private Runnable onSauvegardeCallback;

    // Map pour lier le label textuel de la combo (ex: "Rés. #12...") à l'ID technique
    private final Map<String, Integer> reservationsMap = new LinkedHashMap<>();
    
    // Map pour stocker le montant calculé automatiquement par réservation
    private final Map<String, Double> montantsMap = new LinkedHashMap<>();

    /**
     * Initialisation du contrôleur.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Chargement des statuts de paiement autorisés
        comboStatut.setItems(FXCollections.observableArrayList("NON_PAYE", "PARTIEL", "PAYE"));
        comboStatut.setValue("NON_PAYE");
        dateEmission.setValue(LocalDate.now());
        
        // Chargement des réservations n'ayant pas encore de facture
        chargerReservations();
        
        // Listener : dès qu'une réservation est choisie, on suggère le montant total calculé
        comboReservation.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && montantsMap.containsKey(newVal)) {
                txtMontantTotal.setText(String.valueOf(montantsMap.get(newVal)));
            }
        });
    }

    /**
     * Recherche les réservations "éligibles" (sans facture) et calcule leur montant théorique.
     */
    private void chargerReservations() {
        reservationsMap.clear();
        montantsMap.clear();
        
        // Requête SQL complexe pour récupérer les infos de calcul de prix
        String sql = "SELECT r.id_reservation, c.nom, c.prenom, ch.numero, r.date_debut, r.date_fin, ch.prix_par_nuit "
                + "FROM reservation r "
                + "JOIN client c ON r.id_client = c.id_client "
                + "JOIN chambre ch ON r.id_chambre = ch.id_chambre "
                + "WHERE r.id_reservation NOT IN (SELECT id_reservation FROM facture) "
                + "ORDER BY r.id_reservation DESC";
                
        try (Statement st = DatabaseConnection.getInstance().getConnection().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                // Création d'un label lisible pour l'utilisateur
                String label = "Rés. #" + rs.getInt("id_reservation")
                        + " — " + rs.getString("prenom") + " " + rs.getString("nom")
                        + " (Ch. " + rs.getString("numero") + ")";
                
                reservationsMap.put(label, rs.getInt("id_reservation"));
                
                // --- Calcul du montant suggéré ---
                java.sql.Date dateDebut = rs.getDate("date_debut");
                java.sql.Date dateFin = rs.getDate("date_fin");
                double prixParNuit = rs.getDouble("prix_par_nuit");
                
                long nbJours = 1;
                if (dateDebut != null && dateFin != null) {
                    // Calcul de la différence entre les dates
                    nbJours = java.time.temporal.ChronoUnit.DAYS.between(dateDebut.toLocalDate(), dateFin.toLocalDate());
                    if (nbJours <= 0) nbJours = 1; // Un séjour d'un jour minimum (ex: départ même jour)
                }
                double montantSuggere = nbJours * prixParNuit;
                montantsMap.put(label, montantSuggere);
            }
        } catch (SQLException e) {
            System.err.println("[FormulaireFactureController] Erreur SQL : " + e.getMessage());
        }
        
        // Mise à jour de la liste dans la ComboBox
        comboReservation.setItems(FXCollections.observableArrayList(reservationsMap.keySet()));
    }

    /**
     * Pré-remplit le formulaire avec les données d'une facture existante.
     */
    public void remplirFormulaire(Facture facture) {
        this.factureEnCours = facture;
        lblTitre.setText("Modifier la facture");
        
        // En mode modification, la réservation associée est verrouillée
        comboReservation.setDisable(true);
        comboReservation.setValue("Réservation #" + facture.getIdReservation());
        
        dateEmission.setValue(facture.getDateEmission());
        txtMontantTotal.setText(String.valueOf(facture.getMontantTotal()));
        comboStatut.setValue(facture.getStatutPaiement());
    }

    /**
     * Définit l'action à exécuter après un enregistrement réussi.
     */
    public void setOnSauvegardeCallback(Runnable callback) {
        this.onSauvegardeCallback = callback;
    }

    /**
     * Enregistre la facture (Ajout ou Modification).
     */
    @FXML
    private void enregistrer() {
        if (!validerChamps()) return;

        boolean succes;
        if (factureEnCours == null) {
            // Création d'une nouvelle facture
            Facture nouvelle = new Facture();
            nouvelle.setDateEmission(dateEmission.getValue());
            nouvelle.setMontantTotal(Double.parseDouble(txtMontantTotal.getText().trim()));
            nouvelle.setStatutPaiement(comboStatut.getValue());
            nouvelle.setIdReservation(reservationsMap.get(comboReservation.getValue()));
            succes = factureDAO.ajouterFacture(nouvelle);
        } else {
            // Mise à jour simplifiée (souvent le statut)
            factureEnCours.setDateEmission(dateEmission.getValue());
            factureEnCours.setMontantTotal(Double.parseDouble(txtMontantTotal.getText().trim()));
            factureEnCours.setStatutPaiement(comboStatut.getValue());
            succes = factureDAO.modifierStatut(factureEnCours.getIdFacture(), comboStatut.getValue());
        }

        if (succes) {
            if (onSauvegardeCallback != null) onSauvegardeCallback.run();
            fermerFenetre();
        } else {
            afficherErreur("Erreur lors de la sauvegarde. Veuillez vérifier les logs.");
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
     * Valide la cohérence des saisies.
     */
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
            if (montant <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            afficherErreur("Le montant doit être un nombre positif.");
            txtMontantTotal.requestFocus();
            return false;
        }
        masquerErreur();
        return true;
    }

    /**
     * Affiche un message d'erreur rouge dans le formulaire.
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
     * Ferme la fenêtre pop-up.
     */
    private void fermerFenetre() {
        ((Stage) txtMontantTotal.getScene().getWindow()).close();
    }
}

