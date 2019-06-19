package com.altona.dashboard.component;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsableRecycler<T> extends RecyclerView {

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
            int rowLayout,
            List<T> items,
            UsableRowRenderer<T> rowRenderer
    ) {
        setup(rowLayout, items, rowRenderer, (view, timeSummaryEntry) -> { }, (view, timeSummaryEntry) -> { });
    }

    public void setup(
            int rowLayout,
            List<T> items,
            UsableRowRenderer<T> rowRenderer,
            UsableClickHandler<T> rowClickHandler
    ) {
        setup(rowLayout, items, rowRenderer, rowClickHandler, (view, timeSummaryEntry) -> { });
    }

    public void setup(
            int rowLayout,
            List<T> items,
            UsableRowRenderer<T> rowRenderer,
            UsableClickHandler<T> rowClickHandler,
            UsableClickHandler<T> rowLongClickHandler
    ) {
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(new UsableRecyclerAdapter<>(rowLayout, items, rowRenderer, rowClickHandler, rowLongClickHandler));
        setItemAnimator(new DefaultItemAnimator());
        addOnItemTouchListener(new UsableRecyclerTouchListener<>(getContext(), this));
    }

}
