package com.example.dreamisland.ui.auth;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamisland.MainActivity;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.databinding.ActivityRegisterBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private DreamDatabaseHelper databaseHelper;
    private Bitmap avatarBitmap;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        avatarBitmap = bmp;
                        binding.avatarImageView.setImageBitmap(bmp);
                    } catch (IOException e) {
                        Toast.makeText(this, "选择头像失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("注册");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = new DreamDatabaseHelper(this);

        binding.pickAvatarButton.setOnClickListener(v -> pickAvatar());
        binding.registerButton.setOnClickListener(v -> doRegister());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void doRegister() {
        String username = binding.usernameEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // 检查是否已存在同名用户
        Cursor c = db.query("users", new String[]{"user_id"}, "username = ?", new String[]{username}, null, null, null);
        if (c.moveToFirst()) {
            c.close();
            db.close();
            Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
            return;
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        if (avatarBitmap != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            values.put("avatar", out.toByteArray());
        }
        long rowId = db.insert("users", null, values);
        if (rowId != -1) {
            // 登录并跳转
            saveLoggedInUser((int) rowId);
            db.close();
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            db.close();
            Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoggedInUser(int userId) {
        SharedPreferences sp = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        sp.edit().putInt("logged_in_user_id", userId).apply();
    }
}


