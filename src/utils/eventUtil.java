package utils;

import javafx.scene.input.MouseEvent;

public class eventUtil {
    static public String getAxisStrFromEvent(MouseEvent evt) {
        return String.format("x = %f, y = %f", evt.getX(), evt.getY());
    }
}
