package com.example.dreamisland.ui.square.plaza;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dreamisland.data.SquareRepository;
import com.example.dreamisland.databinding.FragmentDreamPlazaBinding;
import com.example.dreamisland.model.Dream;

import java.util.ArrayList;
import java.util.List;

public class DreamPlazaFragment extends Fragment {

    private FragmentDreamPlazaBinding binding;
    private DreamPlazaAdapter adapter;
    private final List<Dream> items = new ArrayList<>();
    private boolean isLoading = false;
    private int offset = 0;
    private static final int PAGE_SIZE = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDreamPlazaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new DreamPlazaAdapter(items);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.emptyView.setVisibility(View.GONE);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            offset = 0;
            items.clear();
            loadMore();
        });

        binding.recyclerView.addOnScrollListener(new EndlessScrollListener(() -> {
            if (!isLoading) loadMore();
        }));

        loadMore();
    }

    private void loadMore() {
        isLoading = true;
        List<Dream> page = SquareRepository.getInstance(requireContext()).fetchPublicDreams(PAGE_SIZE, offset);
        items.addAll(page);
        adapter.notifyDataSetChanged();
        offset += page.size();
        isLoading = false;
        binding.swipeRefresh.setRefreshing(false);
        binding.emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}


