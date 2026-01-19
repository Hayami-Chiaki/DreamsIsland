package com.example.dreamisland.ui.mySleep;


import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.example.dreamisland.ui.myDreams.MyDreamDetailActivity;

import com.example.dreamisland.R;
import com.example.dreamisland.adapter.BodyStateAdapter;
import com.example.dreamisland.adapter.CalendarAdapter;
import com.example.dreamisland.adapter.HeatMapAdapter;
import com.example.dreamisland.adapter.ChartPagerAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.dreamisland.dao.BodyStateDao;
import com.example.dreamisland.dao.DreamDao;
import com.example.dreamisland.database.DreamDatabaseHelper;
import com.example.dreamisland.dialog.StatisticsDialog;
import com.example.dreamisland.entity.BodyState;
import com.example.dreamisland.entity.Dream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.Context;

public class MySleepFragment extends Fragment {

    private DreamDatabaseHelper dbHelper;
    private BodyStateDao bodyStateDao;
    private DreamDao dreamDao; // 新增
    private RecyclerView rvCalendar;
    private CalendarAdapter calendarAdapter;
    private Button btnTired, btnGeneral, btnEnergetic;
    private Button btnBodyStatus, btnDreamStatus;
    private ImageButton btnPrevMonth, btnNextMonth;
    private TextView tvCurrentDate;
    private View vToggleBackground;
    
    // 图表相关变量
    private ViewPager2 viewPagerCharts;
    private ChartPagerAdapter chartPagerAdapter;
    private Button btnPieChart, btnBarChart, btnDonutChart;
    private View vChartToggleBackground;

    // 当前显示的年份和月份
    private int currentDisplayYear;
    private int currentDisplayMonth;
    private boolean isBodyStatus = true;
    
    private int currentUserId; // 从登录信息获取
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private SimpleDateFormat heatMapDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // 热力图专用
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy年MM月", Locale.getDefault()); // 用于显示年月

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 获取当前登录用户ID
        SharedPreferences sp = requireActivity().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentUserId = sp.getInt("logged_in_user_id", 1);

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
        rvCalendar = root.findViewById(R.id.rvCalendar);
        btnTired = root.findViewById(R.id.btnTired);
        btnGeneral = root.findViewById(R.id.btnGeneral);
        btnEnergetic = root.findViewById(R.id.btnEnergetic);
        btnBodyStatus = root.findViewById(R.id.btnBodyStatus);
        btnDreamStatus = root.findViewById(R.id.btnDreamStatus);
        btnPrevMonth = root.findViewById(R.id.btnPrevMonth);
        btnNextMonth = root.findViewById(R.id.btnNextMonth);
        tvCurrentDate = root.findViewById(R.id.tvCurrentDate);
        vToggleBackground = root.findViewById(R.id.vToggleBackground);

        // 初始化当前显示的年月
        Calendar currentCalendar = Calendar.getInstance();
        currentDisplayYear = currentCalendar.get(Calendar.YEAR);
        currentDisplayMonth = currentCalendar.get(Calendar.MONTH);

        // 初始化日历适配器
        List<Date> currentMonthDates = getMonthDates(currentDisplayYear, currentDisplayMonth);
        calendarAdapter = new CalendarAdapter(currentMonthDates, new ArrayList<>());
        rvCalendar.setAdapter(calendarAdapter);

        // 设置日历日期点击监听器
        calendarAdapter.setOnDateClickListener(this::onDateClick);

        // 更新显示的年月
        updateDisplayDate();

        // 设置按钮点击事件
        btnTired.setOnClickListener(v -> recordBodyState("疲惫"));
        btnGeneral.setOnClickListener(v -> recordBodyState("一般"));
        btnEnergetic.setOnClickListener(v -> recordBodyState("精神"));
        
        // 设置日期点击显示统计
        tvCurrentDate.setOnClickListener(v -> showStatistics());
        
