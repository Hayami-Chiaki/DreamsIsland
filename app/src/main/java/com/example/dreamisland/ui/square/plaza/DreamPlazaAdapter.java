package com.example.dreamisland.ui.square.plaza;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.databinding.ItemDreamPlazaBinding;
import com.example.dreamisland.model.Dream;
import com.example.dreamisland.ui.square.plaza.detail.DreamDetailActivity;

import java.util.List;

public class DreamPlazaAdapter extends RecyclerView.Adapter<DreamPlazaAdapter.ViewHolder> {
    private final List<Dream> data;

    public DreamPlazaAdapter(List<Dream> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDreamPlazaBinding binding = ItemDreamPlazaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dream d = data.get(position);
        holder.binding.username.setText(d.username);
        holder.binding.title.setText(d.title);
        holder.binding.preview.setText(d.previewContent);
        holder.binding.nature.setText(d.nature);
        holder.binding.tags.setText(d.tags);
        holder.binding.time.setText(d.createdAt);
        holder.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent i = new Intent(ctx, DreamDetailActivity.class);
            i.putExtra("dream_id", d.dreamId);
            ctx.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemDreamPlazaBinding binding;
        ViewHolder(ItemDreamPlazaBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }
}


