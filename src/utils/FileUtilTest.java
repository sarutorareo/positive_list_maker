package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilTest {
    @org.junit.jupiter.api.Test
    void test_getExtention() {
        String path = "d:\\temp\\exp0.test.tif";
        assertEquals("tif", FileUtil.getExtention(path));

        path = "d:\\temp\\exp0_test_tif";
        assertEquals("", FileUtil.getExtention(path));
    }

    @org.junit.jupiter.api.Test
    void test_getWithoutExtention() {
        String path = "d:\\temp\\exp0.test.tif";
        assertEquals("d:\\temp\\exp0.test", FileUtil.getWithoutExtention(path));

        path = "d:\\temp\\exp0_test_tif";
        assertEquals("d:\\temp\\exp0_test_tif", FileUtil.getWithoutExtention(path));
    }
}