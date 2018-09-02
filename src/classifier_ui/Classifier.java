package classifier_ui;

import groovy.transform.PackageScope;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import opencv_client.CFFacade;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

abstract public class Classifier {
    private CFResult m_cr;

    public Classifier() {
       m_cr = m_createDefaultCFResult();
    }

    abstract protected CFFacade m_createFacade();
    abstract protected CFResult m_createDefaultCFResult();

    @PackageScope
    void classify( CFSettings cs, Image fxImage,
                         Pane pane, boolean isFixedSize,
                         Consumer<Rectangle> setRectangleEvents
                         ) throws Exception {
        m_cr = m_createFacade().classify(cs, fxImage);

        System.out.println(String.format(m_cr.getClass().toString() + ": m_classifyUI.classify resultRectSize = %d, fullRectSize = %d",
                m_cr.getRectangleList().size(), m_cr.getFullRectangleList().size()));
        // 結果の表示
        m_cr.getResultRectangles(pane, fxImage, m_cr.getFullRectangleList(),
                false, false);
        m_cr.getResultRectangles(pane, fxImage, m_cr.getRectangleList(),
                true, isFixedSize);

        // 後から編集可能にするためイベントを設定
        m_cr.getRectangleList().forEach(setRectangleEvents);
    }

    @PackageScope
    ObservableList<Rectangle> getRectangleList() {
        return m_cr.getRectangleList();
    }
    public ObservableList<Rectangle> getFullRectangleList() {
        return m_cr.getFullRectangleList();
    }
    @PackageScope
    void clearRects(Pane pane) {
        m_cr.clearRects(pane);
    }
    @PackageScope
    void changeHideFullRect(boolean isHide) {
        m_cr.changeHideFullRect(isHide);
    }
}
