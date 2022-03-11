package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.iterator.BreadthFirstSpliterator;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class ChunkedMutableIntAttributed32BitIndexedBidiGraphTest extends AbstractMutableIndexedBidiGraphTest {
    @Override
    protected MutableIndexedBidiGraph newInstance(int maxArity) {
        return new ChunkedMutableIntAttributed32BitIndexedBidiGraph();
    }

    @Override
    public @NonNull List<DynamicTest> dynamicTestsRandomGraph() {
        return List.of();
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomMultiGraph() {
        return Collections.emptyList();
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRandomGraphWithArrowData() {
        return Collections.emptyList();
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsRemoveAll() {
        return Arrays.asList(
                dynamicTest("arity0", () -> testGraphRemoveAll(8, 0)),
                dynamicTest("arity1", () -> testGraphRemoveAll(8, 1)),
                dynamicTest("arity7", () -> testGraphRemoveAll(8, 7)),
                dynamicTest("arity17", () -> testGraphRemoveAll(20, 17))
        );
    }

    public void testGraphRemoveAll(int vertexCount, int maxArity) throws IOException {
        ChunkedMutableIntAttributed32BitIndexedBidiGraph instance = (ChunkedMutableIntAttributed32BitIndexedBidiGraph) newInstance(maxArity);
        SimpleMutableBidiGraph<Integer, Integer> expected = new SimpleMutableBidiGraph<>();
        IndexedBidiGraphWrapper actual = new IndexedBidiGraphWrapper(instance);

        for (int vidx = 0; vidx < vertexCount; vidx++) {
            instance.addVertexAsInt(vidx);
            expected.addVertex(vidx);
        }

        Random rng = new Random(0);
        for (int v = 0; v < vertexCount; v++) {
            int n = rng.nextInt(maxArity + 1);
            for (int i = 0; i < n; i++) {
                int u = rng.nextInt(vertexCount);

                int nextCount = expected.getNextCount(v);
                int prevCount = expected.getPrevCount(u);
                if (nextCount < maxArity && prevCount < maxArity) {
                    if (expected.findIndexOfNext(v, u) < 0) {
                        instance.addArrowAsInt(v, u, -u);
                        expected.addArrow(v, u, -u);
                        assertEqualSortedGraphInt(expected, instance);
                    }
                }
            }
        }
        assertEqualSortedGraphInt(expected, instance);

        // Remove random arrows
        for (int i = 0; i < vertexCount; i++) {
            int vidx = rng.nextInt(vertexCount);
            for (int j = expected.getNextCount(vidx) - 1; j >= 0; j--) {
                expected.removeNext(vidx, j);
            }
            instance.removeAllNextAsInt(vidx);
            assertEqualSortedGraphInt(expected, instance);
        }
    }

    @Override
    protected void assertEqualSortedGraphInt(BidiGraph<Integer, Integer> expected, IndexedBidiGraph actual) {
        super.assertEqualSortedGraphInt(expected, actual);
        ChunkedMutableIntAttributed32BitIndexedBidiGraph a = (ChunkedMutableIntAttributed32BitIndexedBidiGraph) actual;
        for (Integer v : expected.getVertices()) {
            {
                Set<Integer> expectedBfs = StreamSupport.stream(new BreadthFirstSpliterator<Integer>(expected::getNextVertices, v), false).collect(Collectors.toCollection(LinkedHashSet::new));
                Set<Integer> actualBfs = StreamSupport.stream(a.breadthFirstLongSpliterator(v), false).map(Long::intValue).collect(Collectors.toCollection(LinkedHashSet::new));
                assertEquals(expectedBfs, actualBfs);
            }
            {
                Set<Integer> expectedBfs = StreamSupport.stream(new BreadthFirstSpliterator<Integer>(expected::getPrevVertices, v), false).collect(Collectors.toCollection(LinkedHashSet::new));
                Set<Integer> actualBfs = StreamSupport.stream(a.backwardBreadthFirstLongSpliterator(v), false).map(Long::intValue).collect(Collectors.toCollection(LinkedHashSet::new));
                assertEquals(expectedBfs, actualBfs);
            }
        }
    }
}
