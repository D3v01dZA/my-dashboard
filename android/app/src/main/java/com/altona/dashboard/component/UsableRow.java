package com.altona.dashboard.component;

import android.view.View;

public interface UsableRow {

    int view();

    void render(View view);

    void onClick(UsableChangeNotifier changeNotifier);

    void onLongClick(UsableChangeNotifier changeNotifier);

}
