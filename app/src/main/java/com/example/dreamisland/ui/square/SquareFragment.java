package com.example.dreamisland.ui.square;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.dreamisland.databinding.FragmentSquareBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.dreamisland.ui.square.plaza.DreamPlazaFragment;
import com.example.dreamisland.ui.square.discussion.DiscussionBoardFragment;

public class SquareFragment extends Fragment {

    private FragmentSquareBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSquareBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 移除了返回按钮功能
        FragmentActivity activity = requireActivity();
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) return new DreamPlazaFragment();
                return new DiscussionBoardFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setUserInputEnabled(true);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "梦境广场" : "自由讨论");
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}