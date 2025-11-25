package com.example.dreamisland.ui.myDreams;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        
        AlertDialog dialog = builder.create();
        
        // 取消按钮点击事件
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
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
            dream.setTags("[]"); // 默认空标签
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