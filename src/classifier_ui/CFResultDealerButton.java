package classifier_ui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class CFResultDealerButton extends CFResult {

    final int RECT_WIDTH = 50;
    final int RECT_HEIGHT = 50;

    public CFResultDealerButton(ArrayList<Rectangle> resultRects, ArrayList<Rectangle> resultFullRects) {
        super(resultRects, resultFullRects);
    }
    public CFResultDealerButton() {
        super();
    }

    protected int m_getRectWidth() {
        return  RECT_WIDTH;
    }
    protected int m_getRectHeight() {
        return RECT_HEIGHT;
    }
    protected Color m_getRectColor() {
        return Color.GREEN;
    }
    protected Color m_getFullRectColor() {
        return Color.DARKGREEN;
    }
}
