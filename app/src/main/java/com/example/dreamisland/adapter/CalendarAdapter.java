package com.example.dreamisland.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.entity.BodyState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private static final String TAG = "CalendarAdapter";

    private List<Date> dates;
    private Map<String, String> stateMap; // date -> state
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public CalendarAdapter(List<Date> dates, List<BodyState> bodyStates) {
        // 确保dates不为null
        this.dates = dates != null ? dates : new ArrayList<>();
        this.stateMap = new HashMap<>();

        if (bodyStates != null) {
            for (BodyState state : bodyStates) {
                stateMap.put(state.getRecordDate(), state.getState());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = dates.get(position);

        if (date == null) {
            // 处理空日期（月份开始前的空白格）
            holder.tvDay.setText("");
            holder.viewColor.setBackgroundColor(Color.parseColor("#E9ECEF")); // 灰色
            holder.tvDay.setTypeface(null, android.graphics.Typeface.NORMAL);
            return;
        }

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            holder.tvDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            String dateStr = dateFormat.format(date);
            String state = stateMap.get(dateStr);

            // 设置颜色
            if (state != null) {
                switch (state) {
                    case "疲惫":
                        holder.viewColor.setBackgroundColor(Color.parseColor("#FF6B6B")); // 红色
                        break;
                    case "精神":
                        holder.viewColor.setBackgroundColor(Color.parseColor("#51CF66")); // 绿色
                        break;
                    case "一般":
                        holder.viewColor.setBackgroundColor(Color.parseColor("#FFD93D")); // 黄色
                        break;
                    default:
                        holder.viewColor.setBackgroundColor(Color.parseColor("#E9ECEF")); // 灰色
                        break;
                }
            } else {
                holder.viewColor.setBackgroundColor(Color.parseColor("#E9ECEF")); // 灰色
            }

            // 如果是今天，加粗显示
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.equals(today)) {
                holder.tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
                holder.tvDay.setTextColor(Color.parseColor("#FF6200EE")); // 紫色突出显示
            } else {
                holder.tvDay.setTypeface(null, android.graphics.Typeface.NORMAL);
                holder.tvDay.setTextColor(Color.parseColor("#FF000000")); // 黑色
            }

        } catch (Exception e) {
            // 如果出现异常，显示空白
            holder.tvDay.setText("");
            holder.viewColor.setBackgroundColor(Color.parseColor("#E9ECEF"));
        }
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public void updateData(List<Date> dates, List<BodyState> bodyStates) {
        this.dates = dates != null ? dates : new ArrayList<>();
        this.stateMap.clear();

        if (bodyStates != null) {
            for (BodyState state : bodyStates) {
                stateMap.put(state.getRecordDate(), state.getState());
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        View viewColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            viewColor = itemView.findViewById(R.id.viewColor);
        }
    }
}