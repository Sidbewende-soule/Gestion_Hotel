package com.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Modèle représentant une Facture de l'hôtel.
 * Ce modèle mappe directement la table `facture` de la base de données.
 */
public class Facture {

    // Propriétés JavaFX pour l'affichage dynamique dans les tableaux
    private final IntegerProperty idFacture = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> dateEmission = new SimpleObjectProperty<>();
    private final DoubleProperty montantTotal = new SimpleDoubleProperty();
    private final StringProperty statutPaiement = new SimpleStringProperty(); // PAYE, NON_PAYE, PARTIEL
    private final IntegerProperty idReservation = new SimpleIntegerProperty();

    // Champs transient (non stockés tels quels en table) pour faciliter l'affichage UI
    private String nomClient;       // Nom du client lié à la facture
    private String numerosChambre; // Numéro(s) de chambre lié(s) à la facture

    /**
     * Constructeur par défaut.
     */
    public Facture() {
    }

    /**
     * Constructeur pour initialiser une facture complète.
     */
    public Facture(int idFacture, LocalDate dateEmission, double montantTotal,
            String statutPaiement, int idReservation) {
        setIdFacture(idFacture);
        setDateEmission(dateEmission);
        setMontantTotal(montantTotal);
        setStatutPaiement(statutPaiement);
        setIdReservation(idReservation);
    }

    // --- Accesseurs de Properties JavaFX ---
    public IntegerProperty idFactureProperty() {
        return idFacture;
    }

    public ObjectProperty<LocalDate> dateEmissionProperty() {
        return dateEmission;
    }

    public DoubleProperty montantTotalProperty() {
        return montantTotal;
    }

    public StringProperty statutPaiementProperty() {
        return statutPaiement;
    }

    public IntegerProperty idReservationProperty() {
        return idReservation;
    }

    // --- Getters et Setters classiques ---
    public int getIdFacture() {
        return idFacture.get();
    }

    public final void setIdFacture(int v) {
        idFacture.set(v);
    }

    public LocalDate getDateEmission() {
        return dateEmission.get();
    }

    public final void setDateEmission(LocalDate v) {
        dateEmission.set(v);
    }

    public double getMontantTotal() {
        return montantTotal.get();
    }

    public final void setMontantTotal(double v) {
        montantTotal.set(v);
    }

    public String getStatutPaiement() {
        return statutPaiement.get();
    }

    public final void setStatutPaiement(String v) {
        statutPaiement.set(v);
    }

    public int getIdReservation() {
        return idReservation.get();
    }

    public final void setIdReservation(int v) {
        idReservation.set(v);
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String v) {
        nomClient = v;
    }

    public String getNumerosChambre() {
        return numerosChambre;
    }

    public void setNumerosChambre(String v) {
        numerosChambre = v;
    }
}

