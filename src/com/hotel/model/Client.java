package com.hotel.model;

import javafx.beans.property.*;

/**
 * Modèle représentant un Client de l'hôtel.
 * Contient les informations personnelles et de fidélité.
 */
public class Client {

    // Propriétés JavaFX pour permettre le binding avec l'UI
    private final IntegerProperty idClient            = new SimpleIntegerProperty();
    private final StringProperty  nom                 = new SimpleStringProperty();
    private final StringProperty  prenom              = new SimpleStringProperty();
    private final StringProperty  telephone           = new SimpleStringProperty();
    private final StringProperty  email               = new SimpleStringProperty();
    private final StringProperty  adresse             = new SimpleStringProperty();
    private final StringProperty  numeroCarteFidelite = new SimpleStringProperty();

    /**
     * Constructeur par défaut (vide).
     */
    public Client() {}

    /**
     * Constructeur complet pour initialiser un client avec toutes ses données.
     */
    public Client(int idClient, String nom, String prenom,
                  String telephone, String email,
                  String adresse, String numeroCarteFidelite) {
        setIdClient(idClient);
        setNom(nom);
        setPrenom(prenom);
        setTelephone(telephone);
        setEmail(email);
        setAdresse(adresse);
        setNumeroCarteFidelite(numeroCarteFidelite);
    }

    // --- Accesseurs de Properties (pour TableView, etc.) ---
    public IntegerProperty idClientProperty()            { return idClient; }
    public StringProperty  nomProperty()                 { return nom; }
    public StringProperty  prenomProperty()              { return prenom; }
    public StringProperty  telephoneProperty()           { return telephone; }
    public StringProperty  emailProperty()               { return email; }
    public StringProperty  adresseProperty()             { return adresse; }
    public StringProperty  numeroCarteFideliteProperty() { return numeroCarteFidelite; }

    // --- Getters et Setters classiques ---
    public int    getIdClient()                    { return idClient.get(); }
    public final void   setIdClient(int v)               { idClient.set(v); }
    public String getNom()                         { return nom.get(); }
    public final void   setNom(String v)                 { nom.set(v); }
    public String getPrenom()                      { return prenom.get(); }
    public final void   setPrenom(String v)              { prenom.set(v); }
    public String getTelephone()                   { return telephone.get(); }
    public final void   setTelephone(String v)           { telephone.set(v); }
    public String getEmail()                       { return email.get(); }
    public final void   setEmail(String v)               { email.set(v); }
    public String getAdresse()                     { return adresse.get(); }
    public final void   setAdresse(String v)             { adresse.set(v); }
    public String getNumeroCarteFidelite()         { return numeroCarteFidelite.get(); }
    public final void   setNumeroCarteFidelite(String v) { numeroCarteFidelite.set(v); }

    /**
     * Retourne le nom et le prénom combinés.
     */
    public String getNomComplet() { return prenom.get() + " " + nom.get(); }

    @Override
    public String toString() { return getNomComplet(); }
}