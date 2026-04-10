package com.hotel.model;

import javafx.beans.property.*;

/**
 * Modèle représentant une Chambre de l'hôtel.
 * Utilise des Properties JavaFX pour permettre le binding avec l'interface graphique (TableView).
 */
public class Chambre {
    private final IntegerProperty idChambre = new SimpleIntegerProperty(); // Identifiant unique en base
    private final StringProperty numero = new SimpleStringProperty();       // Numéro de la chambre (ex: 101)
    private final StringProperty etat = new SimpleStringProperty();         // État (DISPONIBLE, EN_NETTOYAGE, etc.)
    private final DoubleProperty prixParNuit = new SimpleDoubleProperty(); // Tarif par nuitée
    private final IntegerProperty idType = new SimpleIntegerProperty();     // ID du type de chambre (Simple, Suite...)
    private final IntegerProperty idEmploye = new SimpleIntegerProperty(); // ID de l'employé assigné (si nettoyage/maintenance)
    private final StringProperty nomPersonnel = new SimpleStringProperty("Aucun"); // Nom de l'employé pour affichage

    /**
     * Constructeur pour initialiser une chambre.
     */
    public Chambre(int idChambre, String numero, String etat, double prixParNuit, int idType) {
        this.idChambre.set(idChambre);
        this.numero.set(numero);
        this.etat.set(etat);
        this.prixParNuit.set(prixParNuit);
        this.idType.set(idType);
    }

    // --- Propriétés JavaFX (utilisées pour le binding dans les TableView) ---
    public IntegerProperty idChambreProperty() { return idChambre; }
    public StringProperty numeroProperty() { return numero; }
    public StringProperty etatProperty() { return etat; }
    public DoubleProperty prixParNuitProperty() { return prixParNuit; }
    public IntegerProperty idTypeProperty() { return idType; }
    public IntegerProperty idEmployeProperty() { return idEmploye; }
    public StringProperty nomPersonnelProperty() { return nomPersonnel; }
    
    // --- Getters classiques pour accéder aux valeurs brutes ---
    public int getIdChambre() { return idChambre.get(); }
    public String getNumero() { return numero.get(); }
    public String getEtat() { return etat.get(); }
    public double getPrixParNuit() { return prixParNuit.get(); }
    public int getIdType() { return idType.get(); }
    public String getNomPersonnel() { return nomPersonnel.get(); }
}