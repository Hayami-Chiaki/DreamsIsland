// StatisticsDialog.java - 兼容两种调用方式
package com.example.dreamisland.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dreamisland.R;
import com.example.dreamisland.dao.BodyStateDao;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.entity.BodyState;

import java.util.List;

public class StatisticsDialog extends DialogFragment {

    private static final String TAG = "StatisticsDialog";

    // 统计数据和健康提示
    private int tiredCount = 0;
    private int energeticCount = 0;
    private int generalCount = 0;
    private String healthTip = "";

    // ============== 新的调用方式 ==============
    public static StatisticsDialog newInstance(int tiredCount, int energeticCount,
                                               int generalCount, String healthTip) {
        StatisticsDialog dialog = new StatisticsDialog();
        Bundle args = new Bundle();
        args.putInt("tiredCount", tiredCount);
        args.putInt("energeticCount", energeticCount);
        args.putInt("generalCount", generalCount);
        args.putString("healthTip", healthTip);
        dialog.setArguments(args);
        return dialog;
    }

    // ============== 旧的构造函数（兼容） ==============
    // 注意：这个方法在 MySleepFragment 中没有被调用，可以保留或删除
    public StatisticsDialog(List<BodyState> monthlyStates, BodyStateDao bodyStateDao) {
        if (monthlyStates != null) {
            for (BodyState state : monthlyStates) {
                if (state != null && state.getState() != null) {
                    switch (state.getState()) {
                        case "疲惫": tiredCount++; break;
                        case "精神": energeticCount++; break;
                        case "一般": generalCount++; break;
                    }
                }
            }
        }

        // 这里需要访问数据库来检查健康提示
        // 但由于在构造函数中无法获取 Context，所以这种方式有限制
    }

    // Fragment 需要的无参构造函数
    public StatisticsDialog() {
        // 空的构造函数
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 从 Bundle 中获取参数
        Bundle args = getArguments();
        if (args != null) {
            tiredCount = args.getInt("tiredCount", 0);
            energeticCount = args.getInt("energeticCount", 0);
            generalCount = args.getInt("generalCount", 0);
            healthTip = args.getString("healthTip", "");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_statistics, null);

        try {
            // 初始化视图
            TextView tvTired = view.findViewById(R.id.tvTiredCount);
            TextView tvEnergetic = view.findViewById(R.id.tvEnergeticCount);
            TextView tvGeneral = view.findViewById(R.id.tvGeneralCount);
            TextView tvHealthTip = view.findViewById(R.id.tvHealthTip);
            TextView tvTotal = view.findViewById(R.id.tvTotalCount);

            // 设置统计数据
            tvTired.setText(String.valueOf(tiredCount));
            tvEnergetic.setText(String.valueOf(energeticCount));
            tvGeneral.setText(String.valueOf(generalCount));

            // 计算并显示总记录数
            int total = tiredCount + energeticCount + generalCount;
            if (tvTotal != null) {
                tvTotal.setText("总记录: " + total + " 天");
            }

            // 设置健康提示
            if (healthTip != null && !healthTip.isEmpty()) {
                tvHealthTip.setText(healthTip);
                tvHealthTip.setVisibility(View.VISIBLE);
            } else {
                tvHealthTip.setVisibility(View.GONE);
            }

            // 设置按钮点击事件
            View btnConfirm = view.findViewById(R.id.btnConfirm);
            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> dismiss());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // 设置对话框背景透明，以便显示布局文件中的圆角背景
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}