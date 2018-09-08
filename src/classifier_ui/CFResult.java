package classifier_ui;

import groovy.transform.PackageScope;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

abstract public class CFResult {
    private final ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList();
    private final ObservableList<Rectangle> m_fullRectangleList = FXCollections.observableArrayList();

    abstract protected int m_getRectWidth();
    abstract protected int m_getRectHeight();
    abstract protected Color m_getRectColor();
    abstract protected Color m_getFullRectColor();

    public  ObservableList<Rectangle> getRectangleList() {
        return m_rectangleList;
    }
    public  ObservableList<Rectangle> getFullRectangleList() {
        return m_fullRectangleList;
    }

    public CFResult(ArrayList<Rectangle> resultRects, ArrayList<Rectangle> resultFullRects) {
        m_rectangleList.addAll(resultRects);
        m_fullRectangleList.addAll(resultFullRects);
    }
    public CFResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public void changeHideFullRect(boolean hide) {
        getFullRectangleList().forEach(r -> {
            r.setVisible(!hide);
        });
    }

    public void getResultRectangles(Pane pane, Image img, ObservableList<Rectangle> rects, boolean isWithParams, boolean isFixed) {
        rects.forEach(newRect -> {
            if (isWithParams) {
                if (isFixed) {
                    newRect.setWidth(m_getRectWidth());
                    newRect.setHeight(m_getRectHeight());
                    newRect.setX(Math.min(img.getWidth() - m_getRectWidth(), Math.max(0, newRect.getX())));
                    newRect.setY(Math.min(img.getHeight() - m_getRectHeight(), Math.max(0, newRect.getY())));
                }
                newRect.setStroke(m_getRectColor());
                newRect.setStrokeWidth(3);
                pane.getChildren().add(newRect);
            }
            else {
                newRect.setStroke(m_getFullRectColor());
                newRect.setStrokeWidth(1);
                newRect.setMouseTransparent(true);
                pane.getChildren().add(newRect);
            }
        });
    }
}
