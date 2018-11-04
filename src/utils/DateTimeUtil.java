package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    static public String toNumStr(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
        return sdf.format(d);
    }
}
