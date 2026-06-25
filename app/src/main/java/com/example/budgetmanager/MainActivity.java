package com.example.budgetmanager;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * MainActivity — activité unique et cœur de l'application (Chapitre 3 du cours).
 *
 * Elle gère :
 *   - l'initialisation des vues (onCreate) ;
 *   - l'ajout d'une transaction (bouton) ;
 *   - le calcul et l'affichage du solde (calculerSolde) ;
 *   - l'affichage de l'historique dans la ListView (actualiserListe) ;
 *   - la suppression d'une transaction par appui long.
 */
public class MainActivity extends AppCompatActivity {

    // --- Vues de l'interface ---
    private TextView tvSolde, tvTotalRevenus, tvTotalDepenses;
    private EditText etMontant, etDescription;
    private RadioGroup rgType;
    private Button btnAjouter;
    private ListView lvHistorique;

    // --- Base de données ---
    private DatabaseHelper bdd;

    // --- Données affichées dans la liste ---
    // lignesAffichage : le texte montré pour chaque transaction
    // idsTransactions : l'id (en base) correspondant, à la même position
    private ArrayList<String> lignesAffichage;
    private ArrayList<Integer> idsTransactions;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1) Récupérer les vues
        tvSolde = findViewById(R.id.tvSolde);
        tvTotalRevenus = findViewById(R.id.tvTotalRevenus);
        tvTotalDepenses = findViewById(R.id.tvTotalDepenses);
        etMontant = findViewById(R.id.etMontant);
        etDescription = findViewById(R.id.etDescription);
        rgType = findViewById(R.id.rgType);
        btnAjouter = findViewById(R.id.btnAjouter);
        lvHistorique = findViewById(R.id.lvHistorique);

        // 2) Initialiser la base de données
        bdd = new DatabaseHelper(this);

        // 3) Préparer l'adaptateur de la liste
        lignesAffichage = new ArrayList<>();
        idsTransactions = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.item_transaction, lignesAffichage);
        lvHistorique.setAdapter(adapter);

        // 4) Listener du bouton "Ajouter"
        btnAjouter.setOnClickListener(v -> ajouterTransaction());

        // 5) Listener d'appui long sur la liste (= suppression)
        lvHistorique.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmerSuppression(position);
            return true; // true = l'événement est consommé
        });

        // 6) Afficher l'historique et le solde au démarrage
        actualiserListe();
    }

    /**
     * Lit les champs de saisie, valide le montant, puis enregistre la transaction.
     */
    private void ajouterTransaction() {
        String montantTexte = etMontant.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation du montant
        if (montantTexte.isEmpty()) {
            Toast.makeText(this, R.string.erreur_montant, Toast.LENGTH_SHORT).show();
            return;
        }
        double montant;
        try {
            montant = Double.parseDouble(montantTexte);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.erreur_montant, Toast.LENGTH_SHORT).show();
            return;
        }
        if (montant <= 0) {
            Toast.makeText(this, R.string.erreur_montant, Toast.LENGTH_SHORT).show();
            return;
        }

        // Type choisi : revenu si rbRevenu coché, sinon dépense
        String type = (rgType.getCheckedRadioButtonId() == R.id.rbRevenu) ? "revenu" : "depense";

        // Date du jour au format AAAA-MM-JJ
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Enregistrement en base
        bdd.ajouterTransaction(montant, type, description, date);
        Toast.makeText(this, R.string.msg_ajout, Toast.LENGTH_SHORT).show();

        // Réinitialiser les champs de saisie
        etMontant.setText("");
        etDescription.setText("");

        // Rafraîchir l'affichage
        actualiserListe();
    }

    /**
     * Recharge la liste des transactions depuis la base, met à jour la ListView,
     * puis recalcule le solde.
     */
    private void actualiserListe() {
        lignesAffichage.clear();
        idsTransactions.clear();

        Cursor cursor = bdd.getToutesTransactions();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
            double montant = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MONTANT));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));

            // Signe + pour un revenu, - pour une dépense
            String signe = type.equals("revenu") ? "+" : "-";
            if (description == null || description.isEmpty()) {
                description = "(sans description)";
            }

            String ligne = signe + formaterMontant(montant) + " FCFA   [" + type + "]\n" + description + "  •  " + date;

            lignesAffichage.add(ligne);
            idsTransactions.add(id);
        }
        cursor.close();

        adapter.notifyDataSetChanged(); // dit à la liste de se redessiner
        calculerSolde();
    }

    /**
     * Calcule le solde (revenus - dépenses) et met à jour les 3 TextView.
     * Solde = Total Revenus - Total Dépenses (règle de gestion du cahier des charges).
     */
    private void calculerSolde() {
        double totalRevenus = bdd.getTotalParType("revenu");
        double totalDepenses = bdd.getTotalParType("depense");
        double solde = totalRevenus - totalDepenses;

        tvSolde.setText(formaterMontant(solde) + " FCFA");
        tvTotalRevenus.setText("Revenus : " + formaterMontant(totalRevenus));
        tvTotalDepenses.setText("Dépenses : " + formaterMontant(totalDepenses));
    }

    /**
     * Met en forme un montant : "150 000" au lieu de "150000.0".
     * (sans décimale si le nombre est entier)
     */
    private String formaterMontant(double montant) {
        if (montant == Math.floor(montant)) {
            return String.format(Locale.getDefault(), "%,d", (long) montant);
        }
        return String.format(Locale.getDefault(), "%,.2f", montant);
    }

    /**
     * Affiche une boîte de dialogue de confirmation avant de supprimer
     * la transaction située à la position donnée dans la liste.
     */
    private void confirmerSuppression(int position) {
        int id = idsTransactions.get(position);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_titre_suppression)
                .setMessage(R.string.dialog_message_suppression)
                .setPositiveButton(R.string.oui, (dialog, which) -> {
                    bdd.supprimerTransaction(id);
                    Toast.makeText(this, R.string.msg_suppression, Toast.LENGTH_SHORT).show();
                    actualiserListe();
                })
                .setNegativeButton(R.string.non, null)
                .show();
    }
}
