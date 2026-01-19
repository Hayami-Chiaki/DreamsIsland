package com.example.dreamisland.ui.square.plaza.detail;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamisland.R;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.databinding.ActivityDreamDetailBinding;
import com.google.android.material.chip.Chip;

public class DreamDetailActivity extends AppCompatActivity {
    private ActivityDreamDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDreamDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("详情");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int dreamId = getIntent().getIntExtra("dream_id", -1);
        if (dreamId != -1) {
            DreamDatabaseHelper helper = new DreamDatabaseHelper(this);
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT d.title, d.content, d.nature, d.tags, d.created_at, u.username FROM dreams d JOIN users u ON d.user_id=u.user_id WHERE d.dream_id=?", new String[]{String.valueOf(dreamId)});
            if (c.moveToFirst()) {
                String title = c.getString(0);
                String content = c.getString(1);
                String nature = c.getString(2);
                String tags = c.getString(3);
                String createdAt = c.getString(4);
                String username = c.getString(5);

                binding.title.setText(title);
                binding.content.setText(content);
                binding.username.setText(username);
                binding.nature.setText("梦境类型：" + nature);
                binding.time.setText(createdAt);

                // 显示带图标的标签
                displayIconTags(tags);
            }
            c.close();
            db.close();
        }
    }

    private void displayIconTags(String tagsJson) {
        binding.chipGroupTags.removeAllViews();
        if (tagsJson == null || tagsJson.isEmpty()) return;

        try {
            // 简单处理标签字符串，如果是JSON数组则解析，否则按空格拆分
            String[] tags;
            if (tagsJson.startsWith("[")) {
                tagsJson = tagsJson.substring(1, tagsJson.length() - 1).replace("\"", "");
                tags = tagsJson.split(",");
            } else {
                tags = tagsJson.split("\\s+");
            }

            for (String tag : tags) {
                String trimmedTag = tag.trim();
                if (trimmedTag.isEmpty()) continue;

                Chip chip = new Chip(this);
                chip.setText(trimmedTag);
                chip.setChipBackgroundColorResource(R.color.tertiary_95);
                chip.setTextColor(getResources().getColor(R.color.tertiary_70));
                chip.setClickable(false);
                chip.setCheckable(false);
                binding.chipGroupTags.addView(chip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


