// HeatMapAdapter.java
package com.example.dreamisland.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HeatMapAdapter extends RecyclerView.Adapter<HeatMapAdapter.ViewHolder> {

    private List<Date> dates;
    private Map<String, String> dreamNatureMap; // date -> nature
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public HeatMapAdapter(List<Date> dates, Map<String, String> dreamNatureMap) {
        this.dates = dates != null ? dates : new ArrayList<>();
        this.dreamNatureMap = dreamNatureMap != null ? dreamNatureMap : new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_heatmap_cell, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = dates.get(position);

        if (date == null) {
            holder.cellView.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        String dateStr = dateFormat.format(date);
        String nature = dreamNatureMap.get(dateStr);

        // 设置颜色
        if (nature != null) {
            switch (nature) {
                case "好梦":
                    holder.cellView.setBackgroundColor(Color.parseColor("#40C463")); // 绿色
                    break;
                case "噩梦":
                    holder.cellView.setBackgroundColor(Color.parseColor("#EBEDF0")); // 红色
                    break;
                case "其他":
                    holder.cellView.setBackgroundColor(Color.parseColor("#9BE9A8")); // 浅绿色
                    break;
                default:
                    holder.cellView.setBackgroundColor(Color.parseColor("#EBEDF0")); // 灰色
                    break;
            }
        } else {
            // 无记录
            holder.cellView.setBackgroundColor(Color.parseColor("#EBEDF0")); // 浅灰色
        }

        // 添加点击事件
        holder.cellView.setOnClickListener(v -> {
            if (date != null) {
                // 显示当天的梦境详情
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View cellView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cellView = itemView.findViewById(R.id.heatmapCell);
        }
    }
    // 在 HeatMapAdapter 类中添加
    public void updateData(List<Date> dates, Map<String, String> dreamNatureMap) {
        this.dates = dates != null ? dates : new ArrayList<>();
        this.dreamNatureMap = dreamNatureMap != null ? dreamNatureMap : new HashMap<>();
        notifyDataSetChanged();
    }
}