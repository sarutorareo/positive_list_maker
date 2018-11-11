package utils;

import classifier_ui.EnEdge;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResizableRectangleTest {
    @org.junit.jupiter.api.Test
    void test_getCorner() {
        ResizableRectangle rect = new ResizableRectangle(0, 0, 20, 20);
        EnEdge c = rect.getEdge(4, 4, true);
        assertEquals(EnEdge.NW, c);
        c = rect.getEdge(4, 4, false);
        assertEquals(EnEdge.W, c);

        c = rect.getEdge(5, 5, true);
        assertEquals(EnEdge.NW, c);
        c = rect.getEdge(5, 5, false);
        assertEquals(EnEdge.NONE, c);

        c = rect.getEdge(5, 4, true);
        assertEquals(EnEdge.NW, c);
        c = rect.getEdge(5, 4, false);
        assertEquals(EnEdge.N, c);

        c = rect.getEdge(4, 5, true);
        assertEquals(EnEdge.NW, c);
        c = rect.getEdge(4, 5, false);
        assertEquals(EnEdge.W, c);

        c = rect.getEdge(15, 4, true);
        assertEquals(EnEdge.NE, c);
        c = rect.getEdge(15, 4, false);
        assertEquals(EnEdge.E, c);

        c = rect.getEdge(14, 4, true);
        assertEquals(EnEdge.NE, c);
        c = rect.getEdge(14, 4, false);
        assertEquals(EnEdge.N, c);

        c = rect.getEdge(14, 15, true);
        assertEquals(EnEdge.SE, c);
        c = rect.getEdge(14, 15, false);
        assertEquals(EnEdge.S, c);

        c = rect.getEdge(15, 15, true);
        assertEquals(EnEdge.SE, c);
        c = rect.getEdge(15, 15, false);
        assertEquals(EnEdge.E, c);

        c = rect.getEdge(15, 14, true);
        assertEquals(EnEdge.SE, c);
        c = rect.getEdge(15, 14, false);
        assertEquals(EnEdge.E, c);

        c = rect.getEdge(4, 15, true);
        assertEquals(EnEdge.SW, c);
        c = rect.getEdge(4, 15, false);
        assertEquals(EnEdge.W, c);
    }
}
