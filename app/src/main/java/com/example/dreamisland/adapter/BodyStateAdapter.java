// BodyStateAdapter.java
package com.example.dreamisland.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.R;
import com.example.dreamisland.entity.BodyState;

import java.util.List;

public class BodyStateAdapter extends RecyclerView.Adapter<BodyStateAdapter.ViewHolder> {

    private List<BodyState> bodyStates;

    public BodyStateAdapter(List<BodyState> bodyStates) {
        this.bodyStates = bodyStates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_body_state, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BodyState state = bodyStates.get(position);
        holder.tvDate.setText(state.getRecordDate());
        holder.tvState.setText(state.getState());

        // 根据状态设置不同的颜色
        switch (state.getState()) {
            case "疲惫":
                holder.tvState.setTextColor(holder.itemView.getContext()
                        .getResources().getColor(android.R.color.holo_red_dark));
                break;
            case "精神":
                holder.tvState.setTextColor(holder.itemView.getContext()
                        .getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "一般":
                holder.tvState.setTextColor(holder.itemView.getContext()
                        .getResources().getColor(android.R.color.holo_orange_dark));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bodyStates != null ? bodyStates.size() : 0;
    }

    public void updateData(List<BodyState> newBodyStates) {
        this.bodyStates = newBodyStates;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvState;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvState = itemView.findViewById(R.id.tvState);
        }
    }
}