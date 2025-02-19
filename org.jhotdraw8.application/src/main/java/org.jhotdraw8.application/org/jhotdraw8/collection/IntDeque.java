/*
 * @(#)IntDeque.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Interface for a {@link Deque} with a primitive integer data elements.
 */
public interface IntDeque extends Deque<Integer>, IntSequencedCollection {
    @Override
    default boolean add(Integer integer) {
        addLastAsInt(integer);
        return true;
    }

    @Override
    default void addFirst(Integer integer) {
        addFirstAsInt(integer);
    }

    /**
     * @see Deque#addFirst(Object)
     */
    void addFirstAsInt(int e);

    @Override
    default void addLast(Integer integer) {
        addLastAsInt(integer);
    }

    /**
     * @see Deque#addLast(Object)
     */
    void addLastAsInt(int e);


    @Override
    default Integer element() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return getFirstAsInt();
    }

    @Override
    default Integer getFirst() {
        return getFirstAsInt();
    }

    /**
     * @see Deque#getFirst()
     */
    int getFirstAsInt();

    @Override
    default Integer getLast() {
        return getLastAsInt();
    }

    /**
     * @see Deque#getLast()
     */
    int getLastAsInt();

    @Override
    default boolean offer(Integer integer) {
        addLastAsInt(integer);
        return true;
    }

    @Override
    default boolean offerFirst(Integer integer) {
        addFirstAsInt(integer);
        return true;
    }

    @Override
    default boolean offerLast(Integer integer) {
        addLastAsInt(integer);
        return true;
    }

    @Override
    default Integer peek() {
        if (isEmpty()) {
            return null;
        }
        return getFirstAsInt();
    }

    @Override
    default Integer peekFirst() {
        if (isEmpty()) {
            return null;
        }
        return getFirstAsInt();
    }

    @Override
    default Integer peekLast() {
        if (isEmpty()) {
            return null;
        }
        return getLastAsInt();
    }

    @Override
    default Integer poll() {
        if (isEmpty()) {
            return null;
        }
        return removeFirstAsInt();
    }

    @Override
    default Integer pollFirst() {
        if (isEmpty()) {
            return null;
        }
        return removeFirstAsInt();
    }

    @Override
    default Integer pollLast() {
        if (isEmpty()) {
            return null;
        }
        return removeLastAsInt();
    }

    @Override
    default Integer pop() {
        return removeFirstAsInt();
    }

    /**
     * @see Deque#pop()
     */
    default int popAsInt() {
        return removeFirstAsInt();
    }

    @Override
    default void push(Integer integer) {
        addFirstAsInt(integer);
    }

    /**
     * @see Deque#push(Object)
     */
    default void pushAsInt(int e) {
        addFirstAsInt(e);
    }

    @Override
    default boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    default Integer remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return removeFirstAsInt();
    }

    @Override
    default Integer removeFirst() {
        return removeFirstAsInt();
    }

    /**
     * @see Deque#removeFirst()
     */
    int removeFirstAsInt();

    @Override
    default boolean removeFirstOccurrence(Object o) {
        if (o instanceof Integer) {
            return removeFirstOccurrenceAsInt((int) o);
        }
        return false;
    }

    /**
     * @see Deque#removeFirstOccurrence(Object)
     */
    boolean removeFirstOccurrenceAsInt(int o);

    @Override
    default Integer removeLast() {
        return removeLastAsInt();
    }

    /**
     * @see Deque#removeLast()
     */
    int removeLastAsInt();

    @Override
    default boolean removeLastOccurrence(Object o) {
        if (o instanceof Integer) {
            return removeLastOccurrenceAsInt((int) o);
        }
        return false;
    }

    /**
     * @see Deque#removeLastOccurrence(Object)
     */
    boolean removeLastOccurrenceAsInt(int o);
}