        // 月份切换按钮
        btnPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        btnNextMonth.setOnClickListener(v -> navigateMonth(1));
        
        // 状态切换按钮
        btnBodyStatus.setOnClickListener(v -> {
            if (!isBodyStatus) {
                switchToBodyStatus();
            }
        });
        
        btnDreamStatus.setOnClickListener(v -> {
            if (isBodyStatus) {
                switchToDreamStatus();
            }
        });
        
        // 默认选择身体状态
        btnBodyStatus.setSelected(true);
        btnDreamStatus.setSelected(false);
        animateToggleBackground();
        
        // 初始化图表
        initCharts(root);
        
        // 设置图表数据
        setupCharts();

        // 检查做梦频率是否过高并提示
        checkDreamFrequency();
    }

    /**
     * 检查最近一周做梦频率，如果过高则提示用户
     */
    private void checkDreamFrequency() {
        if (dreamDao != null) {
            new Thread(() -> {
                int dreamCount = dreamDao.getDreamCountInWeek(currentUserId);
                if (dreamCount >= 5) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "您近期梦境频繁（一周内" + dreamCount + "次），建议保持规律作息，注意休息哦。", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }).start();
        }
    }

    private Map<String, String> getDreamDataForHeatMap() {
        Map<String, String> dreamData = new HashMap<>();

        try {
            if (dreamDao != null) {
                // 从数据库获取梦境数据
                dreamData = dreamDao.getDreamsForHeatMap(currentUserId);
                Log.d(TAG, "Loaded " + dreamData.size() + " dream records for heatmap");

                // 如果没有数据，可以添加一些测试数据
                if (dreamData.isEmpty()) {
                    addTestDreamDataForHeatMap();
                    // 重新获取
                    dreamData = dreamDao.getDreamsForHeatMap(currentUserId);
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
            // 加载当前月份的数据用于日历
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, currentDisplayYear);
            calendar.set(Calendar.MONTH, currentDisplayMonth);
            String currentMonth = monthFormat.format(calendar.getTime());
            
            List<BodyState> monthlyStates = bodyStateDao.getBodyStatesByMonth(currentUserId, currentMonth);
            List<Date> monthDates = getMonthDates(currentDisplayYear, currentDisplayMonth);
            calendarAdapter.updateData(monthDates, monthlyStates);


        } catch (Exception e) {
            Log.e(TAG, "Error in loadBodyStates: " + e.getMessage(), e);
        }
    }

    private void recordBodyState(String state) {
        try {
            String today = dateFormat.format(new Date());

            // 检查今天是否已记录
            BodyState existingState = bodyStateDao.getBodyStateByDate(currentUserId, today);
            
            long result;
            if (existingState != null) {
                // 如果已存在记录，更新状态
                existingState.setState(state);
                result = bodyStateDao.update(existingState);
                if (result != -1) {
                    Toast.makeText(getContext(), "状态已更新为：" + state, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 如果不存在记录，创建新记录
                BodyState bodyState = new BodyState(currentUserId, state, today);
                result = bodyStateDao.insert(bodyState);
                if (result != -1) {
                    Toast.makeText(getContext(), "记录成功：" + state, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "记录失败", Toast.LENGTH_SHORT).show();
                }
            }

            loadBodyStates(); // 刷新数据

        } catch (Exception e) {
            Log.e(TAG, "Error in recordBodyState: " + e.getMessage(), e);
            Toast.makeText(getContext(), "操作出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void showStatistics() {
        try {
            String currentMonth = monthFormat.format(new Date());
            List<BodyState> monthlyStates = bodyStateDao.getBodyStatesByMonth(currentUserId, currentMonth);

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
            int consecutiveTired = bodyStateDao.checkConsecutiveTiredDays(currentUserId, 3);
            if (consecutiveTired >= 3) {
                healthTip = "您近期醒来后常感疲惫，建议调整入睡环境或咨询医生。";
            }

            // 检查一周内梦境次数
            if (dreamDao != null) {
                int dreamCount = dreamDao.getDreamCountInWeek(currentUserId);
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

    /**
     * 获取指定年月的日期列表
     */
    private List<Date> getMonthDates(int year, int month) {
        List<Date> dates = new ArrayList<>();
        try {
            // 创建指定月份第一天的日历
            Calendar firstDay = Calendar.getInstance();
            firstDay.set(Calendar.YEAR, year);
            firstDay.set(Calendar.MONTH, month);

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
                dayCal.set(Calendar.YEAR, year);
                dayCal.set(Calendar.MONTH, month);

                dayCal.set(Calendar.DAY_OF_MONTH, i);
                dayCal.set(Calendar.HOUR_OF_DAY, 0);
                dayCal.set(Calendar.MINUTE, 0);
                dayCal.set(Calendar.SECOND, 0);
                dayCal.set(Calendar.MILLISECOND, 0);
                dates.add(dayCal.getTime());
            }

            Log.d(TAG, "Generated " + dates.size() + " dates for calendar");

        } catch (Exception e) {
            Log.e(TAG, "Error in getMonthDates: " + e.getMessage(), e);
        }
        return dates;
    }
    
    /**
     * 更新显示的年月
     */
    private void updateDisplayDate() {
        Calendar displayCalendar = Calendar.getInstance();
        displayCalendar.set(Calendar.YEAR, currentDisplayYear);
        displayCalendar.set(Calendar.MONTH, currentDisplayMonth);
        tvCurrentDate.setText(displayDateFormat.format(displayCalendar.getTime()));
    }
    
    /**
     * 月份导航
     * @param direction -1: 上一个月, 1: 下一个月
     */
    private void navigateMonth(int direction) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, currentDisplayYear);
        calendar.set(Calendar.MONTH, currentDisplayMonth);
        calendar.add(Calendar.MONTH, direction);
        
        currentDisplayYear = calendar.get(Calendar.YEAR);
        currentDisplayMonth = calendar.get(Calendar.MONTH);
        
        // 加载新月份的数据
        loadMonthData();
        
        // 更新显示的年月
        updateDisplayDate();
    }
    
    /**
     * 加载指定月份的数据
     */
    private void loadMonthData() {
        try {
            // 格式化月份
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, currentDisplayYear);
            calendar.set(Calendar.MONTH, currentDisplayMonth);
            String month = monthFormat.format(calendar.getTime());
            List<Date> monthDates = getMonthDates(currentDisplayYear, currentDisplayMonth);
            
            if (isBodyStatus) {
                // 加载身体状态数据
                List<BodyState> monthlyStates = bodyStateDao.getBodyStatesByMonth(currentUserId, month);
                calendarAdapter.updateData(monthDates, monthlyStates);
            } else {
                // 加载梦境状态数据
                List<Dream> monthlyDreams = dreamDao.getDreamsByMonth(currentUserId, month);
                calendarAdapter.updateDreamData(monthDates, monthlyDreams);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in loadMonthData: " + e.getMessage(), e);
        }
    }
    
    /**
     * 切换到身体状态
     */
    private void switchToBodyStatus() {
        isBodyStatus = true;
        btnBodyStatus.setSelected(true);
        btnDreamStatus.setSelected(false);
        animateToggleBackground();
        // 告诉适配器当前是身体状态
        calendarAdapter.setBodyStatus(true);
        // 加载身体状态数据
        loadMonthData();
        // 更新图表数据类型
        if (chartPagerAdapter != null) {
            chartPagerAdapter.updateDataType(true);
            setupCharts();
        }
    }
    
    /**
     * 切换到梦境状态
     */
    private void switchToDreamStatus() {
        isBodyStatus = false;
        btnBodyStatus.setSelected(false);
        btnDreamStatus.setSelected(true);
        animateToggleBackground();
        // 告诉适配器当前是梦境状态
        calendarAdapter.setBodyStatus(false);
        // 加载梦境状态数据
        loadMonthData();
        // 更新图表数据类型
        if (chartPagerAdapter != null) {
            chartPagerAdapter.updateDataType(false);
            setupCharts();
        }
    }
    
    /**
     * 动画切换背景位置
     */
    private void animateToggleBackground() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) vToggleBackground.getLayoutParams();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) vToggleBackground.getParent());
        
        if (isBodyStatus) {
            // 切换到身体状态，背景在左边
            constraintSet.clear(vToggleBackground.getId(), ConstraintSet.END);
            constraintSet.connect(vToggleBackground.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        } else {
            // 切换到梦境状态，背景在右边
            constraintSet.clear(vToggleBackground.getId(), ConstraintSet.START);
            constraintSet.connect(vToggleBackground.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        }
        
        // 应用动画
        TransitionManager.beginDelayedTransition((ConstraintLayout) vToggleBackground.getParent(), 
                new ChangeBounds().setDuration(300).setInterpolator(new FastOutSlowInInterpolator()));
        constraintSet.applyTo((ConstraintLayout) vToggleBackground.getParent());
    }
    
    // 初始化图表
    private void initCharts(View root) {
        // 初始化图表切换按钮和滑动背景
        btnPieChart = root.findViewById(R.id.btnPieChart);
        btnBarChart = root.findViewById(R.id.btnBarChart);
        btnDonutChart = root.findViewById(R.id.btnDonutChart);
        vChartToggleBackground = root.findViewById(R.id.vChartToggleBackground);
        
        // 初始化ViewPager2
        viewPagerCharts = root.findViewById(R.id.viewPagerCharts);
        
        // 创建图表适配器，并传递用户ID、当前月份信息和数据类型
        int userId = 1; // 假设当前用户ID为1，实际应用中应该从用户登录信息获取
        String currentMonth = String.format(Locale.getDefault(), "%04d-%02d", currentDisplayYear, currentDisplayMonth + 1);
        chartPagerAdapter = new ChartPagerAdapter(getActivity(), userId, currentMonth, isBodyStatus);
        viewPagerCharts.setAdapter(chartPagerAdapter);
        
        // 设置图表切换监听
        setupChartToggleListeners();
        
        // 默认选择第一个图表
        btnPieChart.setSelected(true);
        btnBarChart.setSelected(false);
        btnDonutChart.setSelected(false);
    }
    
    // 设置图表数据
    private void setupCharts() {
        // 图表数据在各自的Fragment中设置
    }
    
    // 设置图表切换监听
    private void setupChartToggleListeners() {
        // 饼状图按钮点击事件
        btnPieChart.setOnClickListener(v -> {
            viewPagerCharts.setCurrentItem(0, true);
            updateChartButtonSelection(0);
        });
        
        // 条形图按钮点击事件
        btnBarChart.setOnClickListener(v -> {
            viewPagerCharts.setCurrentItem(1, true);
            updateChartButtonSelection(1);
        });
        
        // 环形图按钮点击事件
        btnDonutChart.setOnClickListener(v -> {
            viewPagerCharts.setCurrentItem(2, true);
            updateChartButtonSelection(2);
        });
        
        // ViewPager页面切换监听
        viewPagerCharts.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateChartButtonSelection(position);
            }
        });
    }
    
    // 更新图表按钮选中状态
    private void updateChartButtonSelection(int selectedPosition) {
        // 更新按钮选中状态
        btnPieChart.setSelected(selectedPosition == 0);
        btnBarChart.setSelected(selectedPosition == 1);
        btnDonutChart.setSelected(selectedPosition == 2);
        
        // 动画切换背景位置
        animateChartToggleBackground(selectedPosition);
    }
    
    /**
     * 动画切换图表按钮背景位置
     */
    private void animateChartToggleBackground(int position) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) vChartToggleBackground.getParent());
        
        // 清除之前的约束
        constraintSet.clear(vChartToggleBackground.getId(), ConstraintSet.START);
        constraintSet.clear(vChartToggleBackground.getId(), ConstraintSet.END);
        
        switch (position) {
            case 0: // 饼状图
                constraintSet.connect(vChartToggleBackground.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                break;
            case 1: // 条形图
                constraintSet.connect(vChartToggleBackground.getId(), ConstraintSet.START, R.id.btnBarChart, ConstraintSet.START);
                break;
            case 2: // 环形图
                constraintSet.connect(vChartToggleBackground.getId(), ConstraintSet.START, R.id.btnDonutChart, ConstraintSet.START);
                break;
        }
        
        // 应用动画
        TransitionManager.beginDelayedTransition((ConstraintLayout) vChartToggleBackground.getParent(), 
                new ChangeBounds().setDuration(300).setInterpolator(new FastOutSlowInInterpolator()));
        constraintSet.applyTo((ConstraintLayout) vChartToggleBackground.getParent());
    }

    // 定期更新图表数据相关
    private Handler updateHandler;
    private Runnable updateRunnable;
    private static final long UPDATE_INTERVAL = 5000; // 5秒更新一次

    @Override
    public void onResume() {
        super.onResume();
        // 启动定期更新
        startChartUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止定期更新
        stopChartUpdates();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopChartUpdates(); // 确保停止更新
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * 启动图表定期更新
     */
    private void startChartUpdates() {
        updateHandler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateAllCharts();
                updateHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
    }

    /**
     * 停止图表定期更新
     */
    private void stopChartUpdates() {
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
            updateHandler = null;
            updateRunnable = null;
        }
    }

    /**
     * 更新所有图表数据
     */
    private void updateAllCharts() {
        // 更新当前显示的图表
        int currentPosition = viewPagerCharts.getCurrentItem();
        Fragment currentFragment = chartPagerAdapter.getFragment(currentPosition);
        
        if (currentFragment != null) {
            if (currentFragment instanceof PieChartFragment) {
                ((PieChartFragment) currentFragment).updateChartData();
            } else if (currentFragment instanceof BarChartFragment) {
                ((BarChartFragment) currentFragment).updateChartData();
            } else if (currentFragment instanceof DonutChartFragment) {
                ((DonutChartFragment) currentFragment).updateChartData();
            }
        }
    }

    /**
     * 处理日期点击事件
     */
    private void onDateClick(Date date) {
        try {
            // 格式化日期为yyyy-MM-dd
            String dateStr = dateFormat.format(date);
            
            // 获取该日期的所有梦境记录
            List<Dream> dreams = dreamDao.getDreamsByDateRange(currentUserId, dateStr, dateStr);
            
            if (dreams != null && !dreams.isEmpty()) {
                // 如果有多个梦境，显示第一个（实际应用中可以显示列表选择）
                Dream dream = dreams.get(0);
                
                // 打开梦境详情页面
                Intent intent = new Intent(getActivity(), MyDreamDetailActivity.class);
                intent.putExtra("dreamId", dream.getDreamId());
                intent.putExtra("userId", currentUserId);
                intent.putExtra("title", dream.getTitle());
                intent.putExtra("content", dream.getContent());
                intent.putExtra("nature", dream.getNature());
                intent.putExtra("createdAt", dream.getCreatedAt());
                intent.putExtra("isPublic", dream.getIsPublic() == 1);
                intent.putExtra("isFavorite", dream.getIsFavorite() == 1);
                intent.putExtra("tags", dream.getTags());
                startActivity(intent);
            } else {
                // 没有梦境记录
                Toast.makeText(getContext(), "该日期没有梦境记录", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling date click: " + e.getMessage(), e);
            Toast.makeText(getContext(), "获取梦境记录失败", Toast.LENGTH_SHORT).show();
        }

    }
}