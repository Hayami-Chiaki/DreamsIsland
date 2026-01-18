package com.example.dreamisland.ui.square.discussion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dreamisland.data.SquareRepository;
import com.example.dreamisland.databinding.FragmentDiscussionBoardBinding;
import com.example.dreamisland.model.Post;

import java.util.ArrayList;
import java.util.List;

public class DiscussionBoardFragment extends Fragment {

    private FragmentDiscussionBoardBinding binding;
    private DiscussionPostAdapter adapter;
    private final List<Post> items = new ArrayList<>();
    private boolean isLoading = false;
    private int offset = 0;
    private static final int PAGE_SIZE = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscussionBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new DiscussionPostAdapter(items);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.emptyView.setVisibility(View.GONE);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            offset = 0;
            items.clear();
            loadMore();
        });

        binding.recyclerView.addOnScrollListener(new com.example.dreamisland.ui.square.plaza.EndlessScrollListener(() -> {
            if (!isLoading) loadMore();
        }));

        binding.fabNewPost.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NewPostActivity.class));
        });

        loadMore();
    }

    private void loadMore() {
        isLoading = true;
        List<Post> page = SquareRepository.getInstance(requireContext()).fetchPostsWithReplyCount(PAGE_SIZE, offset);
        items.addAll(page);
        adapter.notifyDataSetChanged();
        offset += page.size();
        isLoading = false;
        binding.swipeRefresh.setRefreshing(false);
        binding.emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}


