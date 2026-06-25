package com.example.budgetmanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

/**
 * AddEditTransactionActivity — écran pour AJOUTER ou MODIFIER une transaction.
 *
 * Le même écran sert aux deux cas :
 *   - sans extra "id"  -> mode AJOUT ;
 *   - avec extra "id"  -> mode MODIFICATION (les champs sont pré-remplis).
 *
 * Règle de gestion : on interdit une dépense supérieure au solde disponible
 * (le solde ne peut donc jamais devenir négatif).
 */
public class AddEditTransactionActivity extends AppCompatActivity {

    // Clés des données passées d'un écran à l'autre
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_MONTANT = "montant";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_CATEGORIE = "categorie";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_DATE = "date";

    private EditText etMontant, etDescription;
    private Spinner spinnerCategorie;
    private RadioGroup rgType;
    private Button btnDate, btnEnregistrer;

    private DatabaseHelper bdd;

    private String dateChoisie;          // "AAAA-MM-JJ"
    private boolean modeEdition = false; // true si on modifie une transaction existante
    private int idTransaction = -1;
    private double ancienMontant = 0;
    private boolean ancienEstRevenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        bdd = new DatabaseHelper(this);

        etMontant = findViewById(R.id.etMontant);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategorie = findViewById(R.id.spinnerCategorie);
        rgType = findViewById(R.id.rgType);
        btnDate = findViewById(R.id.btnDate);
        btnEnregistrer = findViewById(R.id.btnEnregistrer);

        // Remplir le Spinner des catégories
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, DatabaseHelper.CATEGORIES);
        spinnerCategorie.setAdapter(adapterCat);

        // Date par défaut : aujourd'hui
        dateChoisie = Utils.dateAujourdhui();
        btnDate.setText(dateChoisie);
        btnDate.setOnClickListener(v -> choisirDate());

        // Mode modification ? On regarde si un id a été transmis.
        if (getIntent().hasExtra(EXTRA_ID)) {
            modeEdition = true;
            prefRemplirChamps();
            setTitle("Modifier la transaction");
        } else {
            setTitle("Ajouter une transaction");
        }

        btnEnregistrer.setOnClickListener(v -> enregistrer());
    }

    /** Pré-remplit les champs avec la transaction reçue (mode modification). */
    private void prefRemplirChamps() {
        idTransaction = getIntent().getIntExtra(EXTRA_ID, -1);
        ancienMontant = getIntent().getDoubleExtra(EXTRA_MONTANT, 0);
        String type = getIntent().getStringExtra(EXTRA_TYPE);
        String categorie = getIntent().getStringExtra(EXTRA_CATEGORIE);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String date = getIntent().getStringExtra(EXTRA_DATE);

        ancienEstRevenu = "revenu".equals(type);

        etMontant.setText(String.valueOf(ancienMontant));
        etDescription.setText(description == null ? "" : description);
        rgType.check(ancienEstRevenu ? R.id.rbRevenu : R.id.rbDepense);

        // Sélectionner la bonne catégorie dans le Spinner
        if (categorie != null) {
            for (int i = 0; i < DatabaseHelper.CATEGORIES.length; i++) {
                if (DatabaseHelper.CATEGORIES[i].equals(categorie)) {
                    spinnerCategorie.setSelection(i);
                    break;
                }
            }
        }

        if (date != null && !date.isEmpty()) {
            dateChoisie = date;
            btnDate.setText(date);
        }
    }

    /** Affiche le sélecteur de date. */
    private void choisirDate() {
        Calendar cal = Calendar.getInstance();
        // Si une date est déjà choisie, on positionne le calendrier dessus
        try {
            String[] p = dateChoisie.split("-");
            cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
        } catch (Exception ignored) {
        }

        DatePickerDialog dialog = new DatePickerDialog(this, (view, annee, mois, jour) -> {
            // mois commence à 0, on rajoute 1 ; on formate en AAAA-MM-JJ
            dateChoisie = String.format("%04d-%02d-%02d", annee, mois + 1, jour);
            btnDate.setText(dateChoisie);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /** Valide les saisies puis enregistre (ajout ou modification). */
    private void enregistrer() {
        String montantTexte = etMontant.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String categorie = spinnerCategorie.getSelectedItem().toString();

        // Validation du montant
        if (montantTexte.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un montant", Toast.LENGTH_SHORT).show();
            return;
        }
        double montant;
        try {
            montant = Double.parseDouble(montantTexte);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show();
            return;
        }
        if (montant <= 0) {
            Toast.makeText(this, "Le montant doit être positif", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = (rgType.getCheckedRadioButtonId() == R.id.rbRevenu) ? "revenu" : "depense";

        // --- Blocage du solde négatif ---
        // Solde disponible SANS compter la transaction en cours d'édition.
        double effetAncien = 0;
        if (modeEdition) {
            effetAncien = ancienEstRevenu ? ancienMontant : -ancienMontant;
        }
        double soldeDisponible = bdd.getSolde() - effetAncien;

        if (type.equals("depense") && montant > soldeDisponible) {
            Toast.makeText(this,
                    "Solde insuffisant ! Disponible : " + Utils.formaterMontant(soldeDisponible) + " FCFA",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Enregistrement
        if (modeEdition) {
            bdd.modifierTransaction(idTransaction, montant, type, categorie, description, dateChoisie);
            Toast.makeText(this, "Transaction modifiée", Toast.LENGTH_SHORT).show();
        } else {
            bdd.ajouterTransaction(montant, type, categorie, description, dateChoisie);
            Toast.makeText(this, "Transaction ajoutée", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish(); // on revient à l'écran précédent
    }
}
