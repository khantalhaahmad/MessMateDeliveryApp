package com.messmate.delivery.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.messmate.delivery.databinding.FragmentEarningsBinding;
import com.messmate.delivery.viewmodel.EarningsViewModel;
import com.messmate.delivery.viewmodel.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class EarningsFragment extends Fragment {

    private FragmentEarningsBinding binding;
    private EarningsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEarningsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(EarningsViewModel.class);

        fetchEarnings();
    }

    private void fetchEarnings() {
        viewModel.getEarnings().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {

                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);

                    if (resource.data != null && resource.data.getData() != null) {

                        double total = resource.data.getData().getTotal();
                        List<Double> weekly = resource.data.getData().getWeekly();
                        List<Double> monthly = resource.data.getData().getMonthly();

                        // 💰 Total Earnings
                        binding.tvTotalEarnings.setText("₹" + total);

                        // 📊 Graphs
                        setupGraph(binding.weeklyChart, weekly, "#4CAF50"); // green
                        setupGraph(binding.monthlyChart, monthly, "#FF9800"); // orange
                    }
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // 🔥 GRAPH SETUP
    private void setupGraph(com.github.mikephil.charting.charts.LineChart chart,
                            List<Double> data,
                            String colorHex) {

        if (data == null || data.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i).floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "");

        // 🎨 STYLE
        int color = Color.parseColor(colorHex);

        dataSet.setColor(color);
        dataSet.setLineWidth(3f);

        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(5f);

        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // gradient feel
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);

        LineData lineData = new LineData(dataSet);

        chart.setData(lineData);

        // clean UI
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);

        chart.animateX(1000);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}