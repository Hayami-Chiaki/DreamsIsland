package com.example.dreamisland.ui.myDreams;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.util.TypedValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.adapter.DreamAdapter;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.model.Dream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class MyDreamsFragment extends Fragment implements DreamAdapter.OnDreamClickListener {

    private MyDreamsViewModel myDreamsViewModel;
    private RecyclerView recyclerViewDreams;
    private DreamAdapter dreamAdapter;
    private Button btnRecordDream, btnViewMatchDream, btnDisconnectMatch;
    private TextView tvMatchHint;
    private LinearLayout layoutEmpty;
    private Context context;
    private int currentUserId; // 登录用户ID

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_dreams, container, false);

        // 初始化UI组件
        initUI(view);

        // 设置ViewModel
        setupViewModel();

        // 设置RecyclerView
        setupRecyclerView();

        // 设置按钮点击事件
        setupButtonListeners();

        // 从SharedPreferences获取当前登录用户ID
        SharedPreferences sp = requireActivity().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentUserId = sp.getInt("logged_in_user_id", 1); // 默认用户ID为1

        // 加载梦境列表
        myDreamsViewModel.loadUserDreams(currentUserId);

        return view;
    }

    private void initUI(View view) {
        btnRecordDream = view.findViewById(R.id.btn_record_dream);
        btnViewMatchDream = view.findViewById(R.id.btn_view_match_dream);
        btnDisconnectMatch = view.findViewById(R.id.btn_disconnect_match);
        tvMatchHint = view.findViewById(R.id.tv_match_hint);
        recyclerViewDreams = view.findViewById(R.id.recyclerView_dreams);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // 添加长按事件到匹配提示文本，用于查看匹配用户的梦境
        tvMatchHint.setOnLongClickListener(v -> {
            viewMatchedUserDreams();
            return true;
        });
    }

    private void setupViewModel() {
        myDreamsViewModel = new ViewModelProvider(this).get(MyDreamsViewModel.class);

        // 观察梦境列表
        myDreamsViewModel.getDreamsList().observe(getViewLifecycleOwner(), dreams -> {
            dreamAdapter.setDreamList(dreams);
            updateEmptyState(dreams.isEmpty());
        });

        // 观察加载状态
        myDreamsViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // 可以在这里添加加载指示器
        });

        // 观察错误信息
        myDreamsViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (!TextUtils.isEmpty(errorMessage)) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // 观察匹配状态
        myDreamsViewModel.isMatching().observe(getViewLifecycleOwner(), isMatching -> {
            if (isMatching) {
                tvMatchHint.setText("正在匹配中...");
                btnViewMatchDream.setEnabled(false);
                btnDisconnectMatch.setEnabled(false);
            } else {
                btnViewMatchDream.setEnabled(true);

                // 如果有匹配，断开按钮也应该可用
                MyDreamsViewModel.MatchInfo currentMatch = myDreamsViewModel.getCurrentMatch().getValue();
                btnDisconnectMatch.setEnabled(currentMatch != null);
            }
        });

        // 观察当前匹配
        myDreamsViewModel.getCurrentMatch().observe(getViewLifecycleOwner(), matchInfo -> {
            if (matchInfo != null) {
                // 匹配成功
                tvMatchHint.setText("已匹配到用户: " + matchInfo.getMatchedUsername());
                btnViewMatchDream.setText("查看匹配对象的梦境");
                btnViewMatchDream.setEnabled(true);
                btnDisconnectMatch.setVisibility(View.VISIBLE);
                btnDisconnectMatch.setEnabled(true);

                Toast.makeText(context, "匹配成功！点击下方按钮可以查看对方的梦境", Toast.LENGTH_LONG).show();
            } else {
                // 没有匹配或已断开匹配
                tvMatchHint.setText("暂无匹配对象");
                btnViewMatchDream.setText("开始匹配用户");
                btnViewMatchDream.setEnabled(true);
                btnDisconnectMatch.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        dreamAdapter = new DreamAdapter(context, new ArrayList<>(), this);
        recyclerViewDreams.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewDreams.setAdapter(dreamAdapter);
    }

    private void setupButtonListeners() {
        // 记录梦境按钮点击事件
        btnRecordDream.setOnClickListener(v -> showAddDreamDialog());

        // 开始匹配/查看匹配对象梦境按钮点击事件
        btnViewMatchDream.setOnClickListener(v -> {
            MyDreamsViewModel.MatchInfo currentMatch = myDreamsViewModel.getCurrentMatch().getValue();
            if (currentMatch != null) {
                // 已经匹配，点击查看匹配对象的梦境
                viewMatchedUserDreams();
            } else {
                // 没有匹配，点击开始匹配
                startMatching();
            }
        });

        // 断开匹配按钮点击事件
        btnDisconnectMatch.setOnClickListener(v -> {
            // 已经匹配，点击断开匹配
            MyDreamsViewModel.MatchInfo currentMatch = myDreamsViewModel.getCurrentMatch().getValue();
            if (currentMatch != null) {
                showDisconnectMatchDialog(currentMatch);
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerViewDreams.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewDreams.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次返回该界面时重新加载数据，确保显示最新的梦境列表
        myDreamsViewModel.loadUserDreams(currentUserId);
    }

    // 显示添加梦境对话框
    private void showAddDreamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_dream, null);
        builder.setView(dialogView);

        // 设置对话框标题
        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        tvDialogTitle.setText("记录梦境");

        // 获取对话框中的UI组件
        EditText etDreamTitle = dialogView.findViewById(R.id.et_dream_title);
        EditText etDreamContent = dialogView.findViewById(R.id.et_dream_content);
        RadioGroup rgDreamNature = dialogView.findViewById(R.id.rg_dream_nature);
        CheckBox cbMakePublic = dialogView.findViewById(R.id.cb_make_public);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // 标签相关组件
        EditText etTagInput = dialogView.findViewById(R.id.et_tag_input);
        Button btnAddTag = dialogView.findViewById(R.id.btn_add_tag);
        LinearLayout layoutTagsContainer = dialogView.findViewById(R.id.layout_tags_container);
        LinearLayout layoutTags = dialogView.findViewById(R.id.layout_tags);

        // 标签列表
        List<String> tagsList = new ArrayList<>();

        AlertDialog dialog = builder.create();

        // 取消按钮点击事件
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // 添加标签按钮点击事件
        btnAddTag.setOnClickListener(v -> {
            String tag = etTagInput.getText().toString().trim();

            // 验证标签
            if (TextUtils.isEmpty(tag)) {
                Toast.makeText(context, "请输入标签内容", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tag.length() > 10) {
                Toast.makeText(context, "标签不能超过10个字符", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tagsList.contains(tag)) {
                Toast.makeText(context, "标签已存在", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tagsList.size() >= 5) {
                Toast.makeText(context, "最多只能添加5个标签", Toast.LENGTH_SHORT).show();
                return;
            }

            // 添加标签
            tagsList.add(tag);
            addTagView(layoutTags, tag, tagsList, layoutTagsContainer);
            etTagInput.setText("");
        });

        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            String title = etDreamTitle.getText().toString().trim();
            String content = etDreamContent.getText().toString().trim();

            // 验证输入
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(context, "请输入梦境标题", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(context, "请输入梦境内容", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取梦境性质
            String nature = "其他";
            int selectedId = rgDreamNature.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_good_dream) {
                nature = "好梦";
            } else if (selectedId == R.id.rb_bad_dream) {
                nature = "噩梦";
            }

            // 创建时间
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

            // 创建Dream对象
            Dream dream = new Dream();
            dream.setUserId(currentUserId);
            dream.setTitle(title);
            dream.setContent(content);
            dream.setNature(nature);
            dream.setTags(gson.toJson(tagsList)); // 将标签列表转换为JSON字符串
            dream.setPublic(cbMakePublic.isChecked());
            dream.setCreatedAt(currentTime);

            // 添加到数据库
            myDreamsViewModel.addDream(dream);

            // 显示成功消息
            Toast.makeText(context, "梦境记录成功", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 添加标签视图
     */
    private void addTagView(LinearLayout parent, String tag, List<String> tagsList, LinearLayout tagsContainer) {
        // 创建标签容器
        LinearLayout tagLayout = new LinearLayout(context);
        tagLayout.setOrientation(LinearLayout.HORIZONTAL);
        tagLayout.setPadding(8, 4, 8, 4);
        tagLayout.setBackgroundColor(context.getResources().getColor(R.color.blue_light));
        tagLayout.setGravity(Gravity.CENTER_VERTICAL);

        // 创建标签文本
        TextView tagText = new TextView(context);
        tagText.setText(tag);
        tagText.setTextColor(context.getResources().getColor(R.color.blue_dark));
        tagText.setTextSize(12);
        tagText.setPadding(0, 0, 8, 0);

        // 创建删除按钮
        Button deleteBtn = new Button(context);
        deleteBtn.setText("×");
        deleteBtn.setTextColor(context.getResources().getColor(R.color.blue_dark));
        deleteBtn.setTextSize(14);
        deleteBtn.setPadding(4, 0, 4, 0);
        deleteBtn.setBackground(null);
        deleteBtn.setWidth(20);
        deleteBtn.setHeight(20);

        // 删除按钮点击事件
        deleteBtn.setOnClickListener(v -> {
            tagsList.remove(tag);
            parent.removeView(tagLayout);

            // 如果没有标签，隐藏标签容器
            if (tagsList.isEmpty()) {
                tagsContainer.setVisibility(View.GONE);
            }
        });

        // 添加到标签布局
        tagLayout.addView(tagText);
        tagLayout.addView(deleteBtn);

        // 添加到父容器
        parent.addView(tagLayout);

        // 显示标签容器
        tagsContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Gson实例，用于JSON转换
     */
    private Gson gson = new Gson();

    // 跳转到梦境详情页面
    private void openDreamDetailPage(Dream dream) {
        android.content.Intent intent = new android.content.Intent(context, MyDreamDetailActivity.class);
        intent.putExtra("dreamId", dream.getDreamId());
        intent.putExtra("userId", currentUserId); // 添加userId参数传递
        intent.putExtra("title", dream.getTitle());
        intent.putExtra("content", dream.getContent());
        intent.putExtra("nature", dream.getNature());
        intent.putExtra("createdAt", dream.getCreatedAt());
        intent.putExtra("isPublic", dream.isPublic());
        intent.putExtra("isFavorite", dream.isFavorite());
        intent.putExtra("tags", dream.getTags());
        startActivity(intent);
    }

    // 显示删除确认对话框
    private void showDeleteConfirmDialog(final Dream dream) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确认删除")
                .setMessage("确定要删除这个梦境吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    myDreamsViewModel.deleteDream(dream.getDreamId(), currentUserId);
                    Toast.makeText(context, "梦境已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 开始匹配
     */
    private void startMatching() {
        // 检查是否已经有匹配
        if (myDreamsViewModel.getCurrentMatch().getValue() != null) {
            Toast.makeText(context, "已经处于匹配状态", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新UI状态
        tvMatchHint.setText("正在匹配中...");
        btnViewMatchDream.setEnabled(false);

        // 实际的匹配过程，从数据库获取用户列表
        new Thread(() -> {
            try {
                // 从ViewModel获取所有用户列表
                List<Map<String, String>> allUsers = getAvailableUsers();

                // 过滤掉当前用户，只保留其他用户
                List<Map<String, String>> availableUsers = new ArrayList<>();
                for (Map<String, String> user : allUsers) {
                    int userId = Integer.parseInt(user.get("user_id"));
                    if (userId != currentUserId) {
                        availableUsers.add(user);
                    }
                }

                // 在主线程显示匹配结果
                requireActivity().runOnUiThread(() -> {
                    if (availableUsers.isEmpty()) {
                        // 只有一个用户，匹配失败
                        Toast.makeText(context, "没有找到可匹配的用户", Toast.LENGTH_SHORT).show();
                        tvMatchHint.setText("暂无匹配对象");
                        btnViewMatchDream.setEnabled(true);
                        btnViewMatchDream.setText("开始匹配用户");
                    } else {
                        // 有其他用户，随机选择一个
                        int randomIndex = new Random().nextInt(availableUsers.size());
                        Map<String, String> selectedUser = availableUsers.get(randomIndex);
                        String matchedUsername = selectedUser.get("username");
                        showMatchConfirmDialog(matchedUsername);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(context, "匹配过程中发生错误", Toast.LENGTH_SHORT).show();
                    tvMatchHint.setText("暂无匹配对象");
                    btnViewMatchDream.setEnabled(true);
                    btnViewMatchDream.setText("开始匹配用户");
                });
            }
        }).start();
    }

    /**
     * 获取所有可用用户列表
     */
    private List<Map<String, String>> getAvailableUsers() {
        List<Map<String, String>> users = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            // 直接访问数据库获取所有用户
            DreamDatabaseHelper dbHelper = new DreamDatabaseHelper(context);
            db = dbHelper.getReadableDatabase();

            // 查询所有用户
            String query = "SELECT user_id, username FROM users";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, String> user = new HashMap<>();
                    user.put("user_id", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))));
                    user.put("username", cursor.getString(cursor.getColumnIndexOrThrow("username")));
                    users.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return users;
    }

    /**
     * 显示匹配确认对话框
     */
    private void showMatchConfirmDialog(String matchedUsername) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("匹配成功")
                .setMessage("已匹配到用户：" + matchedUsername)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 用户确认，进入匹配状态
                    myDreamsViewModel.confirmMatching(currentUserId, matchedUsername);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 用户取消，恢复初始状态
                    tvMatchHint.setText("暂无匹配对象");
                    btnViewMatchDream.setEnabled(true);
                    btnViewMatchDream.setText("开始匹配用户");
                })
                .setCancelable(false); // 不可点击外部取消

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 显示断开匹配确认对话框
     */
    private void showDisconnectMatchDialog(final MyDreamsViewModel.MatchInfo matchInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确认断开匹配")
                .setMessage("确定要断开与" + matchInfo.getMatchedUsername() + "的匹配吗？")
                .setPositiveButton("断开", (dialog, which) -> {
                    myDreamsViewModel.endMatching(matchInfo.getMatchId());
                    Toast.makeText(context, "已断开匹配", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 查看匹配用户的梦境
     */
    private void viewMatchedUserDreams() {
        MyDreamsViewModel.MatchInfo matchInfo = myDreamsViewModel.getCurrentMatch().getValue();
        if (matchInfo == null) {
            Toast.makeText(context, "没有匹配对象", Toast.LENGTH_SHORT).show();
            return;
        }

        // 启动MatchedDreamsActivity，传递匹配用户的ID和名称
        Intent intent = new Intent(context, MatchedDreamsActivity.class);
        intent.putExtra("matchedUserId", matchInfo.getMatchedUserId());
        intent.putExtra("matchedUsername", matchInfo.getMatchedUsername());
        startActivity(intent);
    }

    @Override
    public void onDreamClick(Dream dream) {
        openDreamDetailPage(dream);
    }

    @Override
    public void onDreamLongClick(Dream dream) {
        // 长按梦境项时显示操作菜单
        showDreamOptionsDialog(dream);
    }

    private void showDreamOptionsDialog(final Dream dream) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择操作")
                .setItems(new String[]{"查看详情", "编辑梦境", "删除梦境"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openDreamDetailPage(dream);
                            break;
                        case 1:
                            Toast.makeText(context, "编辑功能暂未实现", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            showDeleteConfirmDialog(dream);
                            break;
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
