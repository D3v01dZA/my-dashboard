package com.altona.util.threading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class TransactionalThreading {

    private TransactionalExecutor transactionalExecutor;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    public TransactionalThreading(TransactionalExecutor transactionalExecutor) {
        this.transactionalExecutor = transactionalExecutor;
    }

    public <T> List<T> executeInReadOnlyTransaction(List<Supplier<T>> suppliers) {
        List<Callable<T>> callables = suppliers.stream()
                .<Callable<T>>map(supplier -> () -> transactionalExecutor.executeInReadOnlyTransaction(supplier))
                .collect(Collectors.toList());
        try {
            return executorService.invokeAll(callables).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
