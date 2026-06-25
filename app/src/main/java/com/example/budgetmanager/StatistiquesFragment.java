package com.example.budgetmanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * StatistiquesFragment — affiche deux graphiques :
 *   - un camembert (PieChart) des dépenses par catégorie ;
 *   - un graphique en barres (BarChart) des revenus vs dépenses par mois.
 */
public class StatistiquesFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private DatabaseHelper bdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistiques, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View vue, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(vue, savedInstanceState);
        bdd = new DatabaseHelper(requireContext());
        pieChart = vue.findViewById(R.id.pieChart);
        barChart = vue.findViewById(R.id.barChart);
    }

    @Override
    public void onResume() {
        super.onResume();
        afficherCamembert();
        afficherBarres();
    }

    /** Camembert : dépenses regroupées par catégorie. */
    private void afficherCamembert() {
        LinkedHashMap<String, Double> map = bdd.getDepensesParCategorie();

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);

        if (map.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("Aucune dépense à afficher");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> e : map.entrySet()) {
            entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.invalidate();
    }

    /** Barres groupées : revenus (vert) vs dépenses (rouge) par mois. */
    private void afficherBarres() {
        LinkedHashMap<String, Double> revenus = bdd.getTotauxParMois("revenu");
        LinkedHashMap<String, Double> depenses = bdd.getTotauxParMois("depense");

        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);

        // Union de tous les mois, triés
        TreeSet<String> moisSet = new TreeSet<>();
        moisSet.addAll(revenus.keySet());
        moisSet.addAll(depenses.keySet());

        if (moisSet.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("Aucune donnée à afficher");
            barChart.invalidate();
            return;
        }

        List<String> mois = new ArrayList<>(moisSet);
        List<BarEntry> entreesRevenus = new ArrayList<>();
        List<BarEntry> entreesDepenses = new ArrayList<>();
        for (int i = 0; i < mois.size(); i++) {
            String m = mois.get(i);
            double r = revenus.containsKey(m) ? revenus.get(m) : 0;
            double d = depenses.containsKey(m) ? depenses.get(m) : 0;
            entreesRevenus.add(new BarEntry(i, (float) r));
            entreesDepenses.add(new BarEntry(i, (float) d));
        }

        BarDataSet setRevenus = new BarDataSet(entreesRevenus, "Revenus");
        setRevenus.setColor(Color.parseColor("#2E7D32"));
        BarDataSet setDepenses = new BarDataSet(entreesDepenses, "Dépenses");
        setDepenses.setColor(Color.parseColor("#C62828"));

        BarData barData = new BarData(setRevenus, setDepenses);
        float groupSpace = 0.3f, barSpace = 0.05f, barWidth = 0.3f;
        barData.setBarWidth(barWidth);
        barChart.setData(barData);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setValueFormatter(new IndexAxisValueFormatter(mois));
        x.setGranularity(1f);
        x.setCenterAxisLabels(true);
        x.setAxisMinimum(0f);
        x.setAxisMaximum(mois.size());
        x.setDrawGridLines(false);

        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.invalidate();
    }
}
