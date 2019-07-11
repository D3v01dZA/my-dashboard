package com.altona.dashboard.component;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsableRecyclerAdapter extends RecyclerView.Adapter<UsableViewHolder> {

    private List<UsableRow> items;

    public UsableRecyclerAdapter(List<UsableRow> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        UsableRow item = items.get(position);
        return item.view();
    }

    @NonNull
    @Override
    public UsableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        return new UsableViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UsableViewHolder usableViewHolder, int i) {
        UsableRow item = items.get(i);
        item.render(usableViewHolder.itemView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void onItemClick(int i) {
        UsableRow item = items.get(i);
        item.onClick(this::notifyDataSetChanged);
    }

    void onItemLongClick(int i) {
        UsableRow item = items.get(i);
        item.onLongClick(this::notifyDataSetChanged);
    }

}
