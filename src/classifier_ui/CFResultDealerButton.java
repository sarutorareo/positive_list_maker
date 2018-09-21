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
        return Color.HOTPINK;
    }
    @Override
    protected Color m_getFullRectColor() {
        return Color.DEEPPINK;
    }
}
