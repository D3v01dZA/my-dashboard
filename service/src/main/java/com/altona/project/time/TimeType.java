package com.altona.project.time;

import java.util.function.Supplier;

public enum TimeType {

    WORK {
        @Override
        public <R> R map(Supplier<R> workSupplier, Supplier<R> breakSupplier) {
            return workSupplier.get();
        }
    },
    BREAK {
        @Override
        public <R> R map(Supplier<R> workSupplier, Supplier<R> breakSupplier) {
            return breakSupplier.get();
        }
    };

    public abstract  <R> R map(Supplier<R> workSupplier, Supplier<R> breakSupplier);

}
