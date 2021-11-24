/*
 * @(#)DepthFirstSpliterator.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AbstractEnumeratorSpliterator;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * DepthFirstSpliterator.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class DepthFirstSpliterator<V> extends AbstractEnumeratorSpliterator<V> {

    private final @NonNull Function<V, Iterable<V>> nextFunction;
    private final @NonNull Deque<V> deque;
    private final @NonNull AddToSet<V> visited;

    /**
     * Creates a new instance.
     *
     * @param nextNodesFunction the nextFunction
     * @param root              the root vertex
     */
    public DepthFirstSpliterator(Function<V, Iterable<V>> nextNodesFunction, V root) {
        this(nextNodesFunction, root, new HashSet<>()::add);
    }

    /**
     * Creates a new instance.
     *
     * @param nextFunction the function that returns the next vertices of a given vertex
     * @param root         the root vertex
     * @param visited      a predicate with side effect. The predicate returns true
     *                     if the specified vertex has been visited, and marks the specified vertex
     *                     as visited.
     */
    public DepthFirstSpliterator(@Nullable Function<V, Iterable<V>> nextFunction, @Nullable V root, @Nullable AddToSet<V> visited) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        Objects.requireNonNull(nextFunction, "nextFunction");
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(visited, "visited");
        this.nextFunction = nextFunction;
        deque = new ArrayDeque<>(16);
        this.visited = visited;
        if (visited.add(root)) {
            deque.addLast(root);
        }
    }

    @Override
    public boolean moveNext() {
        if (deque.isEmpty()) {
            return false;
        }
        current = deque.removeLast();
        for (V next : nextFunction.apply(current)) {
            if (visited.add(next)) {
                deque.addLast(next);
            }
        }
        return true;
    }
}
