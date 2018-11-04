package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static utils.DateTimeUtil.toNumStr;

public class DateTimeUtilTest {
    @org.junit.jupiter.api.Test
    void test_toNum() {
        Calendar c = Calendar.getInstance();
        c.set(2018, 3 /* 3=4月*/, 2,12, 13, 4);

        String numStr = toNumStr(c.getTime());
        assertEquals("20180402_121304", numStr);
    }
}
