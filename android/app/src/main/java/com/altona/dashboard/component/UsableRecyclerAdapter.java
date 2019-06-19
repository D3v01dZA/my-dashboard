package com.altona.dashboard.component;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UsableRecyclerAdapter<T> extends RecyclerView.Adapter<UsableViewHolder> {

    private int rowLayout;
    private List<T> items;
    private UsableRowRenderer<T> viewRenderer;
    private UsableClickHandler<T> rowClickHandler;
    private UsableClickHandler<T> rowLongClickHandler;

    @NonNull
    @Override
    public UsableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(rowLayout, parent, false);
        return new UsableViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UsableViewHolder usableViewHolder, int i) {
        T item = items.get(i);
        viewRenderer.render(usableViewHolder.itemView, item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void onItemClick(int i) {
        T item = items.get(i);
        rowClickHandler.handle(item, this::notifyDataSetChanged);
    }

    void onItemLongClick(int i) {
        T item = items.get(i);
        rowLongClickHandler.handle(item, this::notifyDataSetChanged);
    }

}
