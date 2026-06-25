package com.example.budgetmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * TransactionAdapter — fait le lien entre une liste de {@link Transaction}
 * et la RecyclerView qui les affiche.
 *
 * Gère deux interactions :
 *   - clic simple  -> modifier la transaction ;
 *   - appui long   -> supprimer la transaction.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {

    /** Interface pour prévenir l'écran quand on clique sur une transaction. */
    public interface OnTransactionListener {
        void onClic(Transaction t);
        void onAppuiLong(Transaction t);
    }

    private final List<Transaction> transactions;
    private final OnTransactionListener listener;

    public TransactionAdapter(List<Transaction> transactions, OnTransactionListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_card, parent, false);
        return new VH(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Transaction t = transactions.get(position);

        String categorie = (t.getCategorie() == null || t.getCategorie().isEmpty())
                ? "Autre" : t.getCategorie();
        String description = (t.getDescription() == null || t.getDescription().isEmpty())
                ? "(sans description)" : t.getDescription();

        h.tvCategorie.setText(categorie);
        h.tvDescription.setText(description);
        h.tvDate.setText(t.getDate());

        // Montant : signe + et couleur verte pour un revenu, - et rouge pour une dépense
        String signe = t.estRevenu() ? "+" : "-";
        h.tvMontant.setText(signe + Utils.formaterMontant(t.getMontant()) + " FCFA");
        int couleur = t.estRevenu() ? R.color.vert_revenu : R.color.rouge_depense;
        h.tvMontant.setTextColor(ContextCompat.getColor(h.itemView.getContext(), couleur));

        h.itemView.setOnClickListener(v -> listener.onClic(t));
        h.itemView.setOnLongClickListener(v -> {
            listener.onAppuiLong(t);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    /** ViewHolder = mémorise les vues d'une ligne pour éviter de les rechercher à chaque fois. */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvCategorie, tvDescription, tvDate, tvMontant;

        VH(@NonNull View v) {
            super(v);
            tvCategorie = v.findViewById(R.id.tvCategorie);
            tvDescription = v.findViewById(R.id.tvDescription);
            tvDate = v.findViewById(R.id.tvDate);
            tvMontant = v.findViewById(R.id.tvMontant);
        }
    }
}
