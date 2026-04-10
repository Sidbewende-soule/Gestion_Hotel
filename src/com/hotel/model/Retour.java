package com.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Modèle représentant un Retour/Avis client.
 * Permet de stocker les commentaires et la note de satisfaction des clients.
 */
public class Retour {

    // Propriétés JavaFX pour le binding avec les vues (ex: Liste des avis)
    private final IntegerProperty            idRetour     = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate>  dateRetour   = new SimpleObjectProperty<>();
    private final StringProperty             commentaire  = new SimpleStringProperty();
    private final IntegerProperty            satisfaction = new SimpleIntegerProperty(); // Note sur 5
    private final IntegerProperty            idClient     = new SimpleIntegerProperty();
    
    // Champ transient pour afficher le nom du client sans refaire de requête SQL séparée
    private String nomClient;

    /**
     * Constructeur par défaut.
     */
    public Retour() {}

    /**
     * Constructeur complet pour initialiser un avis client.
     */
    public Retour(int idRetour, LocalDate dateRetour, String commentaire,
                  int satisfaction, int idClient) {
        setIdRetour(idRetour);
        setDateRetour(dateRetour);
        setCommentaire(commentaire);
        setSatisfaction(satisfaction);
        setIdClient(idClient);
    }

    // --- Accesseurs de Properties JavaFX ---
    public IntegerProperty           idRetourProperty()    { return idRetour; }
    public ObjectProperty<LocalDate> dateRetourProperty()  { return dateRetour; }
    public StringProperty            commentaireProperty() { return commentaire; }
    public IntegerProperty           satisfactionProperty(){ return satisfaction; }
    public IntegerProperty           idClientProperty()    { return idClient; }

    // --- Getters et Setters classiques ---
    public int       getIdRetour()              { return idRetour.get(); }
    public final void      setIdRetour(int v)         { idRetour.set(v); }
    public LocalDate getDateRetour()            { return dateRetour.get(); }
    public final void      setDateRetour(LocalDate v) { dateRetour.set(v); }
    public String    getCommentaire()           { return commentaire.get(); }
    public final void      setCommentaire(String v)   { commentaire.set(v); }
    public int       getSatisfaction()          { return satisfaction.get(); }
    public final void      setSatisfaction(int v)     { satisfaction.set(v); }
    public int       getIdClient()              { return idClient.get(); }
    public final void      setIdClient(int v)         { idClient.set(v); }
    public String    getNomClient()             { return nomClient; }
    public void      setNomClient(String v)     { nomClient = v; }
}