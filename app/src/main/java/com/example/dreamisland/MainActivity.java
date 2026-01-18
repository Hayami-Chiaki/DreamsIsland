package com.example.dreamisland;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private androidx.appcompat.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!isLoggedIn()) {
            startActivity(new Intent(this, com.example.dreamisland.ui.auth.LoginActivity.class));
            finish();
            return;
        }

        // 初始化数据库
        databaseHelper = new DreamDatabaseHelper(this);

        // 测试数据库连接（开发阶段使用）
        testDatabaseConnection();

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

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

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (toolbar != null) {
                int id = destination.getId();
                if (id == R.id.navigation_my_dreams) {
                    toolbar.setTitle("我的梦");
                } else if (id == R.id.navigation_my_sleep) {
                    toolbar.setTitle("我的睡眠");
                } else if (id == R.id.navigation_square) {
                    toolbar.setTitle("广场");
                } else if (id == R.id.navigation_profile) {
                    toolbar.setTitle("我的");
                } else {
                    toolbar.setTitle(getString(R.string.app_name));
                }
            }
        });
    }

    private boolean isLoggedIn() {
        SharedPreferences sp = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        return sp.getInt("logged_in_user_id", -1) != -1;
    }

    private void testDatabaseConnection() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        if (db.isOpen()) {
            Log.d("MainActivity", "Database connected successfully");
            db.close();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

}
