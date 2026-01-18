package com.example.dreamisland.ui.mySleep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.example.dreamisland.R;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.dao.BodyStateDao;
import com.example.dreamisland.dao.DreamDao;
import com.example.dreamisland.entity.Dream;
import com.example.dreamisland.dao.BodyStateDao.StateCount;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonutChartFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_CURRENT_MONTH = "current_month";
    private static final String ARG_IS_BODY_DATA = "is_body_data";
    private int userId;
    private String currentMonth;
    private boolean isBodyData;
    private PieChart donutChart;
    private DreamDatabaseHelper dbHelper;
    private BodyStateDao bodyStateDao;
    private DreamDao dreamDao;

    public static DonutChartFragment newInstance(int userId, String currentMonth, boolean isBodyData) {
        DonutChartFragment fragment = new DonutChartFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_CURRENT_MONTH, currentMonth);
        args.putBoolean(ARG_IS_BODY_DATA, isBodyData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(ARG_USER_ID);
            currentMonth = getArguments().getString(ARG_CURRENT_MONTH);
            isBodyData = getArguments().getBoolean(ARG_IS_BODY_DATA, true);
        }
        dbHelper = new DreamDatabaseHelper(requireContext());
        bodyStateDao = new BodyStateDao(dbHelper.getReadableDatabase());
        dreamDao = new DreamDao(dbHelper.getReadableDatabase());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_donut_chart, container, false);
        donutChart = root.findViewById(R.id.donutChart);
        setupDonutChart();
        return root;
    }

    private void setupDonutChart() {
        // 设置图表背景为透明
        donutChart.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));

        // 设置为环形图
        donutChart.setHoleRadius(50f);
        donutChart.setTransparentCircleRadius(55f);

        // 设置图例
        Legend legend = donutChart.getLegend();
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        legend.setTextSize(12f);
        
        // 初始加载数据
        updateChartData();
    }
    
    /**
     * 更新环形图数据，支持实时更新
     */
    public void updateChartData() {
        if (donutChart == null) {
            return;
        }
        
        ArrayList<PieEntry> entries = new ArrayList<>();
        String title = "";
        int[] colors = {};
        
        if (isBodyData) {
            // 显示身体状态数据
            if (bodyStateDao == null) return;
            
            List<StateCount> stateCounts = bodyStateDao.getStateStatistics(userId, currentMonth);

            // 创建环形图数据
            if (stateCounts.isEmpty()) {
                entries.add(new PieEntry(100f, "无数据"));
            } else {
                for (StateCount stateCount : stateCounts) {
                    entries.add(new PieEntry(stateCount.count, stateCount.state));
                }
            }

            title = "身体状态分布";
            colors = new int[]{ContextCompat.getColor(requireContext(), R.color.color_tired),
                    ContextCompat.getColor(requireContext(), R.color.color_general),
                    ContextCompat.getColor(requireContext(), R.color.color_energetic)};
        } else {
            // 显示梦境数据
            if (dreamDao == null) return;
            
            List<Dream> dreams = dreamDao.getDreamsByMonth(userId, currentMonth);

            // 统计不同类型梦境的数量
            Map<String, Integer> dreamTypeCount = new HashMap<>();
            dreamTypeCount.put("好梦", 0);
            dreamTypeCount.put("噩梦", 0);
            dreamTypeCount.put("其他", 0);

            for (Dream dream : dreams) {
                String nature = dream.getNature();
                if (nature != null) {
                    switch (nature) {
                        case "好梦":
                            dreamTypeCount.put("好梦", dreamTypeCount.get("好梦") + 1);
                            break;
                        case "噩梦":
                            dreamTypeCount.put("噩梦", dreamTypeCount.get("噩梦") + 1);
                            break;
                        default:
                            dreamTypeCount.put("其他", dreamTypeCount.get("其他") + 1);
                            break;
                    }
                } else {
                    dreamTypeCount.put("其他", dreamTypeCount.get("其他") + 1);
                }
            }

            // 创建环形图数据
            for (Map.Entry<String, Integer> entry : dreamTypeCount.entrySet()) {
                if (entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
            }

            if (entries.isEmpty()) {
                entries.add(new PieEntry(100f, "无数据"));
            }

            title = "梦境类型分布";
            colors = new int[]{ContextCompat.getColor(requireContext(), R.color.purple_500),
                    ContextCompat.getColor(requireContext(), R.color.color_tired),
                    ContextCompat.getColor(requireContext(), R.color.color_general)};
        }

        PieDataSet dataSet = new PieDataSet(entries, title);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        donutChart.setData(data);
        donutChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        donutChart.setEntryLabelTextSize(12f);

        // 动画更新图表
        donutChart.animateY(500);
        donutChart.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}