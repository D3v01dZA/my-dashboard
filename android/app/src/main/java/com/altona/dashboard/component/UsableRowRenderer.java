package com.altona.dashboard.component;

import android.view.View;

public interface UsableRowRenderer<T> {

    void render(View view, T item);

}
