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
import com.example.dreamisland.entity.Dream;

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

    // 定义日期点击回调接口
    public interface OnDateClickListener {
        void onDateClick(Date date);
    }

    private OnDateClickListener onDateClickListener;
    private List<Date> dates;
    private Map<String, String> stateMap; // date -> state
    private Map<String, String> dreamMap; // date -> dream nature
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private boolean isBodyStatus = true;

    // 设置日期点击监听器
    public void setOnDateClickListener(OnDateClickListener listener) {
        this.onDateClickListener = listener;
    }

    public CalendarAdapter(List<Date> dates, List<BodyState> bodyStates) {
        // 确保dates不为null
        this.dates = dates != null ? dates : new ArrayList<>();
        this.stateMap = new HashMap<>();
        this.dreamMap = new HashMap<>();

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
            holder.tvDay.setBackgroundColor(Color.parseColor("#000000")); // 黑色背景

            holder.tvDay.setTypeface(null, android.graphics.Typeface.NORMAL);
            return;
        }

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            holder.tvDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            String dateStr = dateFormat.format(date);
            String status = null;

            // 根据当前状态类型获取状态
            if (isBodyStatus) {
                status = stateMap.get(dateStr);
            } else {
                status = dreamMap.get(dateStr);
            }

            // 设置颜色
            if (status != null) {
                if (isBodyStatus) {
                    // 身体状态颜色
                    switch (status) {
                        case "疲惫":
                            holder.tvDay.setBackgroundColor(Color.parseColor("#FF6B6B")); // 红色
                            break;
                        case "精神":
                            holder.tvDay.setBackgroundColor(Color.parseColor("#51CF66")); // 绿色
                            break;
                        case "一般":
                            holder.tvDay.setBackgroundColor(Color.parseColor("#FFD93D")); // 黄色
                            break;
                        default:
                            holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background); // 默认黑色框框
                            break;
                    }
                } else {
                    // 梦境状态颜色
                    switch (status) {
                        case "好梦":
                            holder.tvDay.setBackgroundColor(Color.parseColor("#90EE90")); // 浅绿
                            break;
                        case "噩梦":
                            holder.tvDay.setBackgroundColor(Color.parseColor("#FF6961")); // 浅红
                            break;
                        case "其他":
                            holder.tvDay.setBackgroundColor(Color.parseColor("#FDFD96")); // 浅黄色
                            break;
                        default:
                            holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background); // 默认黑色框框
                            break;
                    }
                }
            } else {
                holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background); // 默认黑色框框

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
                holder.tvDay.setTextColor(Color.parseColor("#BB86FC")); // 淡紫色突出显示
            } else {
                holder.tvDay.setTypeface(null, android.graphics.Typeface.NORMAL);
                holder.tvDay.setTextColor(Color.parseColor("#FFFFFF")); // 白色
            }

            // 保存原始背景颜色或状态
            final int originalColor = holder.tvDay.getSolidColor();
            final boolean hasOriginalColor = status != null;
            final String originalStatus = status;

            // 添加日期点击事件
            holder.itemView.setOnClickListener(v -> {
                if (onDateClickListener != null && date != null) {
                    // 执行内缩动画
                    v.animate()
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                // 恢复原大小
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start();
                            })
                            .start();

                    // 临时改变背景颜色为灰色
                    holder.tvDay.setBackgroundColor(Color.parseColor("#6C757D")); // 灰色

                    // 恢复原背景
                    new android.os.Handler().postDelayed(() -> {
                        if (hasOriginalColor && originalStatus != null) {
                            // 如果有原始状态，重新应用状态对应的颜色
                            if (isBodyStatus) {
                                // 身体状态颜色
                                switch (originalStatus) {
                                    case "疲惫":
                                        holder.tvDay.setBackgroundColor(Color.parseColor("#FF6B6B")); // 红色
                                        break;
                                    case "精神":
                                        holder.tvDay.setBackgroundColor(Color.parseColor("#51CF66")); // 绿色
                                        break;
                                    case "一般":
                                        holder.tvDay.setBackgroundColor(Color.parseColor("#FFD93D")); // 黄色
                                        break;
                                    default:
                                        holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background); // 默认黑色框框
                                        break;
                                }
                            } else {
                                // 梦境状态颜色
                                switch (originalStatus) {
                                    case "好梦":
                                        holder.tvDay.setBackgroundColor(Color.parseColor("#90EE90")); // 浅绿
                                        break;
                                    case "噩梦":
                                        holder.tvDay.setBackgroundColor(Color.parseColor("#FF6961")); // 浅红
                                        break;
                                    case "其他":
                                        holder.tvDay.setBackgroundColor(Color.parseColor("#FDFD96")); // 浅黄色
                                        break;
                                    default:
                                        holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background); // 默认黑色框框
                                        break;
                                }
                            }
                        } else {
                            // 没有原始状态，使用默认背景
                            holder.tvDay.setBackgroundResource(R.drawable.calendar_day_background); // 默认黑色框框
                        }
                    }, 200);

                    // 调用点击回调
                    onDateClickListener.onDateClick(date);
                }
            });

        } catch (Exception e) {
            // 如果出现异常，显示空白
            holder.tvDay.setText("");
            holder.tvDay.setBackgroundColor(Color.parseColor("#E9ECEF"));

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
    
    /**
     * 更新梦境数据
     */
    public void updateDreamData(List<Date> dates, List<Dream> dreams) {
        this.dates = dates != null ? dates : new ArrayList<>();
        this.dreamMap.clear();

        if (dreams != null) {
            for (Dream dream : dreams) {
                if (dream.getCreatedAt() != null) {
                    String dateStr = dream.getCreatedAt().substring(0, 10); // 只取日期部分 yyyy-MM-dd
                    dreamMap.put(dateStr, dream.getNature());
                }
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * 更新所有数据（身体状态和梦境状态）
     */
    public void updateAllData(List<Date> dates, List<BodyState> bodyStates, List<Dream> dreams) {
        this.dates = dates != null ? dates : new ArrayList<>();
        this.stateMap.clear();
        this.dreamMap.clear();

        // 更新身体状态数据
        if (bodyStates != null) {
            for (BodyState state : bodyStates) {
                stateMap.put(state.getRecordDate(), state.getState());
            }
        }

        // 更新梦境状态数据
        if (dreams != null) {
            for (Dream dream : dreams) {
                if (dream.getCreatedAt() != null) {
                    String dateStr = dream.getCreatedAt().substring(0, 10); // 只取日期部分 yyyy-MM-dd
                    dreamMap.put(dateStr, dream.getNature());
                }
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * 设置当前显示的状态类型
     */
    public void setBodyStatus(boolean isBodyStatus) {
        this.isBodyStatus = isBodyStatus;
        notifyDataSetChanged();
    }
    
    /**
     * 获取当前显示的状态类型
     */
    public boolean isBodyStatus() {
        return isBodyStatus;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
        }
    }
}