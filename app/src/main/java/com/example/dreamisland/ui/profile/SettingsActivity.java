package com.example.dreamisland.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.dreamisland.R;
import com.example.dreamisland.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);

        loadSettings();
        setupViews();
    }

    private void setupViews() {
        // 通知设置开关
        binding.enableNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "通知已启用" : "通知已禁用", Toast.LENGTH_SHORT).show();
        });

        // 深色模式开关
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            Toast.makeText(this, isChecked ? "深色模式已启用" : "深色模式已禁用", Toast.LENGTH_SHORT).show();
        });

        // 清除缓存
        binding.clearCacheItem.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("清除缓存")
                    .setMessage("确定要清除缓存吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 这里可以添加清除缓存的逻辑
                        Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 关于
        binding.aboutItem.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.about)
                    .setMessage("DreamIsland v1.0\n\n一个记录梦境的应用")
                    .setPositiveButton("确定", null)
                    .show();
        });

        // 隐私政策
        binding.privacyPolicyItem.setOnClickListener(v -> {
            Toast.makeText(this, "隐私政策功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 服务条款
        binding.termsOfServiceItem.setOnClickListener(v -> {
            Toast.makeText(this, "服务条款功能待实现", Toast.LENGTH_SHORT).show();
        });

        // 退出登录
        binding.logoutItem.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("退出登录")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 清除登录状态并返回登录页
                        SharedPreferences sp = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
                        sp.edit().remove("logged_in_user_id").apply();
                        android.content.Intent intent = new android.content.Intent(this, com.example.dreamisland.ui.auth.LoginActivity.class);
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Toast.makeText(this, R.string.logout, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void loadSettings() {
        // 加载通知设置
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        binding.enableNotificationsSwitch.setChecked(notificationsEnabled);

        // 加载深色模式设置
        boolean darkMode = sharedPreferences.getBoolean("dark_mode", false);
        binding.darkModeSwitch.setChecked(darkMode);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

