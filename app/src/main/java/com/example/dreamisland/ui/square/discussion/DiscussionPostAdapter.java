package com.example.dreamisland.ui.square.discussion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamisland.databinding.ItemDiscussionPostBinding;
import com.example.dreamisland.model.Post;
import com.example.dreamisland.ui.square.discussion.detail.PostDetailActivity;

import java.util.List;

public class DiscussionPostAdapter extends RecyclerView.Adapter<DiscussionPostAdapter.ViewHolder> {
    private final List<Post> data;

    public DiscussionPostAdapter(List<Post> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiscussionPostBinding binding = ItemDiscussionPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post p = data.get(position);
        holder.binding.title.setText(p.title);
        holder.binding.author.setText(p.username);
        holder.binding.time.setText(p.createdAt);
        holder.binding.preview.setText(p.previewContent);
        holder.binding.replyCount.setText(String.valueOf(p.replyCount));
        holder.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent i = new Intent(ctx, PostDetailActivity.class);
            i.putExtra("post_id", p.postId);
            ctx.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemDiscussionPostBinding binding;
        ViewHolder(ItemDiscussionPostBinding b) {
            super(b.getRoot());
            this.binding = b;
        }
    }
}


