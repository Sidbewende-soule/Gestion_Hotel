package com.hotel.model;

import java.util.Map;

/**
 * Modèle regroupant toutes les statistiques clés (KPIs) affichées sur le tableau de bord.
 * Sert de conteneur pour les données agrégées (SQL COUNT, SUM, AVG).
 */
public class StatsDashboard {

    private int reservationsActives;     // Nombre de réservations non terminées
    private int totalReservations;       // Nombre total historique
    private int chambresTotal;           // Nombre total de chambres de l'hôtel
    private int chambresOccupees;        // Nombre de chambres avec statut occupé/en nettoyage
    private double tauxOccupation;       // Pourcentage d'occupation calculé
    private double totalRevenus;         // Somme de tous les paiements perçus
    private double satisfactionMoyenne;  // Moyenne des notes des avis clients
    private int nombreAvis;              // Nombre total de retours clients
    private Map<Integer, Double> revenusParMois; // Distribution du CA par mois (pour le graphique)

    /**
     * Constructeur par défaut.
     */
    public StatsDashboard() {}

    // --- Accesseurs et Mutateurs (Getters / Setters) ---

    public int getReservationsActives()         { return reservationsActives; }
    public void setReservationsActives(int v)   { reservationsActives = v; }

    public int getTotalReservations()           { return totalReservations; }
    public void setTotalReservations(int v)     { totalReservations = v; }

    public int getChambresTotal()               { return chambresTotal; }
    public void setChambresTotal(int v)         { chambresTotal = v; }

    public int getChambresOccupees()            { return chambresOccupees; }
    public void setChambresOccupees(int v)      { chambresOccupees = v; }

    public double getTauxOccupation()           { return tauxOccupation; }
    public void setTauxOccupation(double v)     { tauxOccupation = v; }

    public double getTotalRevenus()             { return totalRevenus; }
    public void setTotalRevenus(double v)       { totalRevenus = v; }

    public double getSatisfactionMoyenne()      { return satisfactionMoyenne; }
    public void setSatisfactionMoyenne(double v){ satisfactionMoyenne = v; }

    public int getNombreAvis()                  { return nombreAvis; }
    public void setNombreAvis(int v)            { nombreAvis = v; }

    /**
     * Retourne la map des revenus mensuels pour le graphique Canvas.
     */
    public Map<Integer, Double> getRevenusParMois()         { return revenusParMois; }
    public void setRevenusParMois(Map<Integer, Double> v)   { revenusParMois = v; }
}

