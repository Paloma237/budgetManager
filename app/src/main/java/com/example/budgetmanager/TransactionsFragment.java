package com.example.budgetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * TransactionsFragment — liste complète des transactions.
 *
 * Fonctions : recherche, filtre par type, ajout (bouton +),
 * modification (clic) et suppression (appui long avec confirmation).
 */
public class TransactionsFragment extends Fragment
        implements TransactionAdapter.OnTransactionListener {

    private RecyclerView recycler;
    private EditText etRecherche;
    private Spinner spinnerFiltre;
    private TextView tvVide;

    private DatabaseHelper bdd;
    private final List<Transaction> liste = new ArrayList<>();
    private TransactionAdapter adapter;

    // Permet de rafraîchir la liste au retour de l'écran d'ajout/modification
    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> charger());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vue, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vue, savedInstanceState);
        bdd = new DatabaseHelper(requireContext());

        recycler = vue.findViewById(R.id.recyclerTransactions);
        etRecherche = vue.findViewById(R.id.etRecherche);
        spinnerFiltre = vue.findViewById(R.id.spinnerFiltre);
        tvVide = vue.findViewById(R.id.tvVide);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(liste, this);
        recycler.setAdapter(adapter);

        // Filtre : Tous / Revenus / Dépenses
        ArrayAdapter<String> adapterFiltre = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Tous", "Revenus", "Dépenses"});
        spinnerFiltre.setAdapter(adapterFiltre);
        spinnerFiltre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { charger(); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });

        // Recherche : recharge à chaque caractère
        etRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { charger(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        // Bouton + : ouvrir l'écran d'ajout
        FloatingActionButton fab = vue.findViewById(R.id.fabAjouter);
        fab.setOnClickListener(v ->
                launcher.launch(new Intent(requireContext(), AddEditTransactionActivity.class)));

        charger();
    }

    @Override
    public void onResume() {
        super.onResume();
        charger();
    }

    /** Recharge la liste depuis la base selon le filtre et la recherche. */
    private void charger() {
        String type = null;
        int pos = spinnerFiltre.getSelectedItemPosition();
        if (pos == 1) type = "revenu";
        else if (pos == 2) type = "depense";

        String recherche = etRecherche.getText().toString().trim();

        liste.clear();
        liste.addAll(bdd.getTransactions(type, null, recherche));
        adapter.notifyDataSetChanged();

        tvVide.setVisibility(liste.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // --- Clic sur une transaction = modification ---
    @Override
    public void onClic(Transaction t) {
        Intent i = new Intent(requireContext(), AddEditTransactionActivity.class);
        i.putExtra(AddEditTransactionActivity.EXTRA_ID, t.getId());
        i.putExtra(AddEditTransactionActivity.EXTRA_MONTANT, t.getMontant());
        i.putExtra(AddEditTransactionActivity.EXTRA_TYPE, t.getType());
        i.putExtra(AddEditTransactionActivity.EXTRA_CATEGORIE, t.getCategorie());
        i.putExtra(AddEditTransactionActivity.EXTRA_DESCRIPTION, t.getDescription());
        i.putExtra(AddEditTransactionActivity.EXTRA_DATE, t.getDate());
        launcher.launch(i);
    }

    // --- Appui long = suppression (avec confirmation) ---
    @Override
    public void onAppuiLong(Transaction t) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Supprimer")
                .setMessage("Voulez-vous supprimer cette transaction ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    bdd.supprimerTransaction(t.getId());
                    Toast.makeText(requireContext(), "Transaction supprimée", Toast.LENGTH_SHORT).show();
                    charger();
                })
                .setNegativeButton("Non", null)
                .show();
    }
}
