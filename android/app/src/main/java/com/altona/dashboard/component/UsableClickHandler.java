package com.altona.dashboard.component;

public interface UsableClickHandler<T> {

    void handle(T item, UsableChangeNotifier changeNotifier);

}
