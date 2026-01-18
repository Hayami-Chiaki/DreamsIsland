package com.example.dreamisland.ui.square.plaza;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EndlessScrollListener extends RecyclerView.OnScrollListener {
    public interface OnLoadMore {
        void onLoadMore();
    }

    private final OnLoadMore callback;

    public EndlessScrollListener(OnLoadMore callback) {
        this.callback = callback;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy <= 0) return;
        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            LinearLayoutManager llm = (LinearLayoutManager) lm;
            int total = llm.getItemCount();
            int lastVisible = llm.findLastVisibleItemPosition();
            if (lastVisible >= total - 2) {
                callback.onLoadMore();
            }
        }
    }
}


