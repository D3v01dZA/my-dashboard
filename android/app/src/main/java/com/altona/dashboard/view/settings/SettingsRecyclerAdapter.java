package com.altona.dashboard.view.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.altona.dashboard.R;
import com.altona.dashboard.service.Settings;
import com.altona.dashboard.view.util.UserInputDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.ViewHolder> {

    private Context context;
    private Settings settings;
    private List<Entry> entries;

    SettingsRecyclerAdapter(Context context) {
        this.context = context;
        this.settings = new Settings(context);
        recreateEntries();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.setting_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        Entry entry = entries.get(i);
        holder.name.setText(entry.getKey());
        holder.value.setText(entry.getValue());
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    protected void recreateEntries() {
        this.entries = new ArrayList<>();
        entries.add(new Entry("Host", settings.getHost(), host -> settings.setHost(host)));
    }

    class Entry {

        private String key;
        private String value;
        private Consumer<String> setter;

        Entry(String key, String value, Consumer<String> setter) {
            this.key = key;
            this.value = value;
            this.setter = setter;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }

        private void set(String value) {
            setter.accept(value);
            recreateEntries();
        }
    }

    void onItemClick(int i) {
        final Entry entry = entries.get(i);
        UserInputDialog.open(context, "Set " + entry.getKey(), entry.getValue(), string -> {
            entry.set(string);
            notifyDataSetChanged();
        }, () -> {});
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
