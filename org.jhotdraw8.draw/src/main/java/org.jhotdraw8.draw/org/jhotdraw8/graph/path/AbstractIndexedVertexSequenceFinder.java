/*
 * @(#)AbstractIntPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public abstract class AbstractIndexedVertexSequenceFinder<C extends Number & Comparable<C>>
        implements VertexSequenceFinder<Integer, C>,
        ReachabilityChecker<Integer, C> {
    protected final @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction;
    protected final @NonNull BiFunction<Integer, Integer, C> costFunction;
    protected final C maxCost;
    protected final C zero;

    protected final @NonNull BiFunction<C, C, C> sumFunction;

    public AbstractIndexedVertexSequenceFinder(@NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                               @NonNull BiFunction<Integer, Integer, C> costFunction,
                                               @NonNull C maxCost, C zero, @NonNull BiFunction<C, C, C> sumFunction) {
        this.nextNodesFunction = nextNodesFunction;
        this.costFunction = costFunction;
        this.maxCost = maxCost;
        this.zero = zero;
        this.sumFunction = sumFunction;
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Integer>, C> findVertexSequence(@NonNull Iterable<Integer> startVertices, @NonNull Predicate<Integer> goalPredicate, @NonNull C maxCost) {
        return findVertexSequence(startVertices, (IntPredicate) goalPredicate::test, maxCost);
    }

    @Override
    public OrderedPair<ImmutableList<Integer>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<Integer> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxCostBetweenWaypoints), zero, sumFunction);
    }


    /**
     * Checks whether a VertexPath through the graph which goes from the specified start
     * vertex to the specified goal vertex exists.
     * <p>
     * This method uses a breadth first search.
     *
     * @param start the start vertex
     * @param goal  the goal vertex
     * @return true if a traversal is possible, false otherwise
     */
    public boolean isReachable(Integer start, Integer goal, C maxCost) {
        return isReachable(start, (IntPredicate) i -> i == goal);
    }


    /**
     * Builds a VertexPath through the graph which goes from the specified start
     * vertex to the specified goal vertex.
     * <p>
     * This method uses a breadth first search and returns the first result that
     * it finds.
     * <p>
     * References:
     * <dl>
     *     <dt>Wikipedia, Dijkstra's algorithm, Practical optimizations and infinite graphs</dt>
     *     <dd><a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm#Practical_optimizations_and_infinite_graphs">
     *      wikipedia.org</a></dd>
     * </dl>
     *
     * @param starts         the start vertex
     * @param goalPredicate the goal predicate
     * @return a VertexPath if traversal is possible, null otherwise
     */
    public @Nullable OrderedPair<ImmutableList<Integer>, C> findVertexSequence(Iterable<Integer> starts, @NonNull IntPredicate goalPredicate, @NonNull C maxCost) {
        return findVertexSequence(starts, goalPredicate, addToBitSet(new BitSet()), maxCost);
    }

    public @Nullable OrderedPair<ImmutableList<Integer>, C> findVertexSequence(int start, @NonNull IntPredicate goalPredicate, @NonNull AddToIntSet visited, @NonNull C maxCost) {
        return findVertexSequence(Collections.singletonList(start), goalPredicate, visited, maxCost);
    }

    public @Nullable OrderedPair<ImmutableList<Integer>, C> findVertexSequence(Iterable<Integer> starts, @NonNull IntPredicate goalPredicate, @NonNull AddToIntSet visited, @NonNull C maxCost) {
        IndexedVertexBackLink<C> current = search(starts, goalPredicate, visited);
        if (current == null) {
            return null;
        }
        Deque<Integer> vertices = new ArrayDeque<Integer>();
        for (IndexedVertexBackLink<C> i = current; i != null; i = i.getParent()) {
            vertices.addFirst(i.getVertex());
        }
        return new OrderedPair<>(ImmutableLists.copyOf(vertices), current.getCost());
    }


    public boolean isReachable(Integer start, @NonNull IntPredicate goalPredicate) {
        return tryToReach(start, goalPredicate, addToBitSet(new BitSet()));
    }

    public static AddToIntSet addToBitSet(BitSet bitSet) {
        return i -> {
            boolean b = bitSet.get(i);
            if (!b) {
                bitSet.set(i);
            }
            return !b;
        };
    }


    /**
     * Builds a VertexPath through the graph which traverses the specified
     * waypoints.
     * <p>
     * This method uses a breadth first path search between waypoints.
     *
     * @param waypoints waypoints, the iteration sequence of this collection
     *                  determines how the waypoints are traversed
     * @return a VertexPath if traversal is possible, null otherwise
     */
    public @Nullable ImmutableList<Integer> findVertexSequenceOverWaypoints(@NonNull Iterable<Integer> waypoints) {
        try {
            return findVertexPathOverWaypointsNonNull(waypoints);
        } catch (PathBuilderException e) {
            return null;
        }
    }

    /**
     * Builds a VertexPath through the graph which traverses the specified
     * waypoints.
     * <p>
     * This method uses a breadth first path search between waypoints.
     *
     * @param waypoints waypoints, the iteration sequence of this collection
     *                  determines how the waypoints are traversed
     * @return a VertexPath
     * @throws PathBuilderException if the path cannot be constructed
     */
    public @NonNull ImmutableList<Integer> findVertexPathOverWaypointsNonNull(@NonNull Iterable<Integer> waypoints) throws PathBuilderException {
        Iterator<Integer> i = waypoints.iterator();
        List<Integer> pathElements = new ArrayList<>(16);
        if (!i.hasNext()) {
            throw new PathBuilderException("Could not find path with empty waypoints.");
        }
        int start = i.next();
        pathElements.add(start); // root element
        while (i.hasNext()) {
            int goal = i.next();
            IndexedVertexBackLink<C> back = search(Collections.singletonList(start), vi -> vi == goal,
                    new LinkedHashSet<>()::add);
            if (back == null) {
                throw new PathBuilderException("Could not find path from " + start + " to " + goal + ".");
            } else {
                int index = pathElements.size();
                for (; back.getParent() != null; back = back.getParent()) {
                    pathElements.add(index, back.getVertex());
                }
            }
            start = goal;
        }
        return ImmutableLists.copyOf(pathElements);
    }

    public @NonNull Function<Integer, Spliterator.OfInt> getNextNodesFunction() {
        return nextNodesFunction;
    }

    private @Nullable IndexedVertexBackLink<C> search(Iterable<Integer> start,
                                                      @NonNull IntPredicate goalPredicate,
                                                      @NonNull AddToIntSet visited) {
        return search(start, goalPredicate, nextNodesFunction, visited, maxCost, zero, costFunction);
    }

    private boolean tryToReach(Integer start,
                               @NonNull IntPredicate goalPredicate,
                               @NonNull AddToIntSet visited) {
        return tryToReach(Collections.singletonList(start), goalPredicate, nextNodesFunction, visited, maxCost, zero, costFunction);
    }

    public boolean isReachable(@NonNull Iterable<Integer> startVertices,
                               @NonNull Predicate<Integer> goalPredicate,
                               @NonNull C maxCost) {
        return tryToReach(startVertices, goalPredicate::test, nextNodesFunction, addToBitSet(new BitSet()), maxCost, zero, costFunction);

    }

    @Override
    public boolean isReachable(@NonNull Integer start, @NonNull Predicate<Integer> goalPredicate, @NonNull C maxCost) {
        return tryToReach(Collections.singletonList(start), goalPredicate::test, nextNodesFunction, addToBitSet(new BitSet()), maxCost, zero, costFunction);
    }

    @Override
    public boolean isReachableOverWaypoints(@NonNull Iterable<Integer> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return false;
    }

    protected abstract @Nullable IndexedVertexBackLink<C> search(@NonNull Iterable<Integer> start,
                                                                 @NonNull IntPredicate goal,
                                                                 @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                                                 @NonNull AddToIntSet visited,
                                                                 @NonNull C maxCost,
                                                                 @NonNull C zero,
                                                                 @NonNull BiFunction<Integer, Integer, C> costFunction);


    protected abstract boolean tryToReach(@NonNull Iterable<Integer> start,
                                          @NonNull IntPredicate goal,
                                          @NonNull Function<Integer, Spliterator.OfInt> nextNodesFunction,
                                          @NonNull AddToIntSet visited,
                                          @NonNull C maxCost,
                                          @NonNull C zero,
                                          @NonNull BiFunction<Integer, Integer, C> costFunction);

}
