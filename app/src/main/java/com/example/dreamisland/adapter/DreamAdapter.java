package com.example.dreamisland.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.model.Dream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DreamAdapter extends RecyclerView.Adapter<DreamAdapter.DreamViewHolder> {

    private Context context;
    private List<Dream> dreamList;
    private OnDreamClickListener listener;

    // 点击事件接口
    public interface OnDreamClickListener {
        void onDreamClick(Dream dream);
        void onDreamLongClick(Dream dream);
    }

    public DreamAdapter(Context context, List<Dream> dreamList, OnDreamClickListener listener) {
        this.context = context;
        this.dreamList = dreamList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dream, parent, false);
        return new DreamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DreamViewHolder holder, int position) {
        Dream dream = dreamList.get(position);
        holder.tvTitle.setText(dream.getTitle());
        holder.tvNature.setText(dream.getNature());
        holder.tvDate.setText(dream.getCreatedAt());

        // 设置收藏状态
        if (dream.isFavorite()) {
            holder.tvFavorite.setVisibility(View.VISIBLE);
        } else {
            holder.tvFavorite.setVisibility(View.GONE);
        }

        // 内容过长时显示省略号
        String content = dream.getContent();
        if (content.length() > 50) {
            content = content.substring(0, 50) + "...";
        }
        holder.tvContent.setText(content);

        // 显示标签
        showTags(holder.layoutTags, dream.getTags());

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDreamClick(dream);
            }
        });

        // 长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onDreamLongClick(dream);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return dreamList != null ? dreamList.size() : 0;
    }

    // 更新数据
    public void setDreamList(List<Dream> dreamList) {
        this.dreamList = dreamList;
        notifyDataSetChanged();
    }

    /**
     * 显示标签
     */
    private void showTags(LinearLayout container, String tagsJson) {
        // 清空之前的标签
        container.removeAllViews();
        container.setVisibility(View.GONE);

        if (tagsJson == null || tagsJson.isEmpty() || tagsJson.equals("[]")) {
            return;
        }

        try {
            // 解析标签JSON
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> tagsList = gson.fromJson(tagsJson, listType);

            if (tagsList != null && !tagsList.isEmpty()) {
                container.setVisibility(View.VISIBLE);

                // 添加标签
                for (String tag : tagsList) {
                    // 创建标签视图
                    TextView tagView = new TextView(context);
                    tagView.setText(tag);
                    tagView.setTextSize(12);
                    tagView.setTextColor(context.getResources().getColor(R.color.blue_dark));
                    tagView.setBackgroundColor(context.getResources().getColor(R.color.blue_light));
                    tagView.setPadding(8, 4, 8, 4);

                    // 添加间距
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 8, 0);
                    tagView.setLayoutParams(params);


                    container.addView(tagView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ViewHolder类
    static class DreamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvNature;
        TextView tvDate;
        LinearLayout layoutTags;
        TextView tvFavorite;

        public DreamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_dream_title);
            tvContent = itemView.findViewById(R.id.tv_dream_content);
            tvNature = itemView.findViewById(R.id.tv_dream_nature);
            tvDate = itemView.findViewById(R.id.tv_dream_date);
            layoutTags = itemView.findViewById(R.id.layout_dream_tags);
            tvFavorite = itemView.findViewById(R.id.tv_favorite);
        }
    }
}