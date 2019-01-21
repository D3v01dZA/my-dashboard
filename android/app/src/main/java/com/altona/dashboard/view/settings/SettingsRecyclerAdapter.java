package com.altona.dashboard.view.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.altona.dashboard.R;

import java.util.function.Consumer;

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.ViewHolder> {

    private Context context;
    private Settings settings;

    SettingsRecyclerAdapter(Context context, Settings settings) {
        this.context = context;
        this.settings = settings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_settings_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        Settings.Entry entry = settings.getEntries().get(i);
        holder.name.setText(entry.getKey());
        holder.value.setText(entry.getValue());
    }

    @Override
    public int getItemCount() {
        return settings.getEntries().size();
    }

    void onItemClick(int i) {
        final Settings.Entry entry = settings.getEntries().get(i);
        SettingsUserInputDialog.open(context, entry.getKey(), entry.getValue(), new Consumer<String>() {
            @Override
            public void accept(String s) {
                entry.set(s);
                notifyDataSetChanged();
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView value;

        ViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.setting_name);
            this.value = view.findViewById(R.id.setting_value);
        }
    }

}
