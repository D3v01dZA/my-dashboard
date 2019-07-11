package com.altona.dashboard.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsableRecycler extends RecyclerView {

    public UsableRecycler(@NonNull Context context) {
        super(context);
    }

    public UsableRecycler(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UsableRecycler(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setup(
            List<UsableRow> items
    ) {
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(new UsableRecyclerAdapter(items));
        setItemAnimator(new DefaultItemAnimator());
        addOnItemTouchListener(new UsableRecyclerTouchListener<>(getContext(), this));
    }

}
