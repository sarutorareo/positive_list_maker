package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaTest {
    @org.junit.jupiter.api.Test
    void test_initialize_array() {
        int a[] = new int[10];

        assertEquals(10, a.length);
        assertEquals(0, a[0]);
        assertEquals(0, a[9]);

        boolean b[] = new boolean[10];

        assertEquals(10, b.length);
        assertEquals(false, b[0]);
        assertEquals(false, b[9]);
    }
}
