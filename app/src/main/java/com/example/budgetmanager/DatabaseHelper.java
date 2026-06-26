package com.example.budgetmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * DatabaseHelper — gestion de la base de données SQLite locale (Chapitre 5 du cours).
 *
 * Version enrichie :
 *   - table "transactions" avec une colonne "categorie" ;
 *   - table "budgets" (un budget maximum par mois) ;
 *   - requêtes pour les statistiques (par catégorie, par mois) ;
 *   - modification d'une transaction.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOM_BDD = "budget_db";
    private static final int VERSION_BDD = 2; // passé de 1 à 2 (ajout catégorie + budgets)

    // --- Table transactions ---
    public static final String TABLE = "transactions";
    public static final String COL_ID = "id";
    public static final String COL_MONTANT = "montant";
    public static final String COL_TYPE = "type";          // "depense" ou "revenu"
    public static final String COL_CATEGORIE = "categorie";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";          // format AAAA-MM-JJ

    // --- Table budgets ---
    public static final String TABLE_BUDGET = "budgets";
    public static final String COL_B_ID = "id";
    public static final String COL_B_MOIS = "mois";        // format AAAA-MM
    public static final String COL_B_MONTANT = "montant_max";

    /** Catégories proposées dans l'application. */
    public static final String[] CATEGORIES = {
            "Nourriture", "Transport", "Loyer", "Loisirs", "Santé",
            "Éducation", "Salaire", "Cadeau", "Autre"
    };

    public DatabaseHelper(Context context) {
        super(context, NOM_BDD, null, VERSION_BDD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MONTANT + " REAL NOT NULL, "
                + COL_TYPE + " TEXT NOT NULL, "
                + COL_CATEGORIE + " TEXT, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_DATE + " TEXT NOT NULL);");

        db.execSQL("CREATE TABLE " + TABLE_BUDGET + " ("
                + COL_B_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_B_MOIS + " TEXT NOT NULL UNIQUE, "
                + COL_B_MONTANT + " REAL NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration douce : on ajoute la colonne catégorie et la table budgets
        // sans détruire les transactions déjà enregistrées.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + COL_CATEGORIE + " TEXT");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BUDGET + " ("
                    + COL_B_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_B_MOIS + " TEXT NOT NULL UNIQUE, "
                    + COL_B_MONTANT + " REAL NOT NULL);");
        }
    }

    // =====================================================================
    //  TRANSACTIONS — CRUD
    // =====================================================================

    public long ajouterTransaction(double montant, String type, String categorie,
                                   String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_MONTANT, montant);
        v.put(COL_TYPE, type);
        v.put(COL_CATEGORIE, categorie);
        v.put(COL_DESCRIPTION, description);
        v.put(COL_DATE, date);
        long id = db.insert(TABLE, null, v);
        db.close();
        return id;
    }

    public int modifierTransaction(int id, double montant, String type, String categorie,
                                   String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_MONTANT, montant);
        v.put(COL_TYPE, type);
        v.put(COL_CATEGORIE, categorie);
        v.put(COL_DESCRIPTION, description);
        v.put(COL_DATE, date);
        int n = db.update(TABLE, v, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return n;
    }

    public int supprimerTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int n = db.delete(TABLE, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return n;
    }

    // =====================================================================
    //  TRANSACTIONS — LECTURE
    // =====================================================================

    /**
     * Liste les transactions, avec filtres optionnels.
     * Passer null (ou "") pour ignorer un filtre.
     *
     * @param filtreType "revenu", "depense", ou null pour tous
     * @param filtreCategorie une catégorie, ou null pour toutes
     * @param recherche texte cherché dans la description, ou null
     */
    public List<Transaction> getTransactions(String filtreType, String filtreCategorie, String recherche) {
        List<Transaction> liste = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();

        if (filtreType != null && !filtreType.isEmpty()) {
            conditions.add(COL_TYPE + " = ?");
            args.add(filtreType);
        }
        if (filtreCategorie != null && !filtreCategorie.isEmpty()) {
            conditions.add(COL_CATEGORIE + " = ?");
            args.add(filtreCategorie);
        }
        if (recherche != null && !recherche.isEmpty()) {
            conditions.add(COL_DESCRIPTION + " LIKE ?");
            args.add("%" + recherche + "%");
        }

        String sql = "SELECT * FROM " + TABLE;
        if (!conditions.isEmpty()) {
            sql += " WHERE " + String.join(" AND ", conditions);
        }
        sql += " ORDER BY " + COL_DATE + " DESC, " + COL_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, args.toArray(new String[0]));
        while (c.moveToNext()) {
            liste.add(curseurVersTransaction(c));
        }
        c.close();
        db.close();
        return liste;
    }

    /** Les N dernières transactions (pour l'accueil). */
    public List<Transaction> getDernieresTransactions(int limite) {
        List<Transaction> liste = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE
                + " ORDER BY " + COL_DATE + " DESC, " + COL_ID + " DESC LIMIT ?",
                new String[]{String.valueOf(limite)});
        while (c.moveToNext()) {
            liste.add(curseurVersTransaction(c));
        }
        c.close();
        db.close();
        return liste;
    }

    /** Construit un objet Transaction à partir de la ligne courante du Cursor. */
    private Transaction curseurVersTransaction(Cursor c) {
        Transaction t = new Transaction();
        t.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        t.setMontant(c.getDouble(c.getColumnIndexOrThrow(COL_MONTANT)));
        t.setType(c.getString(c.getColumnIndexOrThrow(COL_TYPE)));
        t.setCategorie(c.getString(c.getColumnIndexOrThrow(COL_CATEGORIE)));
        t.setDescription(c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)));
        t.setDate(c.getString(c.getColumnIndexOrThrow(COL_DATE)));
        return t;
    }

    // =====================================================================
    //  TOTAUX & SOLDE
    // =====================================================================

    /** Somme des montants d'un type donné ("revenu" ou "depense"), tous mois confondus. */
    public double getTotalParType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor c = db.rawQuery("SELECT SUM(" + COL_MONTANT + ") FROM " + TABLE
                + " WHERE " + COL_TYPE + " = ?", new String[]{type});
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        db.close();
        return total;
    }

    /** Somme des montants d'un type pour un mois donné (format AAAA-MM). */
    public double getTotalParTypeEtMois(String type, String mois) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor c = db.rawQuery("SELECT SUM(" + COL_MONTANT + ") FROM " + TABLE
                + " WHERE " + COL_TYPE + " = ? AND " + COL_DATE + " LIKE ?",
                new String[]{type, mois + "%"});
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        db.close();
        return total;
    }

    /** Solde global = total revenus - total dépenses. */
    public double getSolde() {
        return getTotalParType("revenu") - getTotalParType("depense");
    }

    // =====================================================================
    //  STATISTIQUES (pour les graphiques)
    // =====================================================================

    /** Total des DÉPENSES regroupées par catégorie (pour le camembert). */
    public LinkedHashMap<String, Double> getDepensesParCategorie() {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_CATEGORIE + ", SUM(" + COL_MONTANT + ") "
                + "FROM " + TABLE + " WHERE " + COL_TYPE + " = 'depense' "
                + "GROUP BY " + COL_CATEGORIE + " ORDER BY SUM(" + COL_MONTANT + ") DESC", null);
        while (c.moveToNext()) {
            String cat = c.getString(0);
            if (cat == null || cat.isEmpty()) cat = "Autre";
            map.put(cat, c.getDouble(1));
        }
        c.close();
        db.close();
        return map;
    }

    /** Total d'un type ("revenu"/"depense") regroupé par mois (pour le graphique en barres). */
    public LinkedHashMap<String, Double> getTotauxParMois(String type) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // substr(date,1,7) = "AAAA-MM"
        Cursor c = db.rawQuery("SELECT substr(" + COL_DATE + ",1,7) AS mois, SUM(" + COL_MONTANT + ") "
                + "FROM " + TABLE + " WHERE " + COL_TYPE + " = ? "
                + "GROUP BY mois ORDER BY mois ASC", new String[]{type});
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getDouble(1));
        }
        c.close();
        db.close();
        return map;
    }

    // =====================================================================
    //  BUDGET MENSUEL
    // =====================================================================

    /** Définit (ou met à jour) le budget maximum d'un mois (format AAAA-MM). */
    public void definirBudget(String mois, double montantMax) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_B_MOIS, mois);
        v.put(COL_B_MONTANT, montantMax);
        // CONFLICT_REPLACE : remplace si un budget existe déjà pour ce mois (mois est UNIQUE)
        db.insertWithOnConflict(TABLE_BUDGET, null, v, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /** Renvoie le budget du mois (0 si aucun budget défini). */
    public double getBudget(String mois) {
        SQLiteDatabase db = this.getReadableDatabase();
        double montant = 0;
        Cursor c = db.rawQuery("SELECT " + COL_B_MONTANT + " FROM " + TABLE_BUDGET
                + " WHERE " + COL_B_MOIS + " = ?", new String[]{mois});
        if (c.moveToFirst()) montant = c.getDouble(0);
        c.close();
        db.close();
        return montant;
    }

    // =====================================================================
    //  ⚠️ TEMPORAIRE — DONNÉES DE DÉMONSTRATION (pour la présentation)
    //  À retirer avant la version finale. Remplit la base avec des
    //  transactions réalistes sur 3 mois et plusieurs catégories.
    // =====================================================================
    public void insererDonneesDemo() {
        // Mois courant (offset 0), mois -1, mois -2
        // Revenus
        ajouterTransaction(200000, "revenu", "Salaire", "Salaire du mois", dateOffsetMois(0, 1));
        ajouterTransaction(200000, "revenu", "Salaire", "Salaire du mois", dateOffsetMois(1, 1));
        ajouterTransaction(180000, "revenu", "Salaire", "Salaire du mois", dateOffsetMois(2, 1));
        ajouterTransaction(25000, "revenu", "Cadeau", "Cadeau anniversaire", dateOffsetMois(1, 12));

        // Dépenses — mois courant
        ajouterTransaction(50000, "depense", "Loyer", "Loyer", dateOffsetMois(0, 3));
        ajouterTransaction(35000, "depense", "Nourriture", "Courses", dateOffsetMois(0, 8));
        ajouterTransaction(15000, "depense", "Transport", "Carburant", dateOffsetMois(0, 10));
        ajouterTransaction(12000, "depense", "Loisirs", "Cinéma", dateOffsetMois(0, 15));
        ajouterTransaction(8000, "depense", "Santé", "Pharmacie", dateOffsetMois(0, 18));

        // Dépenses — mois -1
        ajouterTransaction(50000, "depense", "Loyer", "Loyer", dateOffsetMois(1, 3));
        ajouterTransaction(30000, "depense", "Nourriture", "Courses", dateOffsetMois(1, 9));
        ajouterTransaction(18000, "depense", "Transport", "Bus", dateOffsetMois(1, 14));

        // Dépenses — mois -2
        ajouterTransaction(50000, "depense", "Loyer", "Loyer", dateOffsetMois(2, 3));
        ajouterTransaction(28000, "depense", "Nourriture", "Courses", dateOffsetMois(2, 11));
        ajouterTransaction(20000, "depense", "Loisirs", "Sortie", dateOffsetMois(2, 16));
        ajouterTransaction(15000, "depense", "Éducation", "Livres", dateOffsetMois(2, 20));

        // Un budget pour le mois courant (pour la barre de progression)
        definirBudget(moisCourant(), 150000);
    }

    /** Renvoie une date "AAAA-MM-JJ" à N mois en arrière, pour un jour donné. */
    private String dateOffsetMois(int offsetMois, int jour) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, -offsetMois);
        cal.set(java.util.Calendar.DAY_OF_MONTH, jour);
        return String.format("%1$tY-%1$tm-%1$td", cal);
    }

    private String moisCourant() {
        return String.format("%1$tY-%1$tm", java.util.Calendar.getInstance());
    }
}
