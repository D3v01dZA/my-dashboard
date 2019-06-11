package com.altona.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Objects;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T, E> {

    public static <T, E> Result<T, E> success(T object) {
        return new Result<>(Objects.requireNonNull(object), null);
    }

    public static <T, E> Result<T, E> failure(E object) {
        return new Result<>(null, Objects.requireNonNull(object));
    }

    private T success;
    private E error;

    @SuppressWarnings("unchecked")
    public <R> Result<R, E> success(
            Function<? super T, ? extends R> whenSuccess
    ) {
        if (success != null) {
            return Result.success(whenSuccess.apply(success));
        }
        return (Result<R, E>) this;
    }

    @SuppressWarnings("unchecked")
    public <R> Result<R, E> successf(
            Function<? super T, Result<R, E>> whenSuccess
    ) {
        if (success != null) {
            return whenSuccess.apply(success);
        }
        return (Result<R, E>) this;
    }

    @SuppressWarnings("unchecked")
    public <R> Result<T, R> failure(
            Function<? super E, ? extends R> whenError
    ) {
        if (success != null) {
            return (Result<T, R>) this;
        }
        return Result.failure(whenError.apply(error));
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
