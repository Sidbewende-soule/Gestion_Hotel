package com.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Modèle représentant un Paiement effectué par un client.
 * Correspond à la table `paiement` de la base de données.
 */
public class Paiement {

    // Propriétés JavaFX pour le binding UI
    private final IntegerProperty idPaiement = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> datePaiement = new SimpleObjectProperty<>();
    private final DoubleProperty montant = new SimpleDoubleProperty();
    private final StringProperty modePaiement = new SimpleStringProperty(); // ESPECES, CARTE, etc.
    private final IntegerProperty idFacture = new SimpleIntegerProperty();

    /**
     * Constructeur par défaut.
     */
    public Paiement() {
    }

    /**
     * Constructeur complet pour initialiser un paiement.
     */
    public Paiement(int idPaiement, LocalDate datePaiement, double montant,
            String modePaiement, int idFacture) {
        setIdPaiement(idPaiement);
        setDatePaiement(datePaiement);
        setMontant(montant);
        setModePaiement(modePaiement);
        setIdFacture(idFacture);
    }

    // --- Accesseurs de Properties JavaFX ---
    public IntegerProperty idPaiementProperty() {
        return idPaiement;
    }

    public ObjectProperty<LocalDate> datePaiementProperty() {
        return datePaiement;
    }

    public DoubleProperty montantProperty() {
        return montant;
    }

    public StringProperty modePaiementProperty() {
        return modePaiement;
    }

    public IntegerProperty idFactureProperty() {
        return idFacture;
    }

    // --- Getters et Setters classiques ---
    public int getIdPaiement() {
        return idPaiement.get();
    }

    public final void setIdPaiement(int v) {
        idPaiement.set(v);
    }

    public LocalDate getDatePaiement() {
        return datePaiement.get();
    }

    public final void setDatePaiement(LocalDate v) {
        datePaiement.set(v);
    }

    public double getMontant() {
        return montant.get();
    }

    public final void setMontant(double v) {
        montant.set(v);
    }

    public String getModePaiement() {
        return modePaiement.get();
    }

    public final void setModePaiement(String v) {
        modePaiement.set(v);
    }

    public int getIdFacture() {
        return idFacture.get();
    }

    public final void setIdFacture(int v) {
        idFacture.set(v);
    }
}

