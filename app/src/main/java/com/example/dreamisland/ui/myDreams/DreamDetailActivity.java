package com.example.dreamisland.ui.myDreams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamisland.R;
import com.example.dreamisland.model.Dream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class DreamDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvNature;
    private TextView tvContent;
    private TextView tvTags;
    private TextView tvCreatedAt;
    private Button btnBack;
    private boolean readOnly = false;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dream_detail);

        gson = new Gson();
        initUI();
        getIntentData();
        setupButtonListeners();
    }

    private void initUI() {
        tvTitle = findViewById(R.id.tv_dream_title);
        tvNature = findViewById(R.id.tv_dream_nature);
        tvContent = findViewById(R.id.tv_dream_content);
        tvTags = findViewById(R.id.tv_dream_tags);
        tvCreatedAt = findViewById(R.id.tv_dream_created_at);
        btnBack = findViewById(R.id.btn_back);
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
            readOnly = intent.getBooleanExtra("readOnly", false);

            // 更新UI
            tvTitle.setText(title);
            tvNature.setText(nature);
            tvContent.setText(content);
            tvCreatedAt.setText(createdAt);

            // 显示标签
            if (!android.text.TextUtils.isEmpty(tagsJson)) {
                List<String> tags = gson.fromJson(tagsJson, new TypeToken<List<String>>(){}.getType());
                if (tags != null && !tags.isEmpty()) {
                    StringBuilder tagsBuilder = new StringBuilder();
                    for (int i = 0; i < tags.size(); i++) {
                        tagsBuilder.append(tags.get(i));
                        if (i < tags.size() - 1) {
                            tagsBuilder.append(" ");
                        }
                    }
                    tvTags.setText(tagsBuilder.toString());
                } else {
                    tvTags.setText("无标签");
                }
            } else {
                tvTags.setText("无标签");
            }
        }
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
