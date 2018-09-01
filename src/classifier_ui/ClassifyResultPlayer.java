package classifier_ui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class ClassifyResultPlayer extends ClassifyResult {

    final int RECT_WIDTH = 175;
    final int RECT_HEIGHT = 70;

    public ClassifyResultPlayer(ArrayList<Rectangle> resultRects, ArrayList<Rectangle> resultFullRects) {
        super(resultRects, resultFullRects);
    }
    public ClassifyResultPlayer() {
        super();
    }

    protected int m_getRectWidth() {
        return  RECT_WIDTH;
    }
    protected int m_getRectHeight() {
        return RECT_HEIGHT;
    }
    protected Color m_getRectColor() {
        return Color.RED;
    }
    protected Color m_getFullRectColor() {
        return Color.BLUE;
    }
}
