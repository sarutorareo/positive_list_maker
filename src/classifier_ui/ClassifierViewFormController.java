package classifier_ui;

import application.ClassifierSettings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.embed.swing.SwingFXUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;

public class ClassifierViewFormController {
    private final ClassifierUI m_classifierUI = new ClassifierUI();
    private Scene m_scene = null;
    private AutoCaptureThread m_autoCaptureThread = null;
    public void setScene(Scene scene) {
        m_scene = scene;
    }

    @FXML
    protected void onClick_positive_list_button(ActionEvent evt) throws Exception {
        System.out.println("positive_list_button");
        Stage parent = (Stage) ((Node) evt.getTarget()).getScene().getWindow();
        try {
            Stage stage = new Stage();
            stage.initOwner(parent);
            stage.setTitle("new window example");
            FXMLLoader loader = new FXMLLoader(Paths.get("src\\classifier_ui\\PositiveListMakerForm.fxml").toUri().toURL());
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

            //　コントローラにデータを渡しておく
            Label lbl = (Label)scene.lookup("#lblStatus");
            assert(lbl != null);
            PositiveListMakerFormController m_controller = loader.getController();
            m_controller.setLabel(lbl);
            m_controller.setScene(scene);

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)  {
                    System.out.println("Stage is closing");
                    try {
                        File f = m_controller.saveImage();
                        m_controller.saveText(f.getPath());
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            });

            stage.show();
            m_controller.onShow(m_getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClick_capture_button(ActionEvent evt) throws Exception {
        captureImageAndClassify();
    }

    public synchronized ClassifyResult captureImageAndClassify() {
        System.out.println("captureImageAndClassify");
        m_doCapture();
        return m_classify();
    }

    public void setResult(ClassifyResult cr) {
        Pane pane = (Pane) m_scene.lookup("#pnImageView");
        m_classifierUI.clearRects(pane);
        // 画像を取得
        Image fxImage = m_getImage();

        // 結果の表示
        m_classifierUI.getResultRectangles(pane, fxImage, cr.fullRects, false);
        m_classifierUI.getResultRectangles(pane, fxImage, cr.rects, true);
        m_changeHideFullRect();
    }

    @FXML
    protected void onClick_hideFullRect(ActionEvent evt) {
        m_changeHideFullRect();
    }

    @FXML
    protected void onClick_autoCapture_button(ActionEvent evt) {
        m_changeAutoCapture();
    }

    private void m_changeHideFullRect() {
        CheckBox chkHide = (CheckBox)m_scene.lookup("#cbxHideFullRect");
        m_classifierUI.changeHideFullRect(chkHide.isSelected());
    }

    private void m_changeAutoCapture() {
        ToggleButton tglAutoCapture = (ToggleButton)m_scene.lookup("#tglAutoCapture");
        if (tglAutoCapture.isSelected()) {
            m_startThread();
        }
        else {
            stopThread();
        }
    }

    private void m_startThread() {
        if (m_autoCaptureThread == null) {
            m_autoCaptureThread = new AutoCaptureThread(this);
        }
        m_autoCaptureThread.start();
    }

    public void stopThread() {
        if (m_autoCaptureThread == null) {
            return;
        }
        m_autoCaptureThread.stopLoop();
        m_autoCaptureThread = null;
    }

    public ClassifyResult m_classify() {
        // 画像を取得
        Image fxImage = m_getImage();

        // 検出器のパラメータを取得
        ClassifierSettings cs = null;
        try {
            cs = ClassifierSettings.load();
        } catch(IOException ex) {
            cs = new ClassifierSettings();
        }

        // 検出
        return m_classifierUI.classify(cs, fxImage);
    }

    private Image m_getImage() {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        assert(imgView != null);
        return imgView.getImage();
    }

    private void m_doCapture() {
        try {
            // キャプチャの範囲 (starsの画面はwidth = 795, height = 579)
            java.awt.Rectangle bounds = new java.awt.Rectangle(0, 0, 880, 640);

            // これで画面キャプチャ
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(bounds);
            WritableImage fxImage = SwingFXUtils.toFXImage(image, null);

            ImageView imgView = (ImageView)m_scene.lookup("#imvPic");
            imgView.setImage(fxImage);

            // 画面サイズを調整
            Pane pane = (Pane)imgView.getParent();
            pane.setMaxWidth(fxImage.getWidth());
            pane.setMaxHeight(fxImage.getHeight());
            imgView.setFitWidth(fxImage.getWidth());
            imgView.setFitHeight(fxImage.getHeight());
            javafx.stage.Window wnd = m_scene.getWindow();
            wnd.setWidth(fxImage.getWidth() + 30);
            wnd.setHeight(fxImage.getHeight() + 100);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
