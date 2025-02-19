/*
 * @(#)MappedList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Wraps a {@link List} in a {@link List} of a different type.
 * <p>
 * The underlying List is referenced - not copied.
 *
 * @author Werner Randelshofer
 */
public final class MappedList<E, F> extends AbstractList<E> {

    private final List<F> backingList;
    private final Function<F, E> mapf;

    public MappedList(List<F> backingList, Function<F, E> mapf) {
        this.backingList = backingList;
        this.mapf = mapf;
    }

    @Override
    public boolean contains(Object o) {
        return backingList.contains(o);
    }

    @Override
    public E get(int index) {
        return mapf.apply(backingList.get(index));
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<F> i = backingList.iterator();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public E next() {
                return mapf.apply(i.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public @NonNull Spliterator<E> spliterator() {
        class MappingSpliterator implements Spliterator<E> {
            private final Spliterator<F> i;

            public MappingSpliterator(Spliterator<F> i) {
                this.i = i;
            }

            @Override
            public boolean tryAdvance(Consumer<? super E> action) {
                return i.tryAdvance(f -> action.accept(mapf.apply(f)));
            }

            @Override
            public Spliterator<E> trySplit() {
                Spliterator<F> fSpliterator = i.trySplit();
                return fSpliterator == null ? null : new MappingSpliterator(fSpliterator);
            }

            @Override
            public long estimateSize() {
                return i.estimateSize();
            }

            @Override
            public int characteristics() {
                return i.characteristics();
            }
        }
        return new MappingSpliterator(backingList.spliterator());
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public @NonNull List<E> subList(int fromIndex, int toIndex) {
        return new MappedList<>(backingList.subList(fromIndex, toIndex), mapf);
    }
}
