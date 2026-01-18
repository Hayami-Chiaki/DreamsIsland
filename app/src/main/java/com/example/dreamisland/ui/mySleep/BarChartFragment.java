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
import com.example.dreamisland.dao.BodyStateDao.StateDatePair;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.example.dreamisland.dao.DreamDao;
import com.example.dreamisland.entity.Dream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BarChartFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_CURRENT_MONTH = "current_month";
    private static final String ARG_IS_BODY_DATA = "is_body_data";
    private int userId;
    private String currentMonth;
    private boolean isBodyData;
    private BarChart barChart;
    private DreamDatabaseHelper dbHelper;
    private BodyStateDao bodyStateDao;
    private DreamDao dreamDao;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static BarChartFragment newInstance(int userId, String currentMonth, boolean isBodyData) {
        BarChartFragment fragment = new BarChartFragment();
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
        View root = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        barChart = root.findViewById(R.id.barChart);
        setupBarChart();
        return root;
    }

    private void setupBarChart() {
        // 设置图表背景为透明
        barChart.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));

        // 设置X轴
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);

        // 设置Y轴
        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        yAxisLeft.setTextSize(12f);
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setAxisMaximum(100f);

        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        yAxisRight.setTextSize(12f);
        yAxisRight.setAxisMinimum(0f);
        yAxisRight.setAxisMaximum(100f);

        // 设置图例
        Legend legend = barChart.getLegend();
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        legend.setTextSize(12f);
        
        // 初始加载数据
        updateChartData();
    }
    
    /**
     * 更新条形图数据，支持实时更新
     */
    public void updateChartData() {
        if (barChart == null) {
            return;
        }
        
        // 获取最近7天的数据
        Calendar calendar = Calendar.getInstance();
        String endDate = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        String startDate = dateFormat.format(calendar.getTime());

        // 初始化7天的数据
        float[] dailyScores = new float[7];
        String[] labels = new String[7];
        String chartTitle = "";

        if (isBodyData) {
            // 显示身体状态数据
            if (bodyStateDao == null) return;
            
            List<StateDatePair> statesInRange = bodyStateDao.getStatesInDateRange(userId, startDate, endDate);

            // 将状态转换为数值评分
            Map<String, Float> stateScores = new HashMap<>();
            stateScores.put("疲惫", 30f);
            stateScores.put("一般", 60f);
            stateScores.put("精神", 90f);
            stateScores.put("精力充沛", 90f);

            // 填充数据
            for (int i = 0; i < 7; i++) {
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, -6 + i);
                String date = dateFormat.format(calendar.getTime());
                labels[i] = getDayOfWeekLabel(calendar.get(Calendar.DAY_OF_WEEK));

                // 查找该日期的状态
                for (StateDatePair pair : statesInRange) {
                    if (pair.date.equals(date)) {
                        dailyScores[i] = stateScores.getOrDefault(pair.state, 50f);
                        break;
                    }
                }
            }
            
            chartTitle = "身体状态评分";
        } else {
            // 显示梦境数据
            if (dreamDao == null) return;
            
            List<Dream> dreamsInRange = dreamDao.getDreamsInDateRange(userId, startDate, endDate);

            // 将梦境类型转换为数值评分
            Map<String, Float> dreamScores = new HashMap<>();
            dreamScores.put("好梦", 80f);
            dreamScores.put("噩梦", 20f);
            dreamScores.put("其他", 50f);

            // 填充数据
            for (int i = 0; i < 7; i++) {
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, -6 + i);
                String date = dateFormat.format(calendar.getTime());
                labels[i] = getDayOfWeekLabel(calendar.get(Calendar.DAY_OF_WEEK));

                // 查找该日期的梦境
                for (Dream dream : dreamsInRange) {
                    if (dream.getCreatedAt().equals(date)) {
                        String nature = dream.getNature();
                        dailyScores[i] = dreamScores.getOrDefault(nature, 50f);
                        break;
                    }
                }
            }
            
            chartTitle = "梦境质量评分";
        }

        // 创建条形图数据
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dailyScores.length; i++) {
            entries.add(new BarEntry(i, dailyScores[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, chartTitle);
        dataSet.setColors(new int[]{ContextCompat.getColor(requireContext(), R.color.purple_500)});
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        
        // 更新X轴标签
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        // 动画更新图表
        barChart.animateY(500);
        barChart.invalidate();
    }

    // 获取星期几标签
    private String getDayOfWeekLabel(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "周日";
            case Calendar.MONDAY:
                return "周一";
            case Calendar.TUESDAY:
                return "周二";
            case Calendar.WEDNESDAY:
                return "周三";
            case Calendar.THURSDAY:
                return "周四";
            case Calendar.FRIDAY:
                return "周五";
            case Calendar.SATURDAY:
                return "周六";
            default:
                return "";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}