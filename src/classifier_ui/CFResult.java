package classifier_ui;

import groovy.transform.PackageScope;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.util.ArrayList;

abstract public class CFResult {
    private final ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList();
    private final ObservableList<Rectangle> m_fullRectangleList = FXCollections.observableArrayList();

    abstract public int getRectFixedWidth();
    abstract public int getRectFixedHeight();
    abstract public Color getRectColor();
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

    @PackageScope
    CFResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    @PackageScope
    void changeHideFullRect(boolean hide) {
        getFullRectangleList().forEach(r -> {
            r.setVisible(!hide);
        });
    }

    @PackageScope
    void getResultRectangles(Pane pane, Image img, ObservableList<Rectangle> rects, boolean isWithParams, boolean isFixed) {
        rects.forEach(newRect -> {
            if (isWithParams) {
                if (isFixed) {
                    newRect.setWidth(getRectFixedWidth());
                    newRect.setHeight(getRectFixedHeight());
                    newRect.setX(Math.min(img.getWidth() - getRectFixedWidth(), Math.max(0, newRect.getX())));
                    newRect.setY(Math.min(img.getHeight() - getRectFixedHeight(), Math.max(0, newRect.getY())));
                }
                newRect.setStroke(getRectColor());
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

    @PackageScope
    void setRectClickable(boolean clickable) {
        getRectangleList().forEach(r -> {
            r.setMouseTransparent(!clickable);
        });
    }

    @PackageScope
    void setMouseCursor(Cursor csr) {
        getRectangleList().forEach(r -> {
            r.setCursor(csr);
        });
    }
}
