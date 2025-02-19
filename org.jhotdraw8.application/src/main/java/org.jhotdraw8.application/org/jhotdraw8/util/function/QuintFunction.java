/*
 * @(#)QuintFunction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.util.function;

import org.jhotdraw8.annotation.NonNull;

/**
 * Represents a function that accepts 5 arguments and produces a result.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <W> the type of the fourth argument to the function
 * @param <X> the type of the fifth argument to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface QuintFunction<T, U, V, W, X, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @param w the fourth function argument
     * @param x the fifth function argument
     * @return the function result
     */
    @NonNull R apply(T t, U u, V v, W w, X x);

}
