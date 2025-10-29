package com.example.dreamisland;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.example.dreamisland.database.DreamDatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dreamisland.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DreamDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化数据库
        databaseHelper = new DreamDatabaseHelper(this);

        // 测试数据库连接（开发阶段使用）
        testDatabaseConnection();

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 禁用图标着色
        navView.setItemIconTintList(null);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_my_dreams, R.id.navigation_my_sleep, R.id.navigation_square, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void testDatabaseConnection() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        if (db.isOpen()) {
            Log.d("MainActivity", "Database connected successfully");
            db.close();
        }
    }


}