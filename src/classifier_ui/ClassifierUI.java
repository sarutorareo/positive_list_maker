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

    public ClassifyResult classify(ClassifierSettings cs, Image fxImage) {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        String cascadeXmlPath = cs.getCascadeXmlPath();

        // フルパワー
        ClassifierSettings full_cs = new ClassifierSettings();

        // 検出器を動かして検出結果をリストに追加
        boolean removeDuplicate = false;
        ArrayList<Rectangle> fullRects = cc.classify(fxImage, cascadeXmlPath,
                full_cs.minNeighbors, full_cs.scaleFactor,
                full_cs.getMinSize(), full_cs.getMaxSize(), removeDuplicate);

        // 本番
        removeDuplicate = true;
        ArrayList<Rectangle> rects = cc.classify(fxImage, cascadeXmlPath,
                cs.minNeighbors, cs.scaleFactor,
                cs.getMinSize(), cs.getMaxSize(), removeDuplicate);
        System.out.println("size = " + rects.size());
        return new ClassifyResult(rects, fullRects);
    }

    public void getResultRectangles(Pane pane, Image img, ArrayList<Rectangle> rects, boolean isWithParams) {
        final int RECT_WIDTH = 175;
        final int RECT_HEIGHT = 70;

        rects.forEach(newRect -> {
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
        });
    }


}
