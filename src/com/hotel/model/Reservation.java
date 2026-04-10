package com.hotel.model;
import java.time.LocalDate;

/**
 * Modèle représentant une Réservation de chambre par un client.
 */
public class Reservation {
    private int id;                // Identifiant unique de la réservation
    private LocalDate dateDebut;   // Date de début du séjour (Check-In)
    private LocalDate dateFin;     // Date de fin du séjour (Check-Out)
    private String statut;         // CONFIRMEE, EN_ATTENTE, ANNULEE, TERMINEE
    private int idClient;          // Référence au client (FK)
    private int idChambre;         // Référence à la chambre (FK)
    private String notes;          // Notes additionnelles ou demandes manuelles
    
    // Champs pour l'affichage UI (issus de jointures SQL, non stockés dans la table reservation)
    private String nomClient;
    private String numeroChambre;

    /**
     * Constructeur simple (sans notes).
     */
    public Reservation(int id, LocalDate dateDebut, LocalDate dateFin, String statut, int idClient, int idChambre) {
        this(id, dateDebut, dateFin, statut, idClient, idChambre, "");
    }

    /**
     * Constructeur complet (avec toutes les données).
     */
    public Reservation(int id, LocalDate dateDebut, LocalDate dateFin, String statut, int idClient, int idChambre, String notes) {
        this.id = id;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.idClient = idClient;
        this.idChambre = idChambre;
        this.notes = notes;
    }

    // --- Accesseurs (Getters) ---
    public int getId() { return id; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public String getStatut() { return statut; }
    public int getIdClient() { return idClient; }
    public int getIdChambre() { return idChambre; }
    public String getNotes() { return notes; }
    
    // Getters pour les informations textuelles affichées dans les listes
    public String getNomClient() { return nomClient; }
    public String getNumeroChambre() { return numeroChambre; }
    
    // Méthode simplifiée pour récupérer l'ID client dans les tableaux
    public int getIdClientForTable() { return idClient; }

    // --- Mutateurs (Setters) ---
    public void setId(int id) { this.id = id; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setIdClient(int idClient) { this.idClient = idClient; }
    public void setIdChambre(int idChambre) { this.idChambre = idChambre; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Setters pour peupler les données issues de jointures SQL
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public void setNumeroChambre(String numeroChambre) { this.numeroChambre = numeroChambre; }
}