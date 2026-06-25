package com.example.budgetmanager;

/**
 * Transaction — modèle de données (POJO).
 *
 * Représente une ligne de la table "transactions" : un revenu ou une dépense.
 * Cette classe sert à transporter les données entre la base et l'interface.
 */
public class Transaction {

    private int id;
    private double montant;
    private String type;         // "revenu" ou "depense"
    private String categorie;
    private String description;
    private String date;         // format AAAA-MM-JJ

    public Transaction() {
    }

    public Transaction(int id, double montant, String type, String categorie,
                       String description, String date) {
        this.id = id;
        this.montant = montant;
        this.type = type;
        this.categorie = categorie;
        this.description = description;
        this.date = date;
    }

    // --- Getters / Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    /** Vrai si c'est un revenu. */
    public boolean estRevenu() {
        return "revenu".equals(type);
    }
}
