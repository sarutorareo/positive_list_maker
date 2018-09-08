package classifier_ui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class CFResultPlayer extends CFResult {

    final int RECT_WIDTH = 175;
    final int RECT_HEIGHT = 70;

    public CFResultPlayer(ArrayList<Rectangle> resultRects, ArrayList<Rectangle> resultFullRects) {
        super(resultRects, resultFullRects);
    }
    public CFResultPlayer() {
        super();
    }

    public int getRectWidth() {
        return  RECT_WIDTH;
    }
    public int getRectHeight() {
        return RECT_HEIGHT;
    }
    protected Color m_getRectColor() {
        return Color.RED;
    }
    protected Color m_getFullRectColor() {
        return Color.BLUE;
    }
}
