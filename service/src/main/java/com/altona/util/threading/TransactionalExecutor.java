package com.altona.util.threading;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TransactionalExecutor {

    @Transactional(readOnly = true)
    public <T> T executeInReadOnlyTransaction(Supplier<T> supplier) {
        return supplier.get();
    }

}
