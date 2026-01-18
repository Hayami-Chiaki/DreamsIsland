package com.example.dreamisland.ui.myDreams;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.adapter.DreamAdapter;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.model.Dream;

import java.util.ArrayList;
import java.util.List;

public class MatchedDreamsActivity extends AppCompatActivity implements DreamAdapter.OnDreamClickListener {

    private TextView tvTitle;
    private Button btnBack;
    private RecyclerView rvMatchedDreams;
    private LinearLayout layoutEmpty;
    private DreamAdapter dreamAdapter;
    private List<Dream> matchedDreams;
    private String matchedUsername;
    private int matchedUserId;
    private DreamDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_dreams);

        dbHelper = new DreamDatabaseHelper(this);
        initUI();
        setupRecyclerView();
        getIntentData();
        loadMatchedUserDreams();
        setupButtonListeners();
    }

    private void initUI() {
        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_back);
        rvMatchedDreams = findViewById(R.id.rv_matched_dreams);
        layoutEmpty = findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        matchedDreams = new ArrayList<>();
        dreamAdapter = new DreamAdapter(this, matchedDreams, this);
        rvMatchedDreams.setLayoutManager(new LinearLayoutManager(this));
        rvMatchedDreams.setAdapter(dreamAdapter);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            matchedUsername = intent.getStringExtra("matchedUsername");
            matchedUserId = intent.getIntExtra("matchedUserId", -1);
        }
    }

    /**
     * 从数据库加载匹配用户的梦境记录
     */
    private void loadMatchedUserDreams() {
        if (matchedUserId == -1) {
            // 如果没有匹配用户ID，显示空状态
            updateEmptyState();
            return;
        }

        new Thread(() -> {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = dbHelper.getReadableDatabase();
                String query = "SELECT dream_id, user_id, title, content, nature, tags, is_public, is_favorite, created_at " +
                        "FROM dreams WHERE user_id = ? ORDER BY created_at DESC";
                cursor = db.rawQuery(query, new String[]{String.valueOf(matchedUserId)});

                List<Dream> dreams = new ArrayList<>();
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        Dream dream = new Dream(
                                cursor.getInt(cursor.getColumnIndexOrThrow("dream_id")),
                                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                                cursor.getString(cursor.getColumnIndexOrThrow("content")),
                                cursor.getString(cursor.getColumnIndexOrThrow("nature")),
                                cursor.getString(cursor.getColumnIndexOrThrow("tags")),
                                cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1,
                                cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1,
                                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                        );
                        dreams.add(dream);
                    } while (cursor.moveToNext());
                }

                // 更新UI
                runOnUiThread(() -> {
                    matchedDreams = dreams;
                    updateUI();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    updateEmptyState();
                });
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }
        }).start();
    }

    private void updateUI() {
        if (matchedDreams == null) {
            matchedDreams = new ArrayList<>();
        }

        // 更新标题
        if (!android.text.TextUtils.isEmpty(matchedUsername)) {
            tvTitle.setText(matchedUsername + "的梦境");
        }

        // 更新梦境列表
        dreamAdapter.setDreamList(matchedDreams);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (matchedDreams.isEmpty()) {
            rvMatchedDreams.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvMatchedDreams.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onDreamClick(Dream dream) {
        // 查看梦境详情
        Intent intent = new Intent(this, MatchedDreamDetailActivity.class);
        intent.putExtra("dreamId", dream.getDreamId());
        intent.putExtra("title", dream.getTitle());
        intent.putExtra("content", dream.getContent());
        intent.putExtra("nature", dream.getNature());
        intent.putExtra("createdAt", dream.getCreatedAt());
        intent.putExtra("isPublic", dream.isPublic());
        intent.putExtra("isFavorite", dream.isFavorite());
        intent.putExtra("tags", dream.getTags());
        intent.putExtra("readOnly", true); // 设置为只读模式
        startActivity(intent);
    }

    @Override
    public void onDreamLongClick(Dream dream) {
        // 长按时不执行任何操作
    }
}
