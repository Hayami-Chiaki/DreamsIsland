package com.example.dreamisland.ui.square.discussion.detail;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dreamisland.data.SquareRepository;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.databinding.ActivityPostDetailBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    private ActivityPostDetailBinding binding;
    private RepliesAdapter adapter;
    private final List<String> replies = new ArrayList<>();
    private int postId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postId = getIntent().getIntExtra("post_id", -1);

        adapter = new RepliesAdapter(replies);
        binding.replies.setLayoutManager(new LinearLayoutManager(this));
        binding.replies.setAdapter(adapter);

        loadPost();
        loadReplies();

        binding.btnReply.setOnClickListener(v -> doReply());
    }

    private void loadPost() {
        DreamDatabaseHelper helper = new DreamDatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT p.title, p.content, p.created_at, u.username FROM posts p JOIN users u ON p.user_id=u.user_id WHERE p.post_id=?", new String[]{String.valueOf(postId)});
        if (c.moveToFirst()) {
            binding.title.setText(c.getString(0));
            binding.content.setText(c.getString(1));
            binding.meta.setText(c.getString(3) + " · " + c.getString(2));
        }
        c.close();
        db.close();
    }

    private void loadReplies() {
        replies.clear();
        DreamDatabaseHelper helper = new DreamDatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT r.content, r.created_at, u.username FROM replies r JOIN users u ON r.user_id=u.user_id WHERE r.post_id=? ORDER BY r.created_at ASC", new String[]{String.valueOf(postId)});
        while (c.moveToNext()) {
            replies.add(c.getString(2) + ": " + c.getString(0) + "  (" + c.getString(1) + ")");
        }
        c.close();
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void doReply() {
        String text = binding.inputReply.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "请输入回复内容", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sp = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        int userId = sp.getInt("logged_in_user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        SquareRepository.getInstance(this).insertReply(postId, userId, text, now);
        binding.inputReply.setText("");
        loadReplies();
    }
}


