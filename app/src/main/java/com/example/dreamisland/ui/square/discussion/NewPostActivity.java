package com.example.dreamisland.ui.square.discussion;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamisland.R;
import com.example.dreamisland.data.SquareRepository;
import com.example.dreamisland.databinding.ActivityNewPostBinding;

public class NewPostActivity extends AppCompatActivity {
    private ActivityNewPostBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topBar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("发布新帖子");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.btnPublish.setOnClickListener(v -> publish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void publish() {
        String title = binding.inputTitle.getText().toString().trim();
        String content = binding.inputContent.getText().toString().trim();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请填写标题和内容", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sp = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        int userId = sp.getInt("logged_in_user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        // 格式化当前时间为用户友好的日期时间格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String now = dateFormat.format(new Date());
        SquareRepository.getInstance(this).insertPost(userId, title, content, now);
        Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}


