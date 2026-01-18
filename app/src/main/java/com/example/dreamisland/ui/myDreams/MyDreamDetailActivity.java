package com.example.dreamisland.ui.myDreams;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.appbar.MaterialToolbar;

import com.example.dreamisland.databinding.ActivityMyDreamDetailBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class MyDreamDetailActivity extends AppCompatActivity {
    private ActivityMyDreamDetailBinding binding;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyDreamDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar toolbar = binding.topAppBar;
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String nature = getIntent().getStringExtra("nature");
        String createdAt = getIntent().getStringExtra("createdAt");
        boolean isPublic = getIntent().getBooleanExtra("isPublic", false);
        String tagsJson = getIntent().getStringExtra("tags");

        binding.tvTitle.setText(title != null ? title : "");
        binding.tvContent.setText(content != null ? content : "");
        binding.tvNatureValue.setText(nature != null ? nature : "");
        binding.tvTimeValue.setText(createdAt != null ? createdAt : "");
        binding.tvPublicValue.setText(isPublic ? "已公开" : "私密");

        if (tagsJson != null && !tagsJson.isEmpty()) {
            try {
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> tags = gson.fromJson(tagsJson, listType);
                ChipGroup group = binding.chipGroupTags;
                group.removeAllViews();
                if (tags != null) {
                    for (String t : tags) {
                        Chip chip = new Chip(this);
                        chip.setText(t);
                        chip.setClickable(false);
                        chip.setCheckable(false);
                        group.addView(chip);
                    }
                }
            } catch (Exception ignored) {}
        }

        ImageButton share = binding.btnShare;
        ImageButton like = binding.btnLike;
        share.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "分享梦境");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, title + "\n\n" + content);
            startActivity(android.content.Intent.createChooser(intent, "选择分享方式"));
        });
        like.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == com.example.dreamisland.R.id.action_edit) {
            android.widget.Toast.makeText(this, "编辑功能暂未实现", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == com.example.dreamisland.R.id.action_delete) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("确认删除")
                    .setMessage("确定要删除这个梦境吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        android.widget.Toast.makeText(this, "删除成功", android.widget.Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return false;
    }
}
