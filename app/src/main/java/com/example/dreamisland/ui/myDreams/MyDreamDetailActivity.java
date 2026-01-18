package com.example.dreamisland.ui.myDreams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dreamisland.R;
import com.example.dreamisland.databinding.ActivityMyDreamDetailBinding;
import com.example.dreamisland.model.Dream;
import com.example.dreamisland.ui.myDreams.MyDreamsViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyDreamDetailActivity extends AppCompatActivity {

    private static final String TAG = "MyDreamDetailActivity";
    private ActivityMyDreamDetailBinding binding;
    private MyDreamsViewModel myDreamsViewModel;
    private final Gson gson = new Gson();

    private int dreamId;
    private String title;
    private String content;
    private String nature;
    private String createdAt;
    private boolean isPublic;
    private boolean isFavorite;
    private List<String> tagsList = new ArrayList<>();
    private int userId = 1; // 默认用户ID

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 不要设置SupportActionBar，直接使用MaterialToolbar
        binding = ActivityMyDreamDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化ViewModel
        myDreamsViewModel = new androidx.lifecycle.ViewModelProvider(this).get(MyDreamsViewModel.class);

        // 获取并显示梦境数据
        getDreamDataFromIntent();
        displayDreamData();

        // 配置顶部按钮
        configureTopButtons();

        // 配置底部分享和收藏按钮
        configureBottomButtons();
    }

    /**
     * 从Intent获取梦境数据
     */
    private void getDreamDataFromIntent() {
        dreamId = getIntent().getIntExtra("dreamId", -1);
        userId = getIntent().getIntExtra("userId", 1); // 获取userId参数
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        nature = getIntent().getStringExtra("nature");
        createdAt = getIntent().getStringExtra("createdAt");
        isPublic = getIntent().getBooleanExtra("isPublic", false);
        isFavorite = getIntent().getBooleanExtra("isFavorite", false);

        // 解析标签
        String tagsJson = getIntent().getStringExtra("tags");
        if (tagsJson != null && !tagsJson.isEmpty()) {
            try {
                Type listType = new TypeToken<List<String>>() {}.getType();
                tagsList = gson.fromJson(tagsJson, listType);
            } catch (Exception e) {
                Log.e(TAG, "解析标签失败: " + e.getMessage());
            }
        }
    }

    /**
     * 显示梦境数据
     */
    private void displayDreamData() {
        binding.tvTitle.setText(title != null ? title : "");
        binding.tvNatureValue.setText(nature != null ? nature : "其他");
        binding.tvTimeValue.setText(createdAt != null ? createdAt : "");
        binding.tvPublicValue.setText(isPublic ? "已公开" : "私密");
        binding.tvContent.setText(content != null ? content : "");

        // 显示标签
        displayTags();
    }

    /**
     * 显示标签
     */
    private void displayTags() {
        ChipGroup chipGroup = binding.chipGroupTags;
        chipGroup.removeAllViews();

        if (tagsList != null && !tagsList.isEmpty()) {
            for (String tag : tagsList) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setClickable(false);
                chip.setCheckable(false);
                chipGroup.addView(chip);
            }
        }
    }

    /**
     * 配置顶部按钮
     */
    private void configureTopButtons() {
        // 返回按钮
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "返回按钮点击事件触发");
                finish(); // 直接返回上一个界面
            }
        });

        // 编辑按钮
        binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "编辑按钮点击事件触发");
                showEditDreamDialog();
            }
        });

        // 删除按钮
        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "删除按钮点击事件触发");
                showDeleteConfirmationDialog();
            }
        });
    }

    /**
     * 配置底部分享和收藏按钮
     */
    private void configureBottomButtons() {
        // 分享按钮
        binding.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "分享按钮被点击");
                shareDream();
            }
        });

        // 收藏按钮 - 设置初始状态
        binding.btnLike.setSelected(isFavorite);

        binding.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "收藏按钮被点击");
                toggleFavorite();
            }
        });
    }

    /**
     * 分享梦境
     */
    private void shareDream() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享梦境");
        intent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content);
        startActivity(Intent.createChooser(intent, "选择分享方式"));
    }

    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        boolean currentFavorite = binding.btnLike.isSelected();
        binding.btnLike.setSelected(!currentFavorite);
        String message = currentFavorite ? "取消收藏成功" : "收藏成功";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // 调用ViewModel切换收藏状态
        myDreamsViewModel.toggleFavorite(dreamId, userId, currentFavorite);
    }

    /**
     * 显示编辑梦境对话框
     */
    private void showEditDreamDialog() {
        Log.d(TAG, "显示编辑梦境对话框");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_dream, null);
        builder.setView(dialogView);

        // 设置对话框标题
        ((android.widget.TextView) dialogView.findViewById(R.id.tv_dialog_title)).setText("编辑梦境");

        // 获取对话框中的UI组件
        final android.widget.EditText etDreamTitle = dialogView.findViewById(R.id.et_dream_title);
        final android.widget.EditText etDreamContent = dialogView.findViewById(R.id.et_dream_content);
        final android.widget.RadioGroup rgDreamNature = dialogView.findViewById(R.id.rg_dream_nature);
        final android.widget.CheckBox cbMakePublic = dialogView.findViewById(R.id.cb_make_public);
        final android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        final android.widget.Button btnSave = dialogView.findViewById(R.id.btn_save);

        // 标签相关组件
        final android.widget.EditText etTagInput = dialogView.findViewById(R.id.et_tag_input);
        final android.widget.Button btnAddTag = dialogView.findViewById(R.id.btn_add_tag);
        final android.widget.LinearLayout layoutTagsContainer = dialogView.findViewById(R.id.layout_tags_container);
        final android.widget.LinearLayout layoutTags = dialogView.findViewById(R.id.layout_tags);

        // 标签列表
        final List<String> newTagsList = new ArrayList<>(tagsList);

        // 设置当前梦境数据
        etDreamTitle.setText(title);
        etDreamContent.setText(content);

        // 设置梦境性质
        if (nature != null) {
            if (nature.equals("好梦")) {
                rgDreamNature.check(R.id.rb_good_dream);
            } else if (nature.equals("噩梦")) {
                rgDreamNature.check(R.id.rb_bad_dream);
            } else {
                rgDreamNature.check(R.id.rb_other_dream);
            }
        }

        // 设置公开状态
        cbMakePublic.setChecked(isPublic);

        // 显示现有标签
        if (!newTagsList.isEmpty()) {
            layoutTagsContainer.setVisibility(View.VISIBLE);
            for (String tag : newTagsList) {
                addTagView(layoutTags, tag, newTagsList, layoutTagsContainer);
            }
        }

        final AlertDialog dialog = builder.create();

        // 取消按钮
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 添加标签按钮
        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = etTagInput.getText().toString().trim();
                if (tag.isEmpty()) {
                    Toast.makeText(MyDreamDetailActivity.this, "请输入标签", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newTagsList.contains(tag)) {
                    Toast.makeText(MyDreamDetailActivity.this, "标签已存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newTagsList.size() >= 5) {
                    Toast.makeText(MyDreamDetailActivity.this, "最多只能添加5个标签", Toast.LENGTH_SHORT).show();
                    return;
                }

                newTagsList.add(tag);
                addTagView(layoutTags, tag, newTagsList, layoutTagsContainer);
                layoutTagsContainer.setVisibility(View.VISIBLE);
                etTagInput.setText("");
            }
        });

        // 保存按钮
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = etDreamTitle.getText().toString().trim();
                String newContent = etDreamContent.getText().toString().trim();

                if (newTitle.isEmpty()) {
                    Toast.makeText(MyDreamDetailActivity.this, "请输入标题", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newContent.isEmpty()) {
                    Toast.makeText(MyDreamDetailActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 获取梦境性质
                String newNature = "其他";
                int selectedNatureId = rgDreamNature.getCheckedRadioButtonId();
                if (selectedNatureId == R.id.rb_good_dream) {
                    newNature = "好梦";
                } else if (selectedNatureId == R.id.rb_bad_dream) {
                    newNature = "噩梦";
                }

                // 获取公开状态
                boolean newIsPublic = cbMakePublic.isChecked();

                // 创建更新后的梦境对象
                Dream updatedDream = new Dream();
                updatedDream.setDreamId(dreamId);
                updatedDream.setUserId(userId);
                updatedDream.setTitle(newTitle);
                updatedDream.setContent(newContent);
                updatedDream.setNature(newNature);
                updatedDream.setTags(gson.toJson(newTagsList));
                updatedDream.setPublic(newIsPublic);
                updatedDream.setCreatedAt(createdAt);

                // 更新数据库
                myDreamsViewModel.updateDream(updatedDream);

                // 更新UI
                title = newTitle;
                content = newContent;
                nature = newNature;
                isPublic = newIsPublic;
                tagsList = newTagsList;
                displayDreamData();

                Toast.makeText(MyDreamDetailActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 添加标签视图
     */
    private void addTagView(android.widget.LinearLayout parent, String tag,
                            final List<String> tagsList, final android.widget.LinearLayout tagsContainer) {
        final android.widget.LinearLayout tagLayout = new android.widget.LinearLayout(this);
        tagLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        tagLayout.setPadding(8, 4, 8, 4);
        tagLayout.setBackgroundColor(getResources().getColor(R.color.blue_light));
        tagLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(4, 4, 4, 4);
        tagLayout.setLayoutParams(layoutParams);

        // 创建标签文本
        android.widget.TextView tagText = new android.widget.TextView(this);
        tagText.setText(tag);
        tagText.setTextColor(getResources().getColor(R.color.blue_dark));
        tagText.setTextSize(12);
        tagText.setPadding(0, 0, 8, 0);

        // 创建删除按钮
        android.widget.Button deleteBtn = new android.widget.Button(this);
        deleteBtn.setText("×");
        deleteBtn.setTextColor(getResources().getColor(R.color.blue_dark));
        deleteBtn.setTextSize(14);
        deleteBtn.setPadding(4, 0, 4, 0);
        deleteBtn.setBackground(null);

        // 删除按钮点击事件
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String removedTag = ((android.widget.TextView) tagLayout.getChildAt(0)).getText().toString();
                tagsList.remove(removedTag);
                parent.removeView(tagLayout);

                if (tagsList.isEmpty()) {
                    tagsContainer.setVisibility(View.GONE);
                }
            }
        });

        tagLayout.addView(tagText);
        tagLayout.addView(deleteBtn);
        parent.addView(tagLayout);
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmationDialog() {
        Log.d(TAG, "显示删除确认对话框");

        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这个梦境吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    Log.d(TAG, "确认删除梦境，ID: " + dreamId);
                    // 删除梦境
                    myDreamsViewModel.deleteDream(dreamId, userId);
                    Toast.makeText(MyDreamDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    // 返回上一个界面
                    finish();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
