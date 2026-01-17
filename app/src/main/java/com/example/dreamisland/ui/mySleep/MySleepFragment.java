package com.example.dreamisland.ui.mySleep;


import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.adapter.BodyStateAdapter;
import com.example.dreamisland.adapter.CalendarAdapter;
import com.example.dreamisland.adapter.HeatMapAdapter;
import com.example.dreamisland.dao.BodyStateDao;
import com.example.dreamisland.dao.DreamDao;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.dialog.StatisticsDialog;
import com.example.dreamisland.entity.BodyState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MySleepFragment extends Fragment {

    private DreamDatabaseHelper dbHelper;
    private BodyStateDao bodyStateDao;
    private DreamDao dreamDao; // 新增
    private RecyclerView rvBodyStates;
    private RecyclerView rvCalendar;
    private BodyStateAdapter bodyStateAdapter;
    private CalendarAdapter calendarAdapter;
    private RecyclerView rvHeatMap;
    private HeatMapAdapter heatMapAdapter;

    private Button btnTired, btnGeneral, btnEnergetic, btnStatistics;

    private static final int CURRENT_USER_ID = 1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat heatMapDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // 热力图专用

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 初始化数据库
        dbHelper = new DreamDatabaseHelper(getContext());
        bodyStateDao = new BodyStateDao(dbHelper.getWritableDatabase());
        dreamDao = new DreamDao(dbHelper.getWritableDatabase()); // 初始化DreamDao

        // 初始化视图
        initViews(root);

        // 加载数据
        loadBodyStates();

        return root;
    }

    private void initViews(View root) {
        rvBodyStates = root.findViewById(R.id.rvBodyStates);
        rvCalendar = root.findViewById(R.id.rvCalendar);
        btnTired = root.findViewById(R.id.btnTired);
        btnGeneral = root.findViewById(R.id.btnGeneral);
        btnEnergetic = root.findViewById(R.id.btnEnergetic);
        btnStatistics = root.findViewById(R.id.btnStatistics);

        // 初始化适配器
        bodyStateAdapter = new BodyStateAdapter(new ArrayList<>());
        rvBodyStates.setAdapter(bodyStateAdapter);

        // 初始化日历适配器
        List<Date> currentMonthDates = getCurrentMonthDates();
        calendarAdapter = new CalendarAdapter(currentMonthDates, new ArrayList<>());
        rvCalendar.setAdapter(calendarAdapter);

        // 设置按钮点击事件
        btnTired.setOnClickListener(v -> recordBodyState("疲惫"));
        btnGeneral.setOnClickListener(v -> recordBodyState("一般"));
        btnEnergetic.setOnClickListener(v -> recordBodyState("精神"));

        btnStatistics.setOnClickListener(v -> showStatistics());

        // 添加热力图
        rvHeatMap = root.findViewById(R.id.rvHeatMap);
        if (rvHeatMap != null) {
            // 创建7列的网格布局（每周7天）
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
            rvHeatMap.setLayoutManager(layoutManager);

            // 初始化热力图适配器
            List<Date> heatMapDates = generateHeatMapDates();
            Map<String, String> dreamData = getDreamDataForHeatMap();
            heatMapAdapter = new HeatMapAdapter(heatMapDates, dreamData);
            rvHeatMap.setAdapter(heatMapAdapter);
        }
    }

    private Map<String, String> getDreamDataForHeatMap() {
        Map<String, String> dreamData = new HashMap<>();

        try {
            if (dreamDao != null) {
                // 从数据库获取梦境数据
                dreamData = dreamDao.getDreamsForHeatMap(CURRENT_USER_ID);
                Log.d(TAG, "Loaded " + dreamData.size() + " dream records for heatmap");

                // 如果没有数据，可以添加一些测试数据
                if (dreamData.isEmpty()) {
                    addTestDreamDataForHeatMap();
                    // 重新获取
                    dreamData = dreamDao.getDreamsForHeatMap(CURRENT_USER_ID);
                }
            } else {
                Log.e(TAG, "dreamDao is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting dream data for heatmap: " + e.getMessage(), e);
            // 如果出错，返回空Map而不是null
        }

        return dreamData;
    }

    private void addTestDreamDataForHeatMap() {
        try {
            // 添加一些测试梦境数据，用于演示热力图
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat testDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            // 添加最近几天的测试数据
            for (int i = 0; i < 30; i++) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                String date = dateFormat.format(calendar.getTime());

                // 随机选择梦境性质
                String nature;
                int random = (int) (Math.random() * 3);
                switch (random) {
                    case 0:
                        nature = "好梦";
                        break;
                    case 1:
                        nature = "噩梦";
                        break;
                    default:
                        nature = "其他";
                        break;
                }

                // 创建测试梦境（这里只是示例，实际应该插入数据库）
                // 注意：这里需要实际调用数据库插入方法
                Log.d(TAG, "Test dream data: " + date + " - " + nature);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error adding test dream data: " + e.getMessage(), e);
        }
    }

    private List<Date> generateHeatMapDates() {
        List<Date> dates = new ArrayList<>();
        try {
            Calendar calendar = Calendar.getInstance();

            // 设置到今天
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 计算应该显示多少周（通常是最近一年的周数，但我们可以显示最近12周）
            int totalWeeks = 12;
            int totalDays = totalWeeks * 7; // 12周 * 7天 = 84天

            // 计算开始日期（从今天往前推totalDays-1天）
            calendar.add(Calendar.DAY_OF_MONTH, -(totalDays - 1));

            // 添加日期（最近12周）
            for (int i = 0; i < totalDays; i++) {
                Date date = calendar.getTime();
                dates.add(date);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            Log.d(TAG, "Generated " + dates.size() + " dates for heatmap");

        } catch (Exception e) {
            Log.e(TAG, "Error generating heatmap dates: " + e.getMessage(), e);
        }

        return dates;
    }

    private void loadBodyStates() {
        try {
            // 从数据库加载所有记录
            List<BodyState> bodyStates = bodyStateDao.getBodyStatesByUserId(CURRENT_USER_ID);
            bodyStateAdapter.updateData(bodyStates);

            // 加载当前月份的数据用于日历
            String currentMonth = monthFormat.format(new Date());
            List<BodyState> monthlyStates = bodyStateDao.getBodyStatesByMonth(CURRENT_USER_ID, currentMonth);
            List<Date> monthDates = getCurrentMonthDates();
            calendarAdapter.updateData(monthDates, monthlyStates);

            // 刷新热力图数据
            if (heatMapAdapter != null) {
                Map<String, String> dreamData = getDreamDataForHeatMap();
                List<Date> heatMapDates = generateHeatMapDates();
                heatMapAdapter.updateData(heatMapDates, dreamData);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in loadBodyStates: " + e.getMessage(), e);
        }
    }

    private void recordBodyState(String state) {
        try {
            String today = dateFormat.format(new Date());

            // 检查今天是否已记录
            if (bodyStateDao.isAlreadyRecordedToday(CURRENT_USER_ID)) {
                Toast.makeText(getContext(), "今天已记录过身体状态", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建新的记录
            BodyState bodyState = new BodyState(CURRENT_USER_ID, state, today);

            // 插入数据库
            long result = bodyStateDao.insert(bodyState);

            if (result != -1) {
                Toast.makeText(getContext(), "记录成功：" + state, Toast.LENGTH_SHORT).show();
                loadBodyStates(); // 刷新数据
            } else {
                Toast.makeText(getContext(), "记录失败", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in recordBodyState: " + e.getMessage(), e);
            Toast.makeText(getContext(), "记录出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatistics() {
        try {
            String currentMonth = monthFormat.format(new Date());
            List<BodyState> monthlyStates = bodyStateDao.getBodyStatesByMonth(CURRENT_USER_ID, currentMonth);

            if (monthlyStates == null || monthlyStates.isEmpty()) {
                Toast.makeText(getContext(), "本月暂无记录", Toast.LENGTH_SHORT).show();
                return;
            }

            // 计算统计数据
            int tiredCount = 0, energeticCount = 0, generalCount = 0;
            for (BodyState state : monthlyStates) {
                if (state != null && state.getState() != null) {
                    switch (state.getState()) {
                        case "疲惫":
                            tiredCount++;
                            break;
                        case "精神":
                            energeticCount++;
                            break;
                        case "一般":
                            generalCount++;
                            break;
                    }
                }
            }

            // 检查健康提示
            String healthTip = "";
            int consecutiveTired = bodyStateDao.checkConsecutiveTiredDays(CURRENT_USER_ID, 3);
            if (consecutiveTired >= 3) {
                healthTip = "您近期醒来后常感疲惫，建议调整入睡环境或咨询医生。";
            }

            // 检查一周内梦境次数
            if (dreamDao != null) {
                int dreamCount = dreamDao.getDreamCountInWeek(CURRENT_USER_ID);
                if (dreamCount >= 5) {
                    if (!healthTip.isEmpty()) {
                        healthTip += "\n";
                    }
                    healthTip += "您近期梦境频繁，建议保持规律作息。";
                }
            }

            // 创建对话框
            StatisticsDialog dialog = StatisticsDialog.newInstance(
                    tiredCount, energeticCount, generalCount, healthTip);

            dialog.show(getParentFragmentManager(), "StatisticsDialog");

        } catch (Exception e) {
            Log.e(TAG, "Error in showStatistics: " + e.getMessage(), e);
            Toast.makeText(getContext(), "显示统计出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<Date> getCurrentMonthDates() {
        List<Date> dates = new ArrayList<>();
        try {
            Calendar calendar = Calendar.getInstance();

            // 获取当前月份信息
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            // 创建当前月份第一天的日历
            Calendar firstDay = Calendar.getInstance();
            firstDay.set(Calendar.YEAR, currentYear);
            firstDay.set(Calendar.MONTH, currentMonth);
            firstDay.set(Calendar.DAY_OF_MONTH, 1);
            firstDay.set(Calendar.HOUR_OF_DAY, 0);
            firstDay.set(Calendar.MINUTE, 0);
            firstDay.set(Calendar.SECOND, 0);
            firstDay.set(Calendar.MILLISECOND, 0);

            // 获取第一天是星期几（1=周日，2=周一，...，7=周六）
            int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);

            // 计算需要添加的空白天数
            // 我们需要将周日作为一周的第一天
            int emptyDays = firstDayOfWeek - 1;

            // 添加前面的空白天数（null）
            for (int i = 0; i < emptyDays; i++) {
                dates.add(null);
            }

            // 获取当前月份的天数
            int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

            // 添加当前月份的日期
            for (int i = 1; i <= daysInMonth; i++) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.set(Calendar.YEAR, currentYear);
                dayCal.set(Calendar.MONTH, currentMonth);
                dayCal.set(Calendar.DAY_OF_MONTH, i);
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);
                dates.add(dayCal.getTime());
            }

            Log.d(TAG, "Generated " + dates.size() + " dates for calendar");

        } catch (Exception e) {
            Log.e(TAG, "Error in getCurrentMonthDates: " + e.getMessage(), e);
        }
        return dates;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}