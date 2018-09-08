package classifier_ui;

import groovy.transform.PackageScope;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class CFResultDealerButton extends CFResult {

    final int RECT_WIDTH = 40;
    final int RECT_HEIGHT = 40;

    public CFResultDealerButton(ArrayList<Rectangle> resultRects, ArrayList<Rectangle> resultFullRects) {
        super(resultRects, resultFullRects);
    }
    @PackageScope
    CFResultDealerButton() {
        super();
    }

    public int getRectWidth() {
        return  RECT_WIDTH;
    }
    public int getRectHeight() {
        return RECT_HEIGHT;
    }
    public Color getRectColor() {
        return Color.HOTPINK;
    }
    protected Color m_getFullRectColor() {
        return Color.DEEPPINK;
    }
}
