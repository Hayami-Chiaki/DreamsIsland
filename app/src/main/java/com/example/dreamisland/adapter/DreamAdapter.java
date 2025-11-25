package com.example.dreamisland.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.model.Dream;

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
        
        // 内容过长时显示省略号
        String content = dream.getContent();
        if (content.length() > 50) {
            content = content.substring(0, 50) + "...";
        }
        holder.tvContent.setText(content);

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

    // ViewHolder类
    static class DreamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvNature;
        TextView tvDate;

        public DreamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_dream_title);
            tvContent = itemView.findViewById(R.id.tv_dream_content);
            tvNature = itemView.findViewById(R.id.tv_dream_nature);
            tvDate = itemView.findViewById(R.id.tv_dream_date);
        }
    }
}