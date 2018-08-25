package application;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import opencv_client.CascadeClassify;
import org.opencv.core.Rect;
import positive_list_maker.PositiveListMakerFormController;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;

public class ViewFormController {
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
            FXMLLoader loader = new FXMLLoader(Paths.get("src\\positive_list_maker\\PositiveListMakerForm.fxml").toUri().toURL());
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
        m_doCapture();
        m_classify();
    }

    /**
     * BufferedImage型（TYPE_3BYTE_RGB）をMat型（CV_8UC3）に変換します
//     * @param image 変換したいBufferedImage型
     * @return 変換したMat型
    public static Mat BufferedImageToMat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.out.println("bufferedimage:" + data.length);
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }
     */

    public void m_classify() {
//        m_clearRectangles();

        CascadeClassify cc = new CascadeClassify();
        // ClassifierSettings cs = m_createClassifierSettingsFromGUI();
        ClassifierSettings cs = new ClassifierSettings();
        String cascadeXmlPath = cs.getCascadeXmlPath();

        // フルパワー
        ClassifierSettings full_cs = new ClassifierSettings();
        // memo : m_imgPathの代わりにBufferedImageToMat を利用して、メモリで画像を渡す。
        // ViewFormのほうもそうする

        // 変更後の画像をファイルに保存
        String imgPath = "";
        try {
            File f = m_saveImg();
            if (f == null) throw new Exception("!!! failed at save img file");
            imgPath = f.getPath();
        } catch (Exception ex) {
            System.out.println("!!! IOException at m_cassify " + ex.getMessage());
            return;
        }

        // 検出器を動かして検出結果をリストに追加
        Rect[] full_rects = cc.classify(imgPath, cascadeXmlPath,
                full_cs.minNeighbors, full_cs.scaleFactor, full_cs.getMinSize(), full_cs.getMaxSize());
        m_getResultRectangles(full_rects, false);
        // m_changeHideFullRect();

        // 本番
        Rect[] rects = cc.classify(imgPath, cascadeXmlPath,
                cs.minNeighbors, cs.scaleFactor, cs.getMinSize(), cs.getMaxSize());
        System.out.println("size = " + rects.length);
        m_getResultRectangles(rects, true);
    }

    private void m_getResultRectangles(Rect[] rects, boolean isWithParams) {
        ImageView imv = (ImageView)m_scene.lookup("#imvPic");
        javafx.scene.image.Image img = ((ImageView)m_scene.lookup("#imvPic")).getImage();

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
                ((Pane)imv.getParent()).getChildren().add(newRect);
//                m_rectangleList.add(newRect);
            }
            else {
                newRect.setStroke(Color.BLUE);
                newRect.setStrokeWidth(1);
                newRect.setMouseTransparent(true);
                ((Pane)imv.getParent()).getChildren().add(newRect);
//                m_fullRectangleList.add(newRect);
            }
        }
    }

    private Rectangle m_rectToRectanble(Rect r) {
        Rectangle result = new Rectangle(r.x, r.y, r.width, r.height);
        result.setFill(Color.TRANSPARENT);
        result.setStroke(Color.RED);
 //       m_setRectangleEvents(result);
        return result;
    }

    private File m_saveImg() throws Exception {
        final String picPath =  "d:\\temp\\temp.png";
        File f = null;
        try {
            // 画像を保存
            f = m_doSaveImg(m_scene, picPath, "png");
        }
        catch (IOException ex) {
            System.out.println("!!! IOException at m_saveImg " + ex.getMessage());
        }
        catch (Exception ex) {
            System.out.println("!!! Exception at m_saveImg " + ex.getMessage());
        }
        return f;
    }

    private File m_doSaveImg(Scene scene, String path, String fmt) throws java.io.IOException {
        System.out.println("start m_doSaveImg");
        ImageView imgView = (ImageView) scene.lookup("#imvPic");
        if (imgView == null) return null;
        javafx.scene.image.Image img = imgView.getImage();
        File f = new File(path);
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), fmt, f);
        System.out.println("return m_doSaveImg");
        return f;
    }

    private void m_doCapture() {
        try {
            // キャプチャの範囲
            java.awt.Rectangle bounds = new java.awt.Rectangle(0, 0, 1280, 800);

            // これで画面キャプチャ
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(bounds);
            WritableImage fxImage = SwingFXUtils.toFXImage(image, null);

            ImageView imgView = (ImageView)m_scene.lookup("#imvPic");
            imgView.setImage(fxImage);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


}
