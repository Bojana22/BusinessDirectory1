package com.example.businessdirectory1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CompanyFragment extends Fragment {

    private final List<Company> staticCompanies = new ArrayList<>();
    private CompanyAdapter adapter;
    private String categoryName;

    public CompanyFragment() {}

    public static CompanyFragment newInstance(String category, List<Company> companies) {
        CompanyFragment fragment = new CompanyFragment();
        Bundle args = new Bundle();
        args.putString("CATEGORY_NAME", category);
        args.putSerializable("COMPANIES_LIST", new ArrayList<>(companies));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            categoryName = getArguments().getString("CATEGORY_NAME");

            List<Company> incomingCompanies =
                    (List<Company>) getArguments().getSerializable("COMPANIES_LIST");

            if (incomingCompanies != null) {
                staticCompanies.clear();
                staticCompanies.addAll(incomingCompanies);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_company, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Почнуваме со празна листа, refreshData ќе ја наполни веднаш
        adapter = new CompanyAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        refreshData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    // Метод за освежување на сите податоци (без филтер)
    public void refreshData() {
        if (categoryName == null || getContext() == null) return;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<Company> dbCompanies = db.companyDao().getCompaniesByCategory(categoryName);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    List<Company> combined = new ArrayList<>(staticCompanies);
                    if (dbCompanies != null) combined.addAll(dbCompanies);

                    if (adapter != null) {
                        adapter.updateList(combined);
                    }
                });
            }
        }).start();
    }

    // ГЛАВНИОТ МЕТОД ЗА ПРЕБАРУВАЊЕ
    public void filter(String query) {
        // Ако нема контекст или категорија, прекини
        if (getContext() == null || categoryName == null) return;

        // Ако пребарувањето е празно, врати ги сите податоци
        if (query == null || query.isEmpty()) {
            refreshData();
            return;
        }

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            // Пребарување во локалната база на уредот
            List<Company> dbFiltered =
                    db.companyDao().searchCompanies("%" + query + "%", categoryName);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    List<Company> searchResult = new ArrayList<>();

                    // 1. Филтрирање на статичните фирми (оние 600 реда што ги имаш во MainActivity)
                    for (Company c : staticCompanies) {
                        if (c.getName().toLowerCase().contains(query.toLowerCase())) {
                            searchResult.add(c);
                        }
                    }

                    // 2. Додавање на филтрираните фирми од базата
                    if (dbFiltered != null) {
                        searchResult.addAll(dbFiltered);
                    }

                    // 3. Ажурирање на рециклерот со новата листа
                    if (adapter != null) {
                        adapter.updateList(searchResult);
                    }
                });
            }
        }).start();
    }
}