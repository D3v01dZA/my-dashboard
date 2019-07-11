package com.altona.dashboard.component;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsableRecyclerTouchListener<T> implements RecyclerView.OnItemTouchListener {

    private GestureDetector gestureDetector;

    UsableRecyclerTouchListener(Context context, final UsableRecycler recyclerView) {
        this.gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    int id = recyclerView.getChildAdapterPosition(child);
                    ((UsableRecyclerAdapter) recyclerView.getAdapter()).onItemClick(id);
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    int id = recyclerView.getChildAdapterPosition(child);
                    ((UsableRecyclerAdapter) recyclerView.getAdapter()).onItemLongClick(id);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }

}
