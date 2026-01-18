package com.example.dreamisland;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.dreamisland.database.DreamDatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dreamisland.databinding.ActivityMainBinding;
import com.example.dreamisland.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    // 声明 toolbar 和 binding 成员变量
    private MaterialToolbar toolbar;
    private DreamDatabaseHelper databaseHelper;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 ViewBinding，绑定布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 检查登录状态，未登录跳转登录页
        if (!isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 初始化数据库
        databaseHelper = new DreamDatabaseHelper(this);

        // 测试数据库连接（开发阶段使用）
        testDatabaseConnection();

        // 使用 ViewBinding 查找控件，替代 findViewById（更安全）
        toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);

        BottomNavigationView navView = binding.navView;

        // 禁用图标着色
        navView.setItemIconTintList(null);

        // 配置顶部导航栏与底部导航的对应关系
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_my_dreams, R.id.navigation_my_sleep, R.id.navigation_square, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 监听导航变化，根据目标页面设置不同的 Toolbar 显示效果
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // 获取当前导航目的地的 id
            int id = destination.getId();

            // 广场页面使用自己内部的 Toolbar，所以隐藏全局的
            if (id == R.id.navigation_square) {
                toolbar.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                // 根据目的地设置 Toolbar 标题
                if (id == R.id.navigation_my_dreams) {
                    toolbar.setTitle("我的梦");
                } else if (id == R.id.navigation_my_sleep) {
                    toolbar.setTitle("我的睡眠");
                } else if (id == R.id.navigation_profile) {
                    toolbar.setTitle("我的");
                } else {
                    toolbar.setTitle(getString(R.string.app_name));
                }
            }
        });
    }

    /**
     * 检查用户是否已登录（通过 SharedPreferences 验证）
     */
    private boolean isLoggedIn() {
        SharedPreferences sp = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        return sp.getInt("logged_in_user_id", -1) != -1;
    }

    /**
     * 测试数据库连接是否成功（开发阶段调试用）
     */
    private void testDatabaseConnection() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        if (db.isOpen()) {
            Log.d("MainActivity", "Database connected successfully");
            // 注意：正式环境中，此处不建议立即 close，避免后续操作需要重新获取连接
            db.close();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
