package classifier_ui;

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
import opencv_client.CFFacadePlayer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;

public class ClassifierViewFormController {
    private CFResultPlayer m_crPlayer = new CFResultPlayer();
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
        CFResult cr = captureImageAndClassify();
        setResult(cr);
    }

    public synchronized CFResult captureImageAndClassify() throws Exception {
        System.out.println("captureImageAndClassify");
        m_doCapture();
        return m_classify();
    }

    public void setResult(CFResult cr) {
        Pane pane = (Pane) m_scene.lookup("#pnImageView");
        m_crPlayer.clearRects(pane);
        m_crPlayer = (CFResultPlayer)cr;
        System.out.println(String.format("m_classifyUI.classify resultRecultSize = %d, fullResutSize = %d",
                m_crPlayer.getRectangleList().size(), m_crPlayer.getFullRectangleList().size()));
        // 画像を取得
        Image fxImage = m_getImage();

        // 結果の表示
        m_crPlayer.getResultRectangles(pane, fxImage, cr.getFullRectangleList(), false, true);
        m_crPlayer.getResultRectangles(pane, fxImage, cr.getRectangleList(), true, true);
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
        m_crPlayer.changeHideFullRect(chkHide.isSelected());
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

    private CFResult m_classify() throws Exception {
        // 画像を取得
        Image fxImage = m_getImage();

        // 検出器のパラメータを取得
        CFSettings cs = null;
        try {
            cs = CFSettings.load();
        } catch(IOException ex) {
            cs = new CFSettings();
        }

        // 検出
        return new CFFacadePlayer().classify(cs, fxImage);
    }

    private Image m_getImage() {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        assert(imgView != null);
        return imgView.getImage();
    }

    private void m_doCapture() {
        try {
            // キャプチャの範囲 (starsの画面はwidth = 795, height = 579)
            java.awt.Rectangle bounds = new java.awt.Rectangle(0, 100, 880, 640);

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
