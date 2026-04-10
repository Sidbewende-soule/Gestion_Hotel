package com.hotel.model;

/**
 * Modèle représentant un Utilisateur système (personnel ayant accès au logiciel).
 */
public class Utilisateur {
    private int id;             // Identifiant unique
    private String nom;         // Nom et prénom
    private String email;       // Email (souvent utilisé comme login)
    private String password;    // Mot de passe (haché ou clair selon implémentation)
    private String role;        // Rôle (ADMIN, RECEPTIONNISTE, DIRECTION)

    /**
     * Constructeur pour créer un utilisateur.
     */
    public Utilisateur(int id, String nom, String email, String password, String role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // --- Getters et Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

