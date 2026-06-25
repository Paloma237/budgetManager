package com.example.budgetmanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity — écran hôte de l'application.
 *
 * Elle ne contient pas de logique métier : elle se contente d'afficher
 * le bon fragment (écran) selon l'onglet choisi dans la barre de navigation.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_transactions) {
                fragment = new TransactionsFragment();
            } else if (id == R.id.nav_stats) {
                fragment = new StatistiquesFragment();
            } else if (id == R.id.nav_budget) {
                fragment = new BudgetFragment();
            } else {
                fragment = new AccueilFragment();
            }
            afficherFragment(fragment);
            return true;
        });

        // Écran affiché au démarrage : l'Accueil
        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_accueil);
        }
    }

    /** Remplace le contenu du conteneur par le fragment donné. */
    private void afficherFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
