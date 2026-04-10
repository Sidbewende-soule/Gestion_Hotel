package com.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Modèle léger représentant un résumé de réservation pour l'affichage spécialisé.
 * Utilisé principalement dans le tableau des "Réservations Récentes" du Dashboard.
 */
public class ReservationResume {

    // Propriétés JavaFX pour la mise à jour automatique des TableView
    private final IntegerProperty            idReservation = new SimpleIntegerProperty();
    private final StringProperty             nomClient     = new SimpleStringProperty();
    private final StringProperty             chambre       = new SimpleStringProperty();
    private final ObjectProperty<LocalDate>  dateDebut     = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate>  dateFin       = new SimpleObjectProperty<>();
    private final StringProperty             statut        = new SimpleStringProperty();

    /**
     * Constructeur vide.
     */
    public ReservationResume() {}

    /**
     * Constructeur d'initialisation rapide pour les données du tableau.
     */
    public ReservationResume(int idReservation, String nomClient, String chambre,
                             LocalDate dateDebut, LocalDate dateFin, String statut) {
        setIdReservation(idReservation);
        setNomClient(nomClient);
        setChambre(chambre);
        setDateDebut(dateDebut);
        setDateFin(dateFin);
        setStatut(statut);
    }

    // --- Accesseurs de Properties (Binding JavaFX) ---
    public IntegerProperty           idReservationProperty() { return idReservation; }
    public StringProperty            nomClientProperty()     { return nomClient; }
    public StringProperty            chambreProperty()       { return chambre; }
    public ObjectProperty<LocalDate> dateDebutProperty()     { return dateDebut; }
    public ObjectProperty<LocalDate> dateFinProperty()       { return dateFin; }
    public StringProperty            statutProperty()        { return statut; }

    // --- Getters et Setters standard ---
    public int       getIdReservation()              { return idReservation.get(); }
    public final void      setIdReservation(int v)         { idReservation.set(v); }

    public String    getNomClient()                  { return nomClient.get(); }
    public final void      setNomClient(String v)          { nomClient.set(v); }

    public String    getChambre()                    { return chambre.get(); }
    public final void      setChambre(String v)            { chambre.set(v); }

    public LocalDate getDateDebut()                  { return dateDebut.get(); }
    public final void      setDateDebut(LocalDate v)       { dateDebut.set(v); }

    public LocalDate getDateFin()                    { return dateFin.get(); }
    public final void      setDateFin(LocalDate v)         { dateFin.set(v); }

    public String    getStatut()                     { return statut.get(); }
    public final void      setStatut(String v)             { statut.set(v); }
}

