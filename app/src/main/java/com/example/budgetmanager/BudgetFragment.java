package com.example.budgetmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * BudgetFragment — définir un budget mensuel et suivre son utilisation,
 * avec une alerte en cas de dépassement.
 */
public class BudgetFragment extends Fragment {

    private TextView tvMoisCourant, tvBudgetDetail, tvAlerte;
    private EditText etBudget;
    private ProgressBar progressBudget;
    private DatabaseHelper bdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vue, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vue, savedInstanceState);
        bdd = new DatabaseHelper(requireContext());

        tvMoisCourant = vue.findViewById(R.id.tvMoisCourant);
        tvBudgetDetail = vue.findViewById(R.id.tvBudgetDetail);
        tvAlerte = vue.findViewById(R.id.tvAlerte);
        etBudget = vue.findViewById(R.id.etBudget);
        progressBudget = vue.findViewById(R.id.progressBudget);

        tvMoisCourant.setText("Budget du mois : " + Utils.moisActuel());

        Button btn = vue.findViewById(R.id.btnDefinir);
        btn.setOnClickListener(v -> enregistrerBudget());
    }

    @Override
    public void onResume() {
        super.onResume();
        charger();
    }

    private void enregistrerBudget() {
        String texte = etBudget.getText().toString().trim();
        if (texte.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez saisir un montant", Toast.LENGTH_SHORT).show();
            return;
        }
        double montant;
        try {
            montant = Double.parseDouble(texte);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Montant invalide", Toast.LENGTH_SHORT).show();
            return;
        }
        bdd.definirBudget(Utils.moisActuel(), montant);
        Toast.makeText(requireContext(), "Budget enregistré", Toast.LENGTH_SHORT).show();
        etBudget.setText("");
        charger();
    }

    private void charger() {
        String mois = Utils.moisActuel();
        double budget = bdd.getBudget(mois);
        double depenses = bdd.getTotalParTypeEtMois("depense", mois);

        if (budget <= 0) {
            progressBudget.setProgress(0);
            tvBudgetDetail.setText("Aucun budget défini pour ce mois");
            tvAlerte.setVisibility(View.GONE);
            return;
        }

        int pourcent = (int) Math.min(100, (depenses / budget) * 100);
        progressBudget.setProgress(pourcent);
        tvBudgetDetail.setText(Utils.formaterMontant(depenses) + " / "
                + Utils.formaterMontant(budget) + " FCFA dépensés (" + pourcent + "%)");

        if (depenses > budget) {
            tvAlerte.setVisibility(View.VISIBLE);
            tvAlerte.setText("⚠️ Budget dépassé de "
                    + Utils.formaterMontant(depenses - budget) + " FCFA !");
        } else {
            double reste = budget - depenses;
            tvAlerte.setVisibility(View.VISIBLE);
            tvAlerte.setTextColor(0xFF2E7D32); // vert
            tvAlerte.setText("Il vous reste " + Utils.formaterMontant(reste) + " FCFA ce mois-ci.");
        }
    }
}
