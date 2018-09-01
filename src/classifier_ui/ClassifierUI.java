package classifier_ui;

import application.ClassifierSettings;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import opencv_client.CascadeClassifierFacade;

import java.util.ArrayList;

public class ClassifierUI {

    public ClassifyResult classify(ClassifierSettings cs, Image fxImage) {
        CascadeClassifierFacade cc = new CascadeClassifierFacade();
        String cascadeXmlPath = cs.getCascadeXmlPath();

        // フルパワー
        ClassifierSettings full_cs = new ClassifierSettings();

        // 検出器を動かして検出結果をリストに追加
        ArrayList<Rectangle> fullRects = cc.classify(fxImage, cascadeXmlPath,
                full_cs.minNeighbors, full_cs.scaleFactor,
                full_cs.getMinSize(), full_cs.getMaxSize(), false);

        // 本番
        ArrayList<Rectangle> rects = cc.classify(fxImage, cascadeXmlPath,
                cs.minNeighbors, cs.scaleFactor,
                cs.getMinSize(), cs.getMaxSize(), true);
        System.out.println("size = " + rects.size());
        return new ClassifyResultPlayer(rects, fullRects);
    }
}
