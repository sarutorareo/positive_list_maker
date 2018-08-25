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
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import opencv_client.CascadeClassifierFacade;
import org.opencv.core.Rect;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;

public class ClassifierViewFormController {
    private final ClassifierUI m_classifierUI = new ClassifierUI();
    private PositiveListMakerFormController m_controller;
    private Scene m_scene = null;

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
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("PositiveListMakerForm.fxml"));
            FXMLLoader loader = new FXMLLoader(Paths.get("src\\classifier_ui\\PositiveListMakerForm.fxml").toUri().toURL());
            Scene scene = new Scene(loader.load());// new Scene(FXMLLoader.load(Paths.get("PositiveListMakerForm.fxml").toUri().toURL())
            stage.setScene(scene);

            //　ラベルをコントローラに渡しておく
            Label lbl = (Label)scene.lookup("#lblStatus");
            assert(lbl != null);
            PositiveListMakerFormController m_controller = loader.getController();
            m_controller.setLabel(lbl);
            m_controller.setScene(scene);

            // FXMLLoader loader = new FXMLLoader(getClass().getResource("PositiveListMakerForm.fxml"));
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)  {
                    System.out.println("Stage is closing");
                    try {
                        m_controller.saveText();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            });

            stage.show();
            m_controller.onShow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClick_capture_button(ActionEvent evt) throws Exception {
        System.out.println("capture_button");
        m_clearRects();
        m_doCapture();
        m_classify();
    }

    @FXML
    protected void onClick_hideFullRect(ActionEvent evt) {
        m_changeHideFullRect();
    }

    private void m_changeHideFullRect() {
        CheckBox chkHide = (CheckBox)m_scene.lookup("#cbxHideFullRect");
        m_classifierUI.changeHideFullRect(chkHide.isSelected());
    }

    public void m_classify() {
        Pane pane = (Pane) m_scene.lookup("#pnImageView");
        m_classifierUI.clearRects(pane);

        // 画像を取得
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        assert(imgView != null);
        javafx.scene.image.Image fxImage = imgView.getImage();

        // 検出器のパラメータを取得
        ClassifierSettings cs = null;
        try {
            cs = ClassifierSettings.load();
        } catch(IOException ex) {
            cs = new ClassifierSettings();
        }

        // 検出、結果の表示
        m_classifierUI.classify(cs, fxImage, pane);

        m_changeHideFullRect();
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

    private void m_clearRects() {
        Pane pane = (Pane)m_scene.lookup("#pnImageView");
        m_classifierUI.clearRects(pane);
    }
}
