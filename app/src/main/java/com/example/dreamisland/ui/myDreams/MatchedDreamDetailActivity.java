package com.example.dreamisland.ui.myDreams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamisland.R;
import com.example.dreamisland.databinding.ActivityMatchedDreamDetailBinding;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class MatchedDreamDetailActivity extends AppCompatActivity {

    private ActivityMatchedDreamDetailBinding binding;
    private boolean readOnly = false;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchedDreamDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("匹配详情");
        }

        gson = new Gson();
        getIntentData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            // 获取梦境数据
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            String nature = intent.getStringExtra("nature");
            String createdAt = intent.getStringExtra("createdAt");
            String tagsJson = intent.getStringExtra("tags");
            String username = intent.getStringExtra("username");
            readOnly = intent.getBooleanExtra("readOnly", false);

            // 更新UI
            binding.tvDreamTitle.setText(title);
            binding.tvDreamUsername.setText(username != null ? username : "");
            binding.tvDreamNature.setText("梦境类型：" + (nature != null ? nature : ""));
            binding.tvDreamContent.setText(content);
            binding.tvDreamCreatedAt.setText(createdAt);

            // 显示带图标的标签
            displayIconTags(tagsJson);
        }
    }

    private void displayIconTags(String tagsJson) {
        binding.chipGroupTags.removeAllViews();
        if (android.text.TextUtils.isEmpty(tagsJson)) return;

        try {
            List<String> tags = gson.fromJson(tagsJson, new TypeToken<List<String>>(){}.getType());
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    Chip chip = new Chip(this);
                    chip.setText(tag);
                    chip.setChipBackgroundColorResource(R.color.tertiary_95);
                    chip.setTextColor(getResources().getColor(R.color.tertiary_70));
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    binding.chipGroupTags.addView(chip);
                }
            }
        } catch (Exception e) {
            // 如果不是JSON数组，尝试按空格拆分
            String[] tags = tagsJson.split("\\s+");
            for (String tag : tags) {
                if (tag.trim().isEmpty()) continue;
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.tertiary_95);
                chip.setTextColor(getResources().getColor(R.color.tertiary_70));
                chip.setClickable(false);
                chip.setCheckable(false);
                binding.chipGroupTags.addView(chip);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
