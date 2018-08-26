package classifier_ui;

import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class ClassifyResult {
    public ArrayList<Rectangle> fullRects;
    public ArrayList<Rectangle> rects;
    public ClassifyResult(ArrayList<Rectangle> resultRects, ArrayList<Rectangle> resultFullRects) {
        rects = resultRects;
        fullRects = resultFullRects;
    }
}
