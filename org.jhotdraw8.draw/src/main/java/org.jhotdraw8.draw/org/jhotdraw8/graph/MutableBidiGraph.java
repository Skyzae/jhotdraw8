/*
 * @(#)MutableBidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Defines an API for a mutable directed graph that allows to follow arrows in
 * backward direction.
 *
 * @param <V> the vertex type
 * @param <A> the arrow data type
 */
public interface MutableBidiGraph<V, A> extends BidiGraph<V, A>, MutableDirectedGraph<V, A> {
    /**
     * Adds an arrow from 'va' to 'vb' and an arrow from 'vb' to 'va'.
     *
     * @param va    vertex a
     * @param vb    vertex b
     * @param arrow the arrow data
     */
    @SuppressWarnings("unused")
    default void addBidiArrow(@NonNull V va, @NonNull V vb, @Nullable A arrow) {
        addArrow(va, vb, arrow);
        addArrow(vb, va, arrow);
    }

}
