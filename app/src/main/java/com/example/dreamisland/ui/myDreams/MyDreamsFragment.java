package com.example.dreamisland.ui.myDreams;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.adapter.DreamAdapter;
import com.example.dreamisland.model.Dream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDreamsFragment extends Fragment implements DreamAdapter.OnDreamClickListener {

    private MyDreamsViewModel myDreamsViewModel;
    private RecyclerView recyclerViewDreams;
    private DreamAdapter dreamAdapter;
    private Button btnRecordDream, btnViewMatchDream;
    private TextView tvMatchHint;
    private LinearLayout layoutEmpty;
    private Context context;
    private int currentUserId = 1; // 默认用户ID

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
        
        // 加载梦境列表
        myDreamsViewModel.loadUserDreams(currentUserId);
        
        return view;
    }

    private void initUI(View view) {
        btnRecordDream = view.findViewById(R.id.btn_record_dream);
        btnViewMatchDream = view.findViewById(R.id.btn_view_match_dream);
        tvMatchHint = view.findViewById(R.id.tv_match_hint);
        recyclerViewDreams = view.findViewById(R.id.recyclerView_dreams);
        layoutEmpty = view.findViewById(R.id.layout_empty);
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
    }

    private void setupRecyclerView() {
        dreamAdapter = new DreamAdapter(context, new ArrayList<>(), this);
        recyclerViewDreams.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewDreams.setAdapter(dreamAdapter);
    }

    private void setupButtonListeners() {
        // 记录梦境按钮点击事件
        btnRecordDream.setOnClickListener(v -> showAddDreamDialog());
        
        // 查看匹配对象梦境按钮点击事件
        btnViewMatchDream.setOnClickListener(v -> {
            Toast.makeText(context, "该功能暂未实现", Toast.LENGTH_SHORT).show();
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

    // 显示梦境详情对话框
    private void showDreamDetailDialog(Dream dream) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dream.getTitle())
               .setMessage(dream.getContent() + "\n\n" + 
                          "类型：" + dream.getNature() + "\n" +
                          "时间：" + dream.getCreatedAt() + "\n" +
                          "公开状态：" + (dream.isPublic() ? "已公开" : "私密"))
               .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
               .setNeutralButton("删除", (dialog, which) -> {
                   showDeleteConfirmDialog(dream);
               });
        
        AlertDialog dialog = builder.create();
        dialog.show();
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

    @Override
    public void onDreamClick(Dream dream) {
        // 点击梦境项时显示详情
        showDreamDetailDialog(dream);
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
                           showDreamDetailDialog(dream);
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