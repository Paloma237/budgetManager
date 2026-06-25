package com.example.budgetmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * AccueilFragment — écran de résumé : solde, revenus/dépenses du mois,
 * budget, et les dernières transactions.
 */
public class AccueilFragment extends Fragment {

    private TextView tvSolde, tvRevenusMois, tvDepensesMois, tvBudgetDetail;
    private ProgressBar progressBudget;
    private RecyclerView recyclerDernieres;
    private DatabaseHelper bdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accueil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vue, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vue, savedInstanceState);
        bdd = new DatabaseHelper(requireContext());

        tvSolde = vue.findViewById(R.id.tvSolde);
        tvRevenusMois = vue.findViewById(R.id.tvRevenusMois);
        tvDepensesMois = vue.findViewById(R.id.tvDepensesMois);
        tvBudgetDetail = vue.findViewById(R.id.tvBudgetDetail);
        progressBudget = vue.findViewById(R.id.progressBudget);
        recyclerDernieres = vue.findViewById(R.id.recyclerDernieres);
        recyclerDernieres.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        chargerDonnees(); // rafraîchir à chaque retour sur l'écran
    }

    private void chargerDonnees() {
        String mois = Utils.moisActuel();

        // Solde global
        tvSolde.setText(Utils.formaterMontant(bdd.getSolde()) + " FCFA");

        // Revenus / dépenses du mois courant
        double revenusMois = bdd.getTotalParTypeEtMois("revenu", mois);
        double depensesMois = bdd.getTotalParTypeEtMois("depense", mois);
        tvRevenusMois.setText(Utils.formaterMontant(revenusMois) + " FCFA");
        tvDepensesMois.setText(Utils.formaterMontant(depensesMois) + " FCFA");

        // Budget du mois
        double budget = bdd.getBudget(mois);
        if (budget > 0) {
            int pourcent = (int) Math.min(100, (depensesMois / budget) * 100);
            progressBudget.setProgress(pourcent);
            String detail = Utils.formaterMontant(depensesMois) + " / "
                    + Utils.formaterMontant(budget) + " FCFA (" + pourcent + "%)";
            if (depensesMois > budget) {
                detail = "⚠️ Budget dépassé ! " + detail;
            }
            tvBudgetDetail.setText(detail);
        } else {
            progressBudget.setProgress(0);
            tvBudgetDetail.setText("Aucun budget défini (onglet Budget)");
        }

        // Dernières transactions (lecture seule ici)
        List<Transaction> dernieres = bdd.getDernieresTransactions(5);
        if (dernieres.isEmpty()) {
            dernieres = new ArrayList<>();
        }
        TransactionAdapter adapter = new TransactionAdapter(dernieres,
                new TransactionAdapter.OnTransactionListener() {
                    @Override public void onClic(Transaction t) { }
                    @Override public void onAppuiLong(Transaction t) { }
                });
        recyclerDernieres.setAdapter(adapter);
    }
}
