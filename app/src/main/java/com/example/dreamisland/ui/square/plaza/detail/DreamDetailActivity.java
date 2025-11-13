package com.example.dreamisland.ui.square.plaza.detail;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.databinding.ActivityDreamDetailBinding;

public class DreamDetailActivity extends AppCompatActivity {
    private ActivityDreamDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDreamDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int dreamId = getIntent().getIntExtra("dream_id", -1);
        if (dreamId != -1) {
            DreamDatabaseHelper helper = new DreamDatabaseHelper(this);
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT d.title, d.content, d.nature, d.tags, d.created_at, u.username FROM dreams d JOIN users u ON d.user_id=u.user_id WHERE d.dream_id=?", new String[]{String.valueOf(dreamId)});
            if (c.moveToFirst()) {
                binding.title.setText(c.getString(0));
                binding.content.setText(c.getString(1));
                binding.meta.setText(c.getString(5) + " · " + c.getString(2) + " · " + c.getString(3));
                binding.time.setText(c.getString(4));
            }
            c.close();
            db.close();
        }
    }
}


