package com.example.dreamisland.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dreamisland.R;
import com.example.dreamisland.databinding.FragmentProfileBinding;
import com.example.dreamisland.model.User;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private SharedPreferences alarmPrefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModelFactory factory = new ProfileViewModelFactory(requireActivity().getApplication());
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        alarmPrefs = requireContext().getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化用户信息
        setupUserInfo();

        // 初始化功能卡片
        setupFeatureCards();

        // 加载用户数据
        profileViewModel.loadUserInfo();

        // 监听用户数据变化
        profileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserUI(user);
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 更新闹钟状态显示
        updateAlarmStatus();
    }

    private void setupUserInfo() {
        // 头像点击事件
        binding.avatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
            startActivityForResult(intent, 100);
        });

        // 编辑资料按钮点击事件
        binding.editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
            startActivityForResult(intent, 100);
        });
    }

    private void setupFeatureCards() {
        // 闹钟设置卡片
        binding.alarmCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AlarmActivity.class);
            startActivity(intent);
        });

        // 应用设置卡片
        binding.settingsCard.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void updateUserUI(User user) {
        // 更新用户名
        binding.usernameTextView.setText(user.getUsername());
        // 更新头像
        if (user.getAvatar() != null) {
            binding.avatarImageView.setImageBitmap(user.getAvatar());
        }
    }

    private void updateAlarmStatus() {
        boolean wakeEnabled = alarmPrefs.getBoolean("wake_enabled", false);
        boolean sleepEnabled = alarmPrefs.getBoolean("sleep_enabled", false);
        StringBuilder statusBuilder = new StringBuilder();

        if (wakeEnabled) {
            int hour = alarmPrefs.getInt("wake_hour", 7);
            int minute = alarmPrefs.getInt("wake_minute", 0);
            String label = getString(R.string.alarm_wake);
            statusBuilder.append(label).append(" ").append(String.format("%02d:%02d", hour, minute));
        }

        if (sleepEnabled) {
            if (statusBuilder.length() > 0) {
                statusBuilder.append(" | ");
            }
            int hour = alarmPrefs.getInt("sleep_hour", 22);
            int minute = alarmPrefs.getInt("sleep_minute", 0);
            String label = getString(R.string.alarm_sleep);
            statusBuilder.append(label).append(" ").append(String.format("%02d:%02d", hour, minute));
        }

        if (statusBuilder.length() > 0) {
            binding.alarmStatusTextView.setText(statusBuilder.toString());
        } else {
            binding.alarmStatusTextView.setText(getString(R.string.alarm_not_set));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            // 用户信息已更新，重新加载数据
            profileViewModel.loadUserInfo();
            Toast.makeText(getContext(), "个人信息已更新", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
