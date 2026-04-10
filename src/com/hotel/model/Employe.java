package com.hotel.model;

/**
 * Modèle représentant un Employé de l'hôtel.
 * Utilisé principalement pour l'assignation des tâches de nettoyage et maintenance.
 */
public class Employe {

    private int id;      // Identifiant unique
    private String nom;  // Nom de l'employé
    private String role; // Rôle (NETTOYEUR, MAINTENANCIER, etc.)

    /**
     * Constructeur pour créer un objet Employé.
     */
    public Employe(int id, String nom, String role) {
        this.id = id;
        this.nom = nom;
        this.role = role;
    }

    /**
     * Retourne l'identifiant de l'employé.
     * @return id_employe
     */
    public int getIdEmploye() { 
        return id; 
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getRole() { return role; }

    /**
     * Représentation sous forme de texte pour les ComboBox.
     */
    @Override
    public String toString() { 
        return nom + " (" + role + ")"; 
    }
}