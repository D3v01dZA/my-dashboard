package com.altona.util.functional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T, E> {

    public static <T, E> Result<T, E> success(T object) {
        return new Result<>(object, null);
    }

    public static <T, E> Result<T, E> error(E object) {
        return new Result<>(null, object);
    }

    private T success;
    private E error;

    public <R> Result<R, E> mapSuccess(
            Function<? super T, ? extends R> whenSuccess
    ) {
        return Result.success(whenSuccess.apply(success));
    }

    public <R> Result<R, E> flatMapSuccess(
            Function<? super T, Result<R, E>> whenSuccess
    ) {
        return whenSuccess.apply(success);
    }

    public <R> R map(
            Function<? super T, ? extends R> whenSuccess,
            Function<? super E, ? extends R> whenError
    ) {
        if (success != null) {
            return whenSuccess.apply(success);
        }
        return whenError.apply(error);
    }

}
