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
        return Color.RED;
    }
    @Override
    protected Color m_getFullRectColor() {
        return Color.BLUE;
    }
}
