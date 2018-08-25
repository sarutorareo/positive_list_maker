package classifier_ui;

import application.ClassifierSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import opencv_client.CascadeClassifierFacade;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class ClassifierUI {
    private final ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList();
    private final ArrayList<Rectangle> m_fullRectangleList = new ArrayList<>();

    public void clearRects(Pane pane) {
        List<Rectangle> list = new ArrayList();
        pane.getChildren().forEach(node -> {
            if (node instanceof Rectangle) {
                list.add((Rectangle)node);
            }
        });

        list.forEach(r -> {
            pane.getChildren().remove(r);
        });

        getRectangleList().clear();
        getFullRectangleList().clear();
    }

    public  ObservableList<Rectangle> getRectangleList() {
        return m_rectangleList;
    }
    public  ArrayList<Rectangle> getFullRectangleList() {
        return m_fullRectangleList;
    }

    public void changeHideFullRect(boolean hide) {
        getFullRectangleList().forEach(r -> {
            r.setVisible(!hide);
        });
    }

    public void classify(ClassifierSettings cs, Image fxImage, Pane pane) {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        String cascadeXmlPath = cs.getCascadeXmlPath();

        // フルパワー
        ClassifierSettings full_cs = new ClassifierSettings();

        // 検出器を動かして検出結果をリストに追加
        Rect[] full_rects = cc.classify(fxImage, cascadeXmlPath,
                full_cs.minNeighbors, full_cs.scaleFactor, full_cs.getMinSize(), full_cs.getMaxSize());
        m_getResultRectangles(pane, fxImage, full_rects, false);

        // 本番
        Rect[] rects = cc.classify(fxImage, cascadeXmlPath,
                cs.minNeighbors, cs.scaleFactor, cs.getMinSize(), cs.getMaxSize());
        System.out.println("size = " + rects.length);
        m_getResultRectangles(pane, fxImage, rects, true);

    }

    private void m_getResultRectangles(Pane pane, Image img, Rect[] rects, boolean isWithParams) {
        final int RECT_WIDTH = 175;
        final int RECT_HEIGHT = 70;

        for (int i = 0; i < rects.length; i++) {
            Rectangle newRect = m_rectToRectanble(rects[i]);
            if (isWithParams) {
                if (true) {
                    newRect.setWidth(RECT_WIDTH);
                    newRect.setHeight(RECT_HEIGHT);
                    newRect.setX(Math.min(img.getWidth() - RECT_WIDTH, Math.max(0, newRect.getX())));
                    newRect.setY(Math.min(img.getHeight() - RECT_HEIGHT, Math.max(0, newRect.getY())));
                }
                newRect.setStroke(Color.RED);
                newRect.setStrokeWidth(3);
                pane.getChildren().add(newRect);
                getRectangleList().add(newRect);
            }
            else {
                newRect.setStroke(Color.BLUE);
                newRect.setStrokeWidth(1);
                newRect.setMouseTransparent(true);
                pane.getChildren().add(newRect);
                getFullRectangleList().add(newRect);
            }
        }
    }

    private Rectangle m_rectToRectanble(Rect r) {
        Rectangle result = new Rectangle(r.x, r.y, r.width, r.height);
        result.setFill(Color.TRANSPARENT);
        return result;
    }

}
