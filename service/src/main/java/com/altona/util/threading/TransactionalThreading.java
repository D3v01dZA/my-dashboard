package com.altona.util.threading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TransactionalThreading {

    private TransactionalExecutor transactionalExecutor;
    private ExecutorService executorService;

    @Autowired
    public TransactionalThreading(TransactionalExecutor transactionalExecutor, @Value("${background.threads}") int threads) {
        this.transactionalExecutor = transactionalExecutor;
        this.executorService = Executors.newFixedThreadPool(threads);
    }

    public void executeInTransaction(Runnable runnable) {
        executorService.execute(() -> transactionalExecutor.executeInTransaction(runnable));
    }

    public void executeInReadOnlyTransaction(Runnable runnable) {
        executorService.execute(() -> transactionalExecutor.executeInReadOnlyTransaction(runnable));
    }

    @Component
    public static class TransactionalExecutor {

        @Transactional(readOnly = true)
        public void executeInReadOnlyTransaction(Runnable runnable) {
            runnable.run();
        }

        @Transactional
        public void executeInTransaction(Runnable runnable) {
            runnable.run();
        }

    }
}
