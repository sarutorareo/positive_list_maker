package classifier_ui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class CFResultChip extends CFResult {

    final int RECT_WIDTH = 40;
    final int RECT_HEIGHT = 20;

    public CFResultChip(ArrayList<Rectangle> resultRects, ArrayList<Rectangle> resultFullRects) {
        super(resultRects, resultFullRects);
    }
    public CFResultChip() {
        super();
    }

    @Override
    public int getRectFixedWidth() {
        return  RECT_WIDTH;
    }
    @Override
    public int getRectFixedHeight() {
        return RECT_HEIGHT;
    }
    @Override
    public Color getRectColor() {
        return Color.YELLOW;
    }
    @Override
    protected Color m_getFullRectColor() {
        return Color.GREENYELLOW;
    }
}
