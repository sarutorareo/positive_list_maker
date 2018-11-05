package utils;

import classifier_ui.EnCorner;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResizableRectangleTest {
    @org.junit.jupiter.api.Test
    void test_getCorner() {
        ResizableRectangle rect = new ResizableRectangle(0, 0, 20, 20);
        EnCorner c = rect.getCorner(4, 4);
        assertEquals(EnCorner.NW, c);

        c = rect.getCorner(5, 4);
        assertEquals(EnCorner.NONE, c);

        c = rect.getCorner(4, 5);
        assertEquals(EnCorner.NONE, c);

        c = rect.getCorner(15, 4);
        assertEquals(EnCorner.NE, c);

        c = rect.getCorner(14, 4);
        assertEquals(EnCorner.NONE, c);

        c = rect.getCorner(14, 15);
        assertEquals(EnCorner.NONE, c);

        c = rect.getCorner(15, 15);
        assertEquals(EnCorner.SE, c);

        c = rect.getCorner(4, 15);
        assertEquals(EnCorner.SW, c);
    }
}
