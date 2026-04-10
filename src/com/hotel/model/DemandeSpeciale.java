package com.hotel.model;

/**
 * Modèle représentant une Demande Spéciale (service optionnel) liée à une réservation.
 * Exemples : Petit-déjeuner inclus, Lit bébé, Vue piscine, etc.
 */
public class DemandeSpeciale {
    private int id;             // Identifiant unique
    private String nomDemande;  // Libellé court du service
    private String description; // Détails sur le service

    /**
     * Constructeur pour initialiser une demande spéciale.
     */
    public DemandeSpeciale(int id, String nomDemande, String description) {
        this.id = id;
        this.nomDemande = nomDemande;
        this.description = description;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getNomDemande() { return nomDemande; }
    public String getDescription() { return description; }
}