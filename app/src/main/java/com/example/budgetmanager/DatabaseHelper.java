package com.example.budgetmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper — gestion de la base de données SQLite locale (Chapitre 5 du cours).
 *
 * Cette classe hérite de {@link SQLiteOpenHelper}. Elle s'occupe :
 *   - de créer la base et la table "transactions" au premier lancement (onCreate) ;
 *   - de gérer les changements de version (onUpgrade) ;
 *   - des opérations CRUD : ajouter, lire, supprimer une transaction ;
 *   - du calcul des totaux (revenus / dépenses) pour le solde.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // --- Constantes de la base de données ---
    private static final String NOM_BDD = "budget_db";
    private static final int VERSION_BDD = 1;

    // --- Table et colonnes ---
    public static final String TABLE = "transactions";
    public static final String COL_ID = "id";
    public static final String COL_MONTANT = "montant";
    public static final String COL_TYPE = "type";          // "depense" ou "revenu"
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";          // format AAAA-MM-JJ

    public DatabaseHelper(Context context) {
        super(context, NOM_BDD, null, VERSION_BDD);
    }

    /**
     * Appelée UNE SEULE FOIS, au tout premier lancement de l'application :
     * elle crée la table "transactions".
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String creationTable = "CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MONTANT + " REAL NOT NULL, "
                + COL_TYPE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_DATE + " TEXT NOT NULL);";
        db.execSQL(creationTable);
    }

    /**
     * Appelée quand on change le numéro de VERSION_BDD : on supprime
     * l'ancienne table et on la recrée (migration simple).
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // =====================================================================
    //  OPÉRATIONS CRUD
    // =====================================================================

    /**
     * Insère une nouvelle transaction dans la base.
     *
     * @return l'identifiant (id) de la ligne créée, ou -1 en cas d'échec.
     */
    public long ajouterTransaction(double montant, String type, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valeurs = new ContentValues();
        valeurs.put(COL_MONTANT, montant);
        valeurs.put(COL_TYPE, type);
        valeurs.put(COL_DESCRIPTION, description);
        valeurs.put(COL_DATE, date);
        long id = db.insert(TABLE, null, valeurs);
        db.close();
        return id;
    }

    /**
     * Récupère TOUTES les transactions, de la plus récente à la plus ancienne.
     *
     * @return un Cursor positionné sur les résultats (à parcourir par l'appelant).
     */
    public Cursor getToutesTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY " + COL_ID + " DESC", null);
    }

    /**
     * Supprime une transaction à partir de son identifiant.
     *
     * @return le nombre de lignes supprimées (1 si succès, 0 sinon).
     */
    public int supprimerTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int nb = db.delete(TABLE, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return nb;
    }

    // =====================================================================
    //  CALCUL DES TOTAUX (pour le solde)
    // =====================================================================

    /**
     * Calcule la somme des montants pour un type donné ("revenu" ou "depense").
     */
    public double getTotalParType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_MONTANT + ") FROM " + TABLE + " WHERE " + COL_TYPE + " = ?",
                new String[]{type});
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0); // 0 si aucune ligne (SUM vaut NULL -> 0)
        }
        cursor.close();
        db.close();
        return total;
    }
}
