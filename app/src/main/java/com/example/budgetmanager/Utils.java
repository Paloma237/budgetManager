package com.example.budgetmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utils — petites fonctions utilitaires partagées par toute l'application.
 */
public class Utils {

    /** Formate un montant : "150 000" au lieu de "150000.0". */
    public static String formaterMontant(double montant) {
        if (montant == Math.floor(montant)) {
            return String.format(Locale.getDefault(), "%,d", (long) montant);
        }
        return String.format(Locale.getDefault(), "%,.2f", montant);
    }

    /** Date du jour au format AAAA-MM-JJ (ex : 2026-06-25). */
    public static String dateAujourdhui() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    /** Mois courant au format AAAA-MM (ex : 2026-06). */
    public static String moisActuel() {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
    }
}
