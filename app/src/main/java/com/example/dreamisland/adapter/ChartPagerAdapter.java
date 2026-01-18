package com.example.dreamisland.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.dreamisland.ui.mySleep.PieChartFragment;
import com.example.dreamisland.ui.mySleep.BarChartFragment;
import com.example.dreamisland.ui.mySleep.DonutChartFragment;

import java.util.ArrayList;
import java.util.List;

public class ChartPagerAdapter extends FragmentStateAdapter {

    private List<Fragment> fragments;
    private int userId;
    private String currentMonth;
    private boolean isBodyData; // true: 身体状态数据, false: 梦境数据

    public ChartPagerAdapter(@NonNull FragmentActivity fragmentActivity, int userId, String currentMonth, boolean isBodyData) {
        super(fragmentActivity);
        this.userId = userId;
        this.currentMonth = currentMonth;
        this.isBodyData = isBodyData;
        initializeFragments();
    }

    private void initializeFragments() {
        fragments = new ArrayList<>();
        fragments.add(PieChartFragment.newInstance(userId, currentMonth, isBodyData));
        fragments.add(BarChartFragment.newInstance(userId, currentMonth, isBodyData));
        fragments.add(DonutChartFragment.newInstance(userId, currentMonth, isBodyData));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
    
    /**
     * 获取指定位置的Fragment实例
     */
    public Fragment getFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            return fragments.get(position);
        }
        return null;
    }
    
    /**
     * 更新数据类型并重新初始化Fragments
     */
    public void updateDataType(boolean isBodyData) {
        this.isBodyData = isBodyData;
        initializeFragments();
        notifyDataSetChanged();
    }
}