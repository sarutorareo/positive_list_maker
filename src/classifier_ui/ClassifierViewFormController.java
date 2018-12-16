package classifier_ui;

import groovy.transform.PackageScope;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import javafx.embed.swing.SwingFXUtils;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel.RIL_WORD;
import static net.sourceforge.tess4j.ITessAPI.TessPageSegMode.*;
import static utils.DateTimeUtil.toNumStr;
import static utils.ImageUtils.*;

public class ClassifierViewFormController {
    private ClassifierPlayer m_cfPlayer = new ClassifierPlayer();
    private ClassifierDealerButton m_cfDealerButton = new ClassifierDealerButton();
    private ClassifierChip m_cfChip = new ClassifierChip();
    private Scene m_scene = null;
    private AutoCaptureThread m_autoCaptureThread = null;
    public void setScene(Scene scene) {
        m_scene = scene;
    }

    private int m_getRIL() {
        return RIL_WORD;
        /*
        TextField tf = (TextField) m_scene.lookup("#txtRIL");
        int result = RIL_WORD;
        try {
            result = Integer.parseInt(tf.getText());
        }
        catch (Exception ex) {
            ;
        }
        return result;
        */
    }

    private String m_getLanguage() {
        TextField txtLang = (TextField) m_scene.lookup("#txtLang");
        return txtLang.getText();
    }

    private double m_getConfidence() {
        TextField txtConfidence = (TextField) m_scene.lookup("#txtConfidence");

        return Double.parseDouble(txtConfidence.getText());
    }

    @FXML
    protected void onClick_ocr_button(ActionEvent evt) throws Exception {
        m_clearRectangles();

        Pane pane = (Pane) m_scene.lookup("#pnImageView");
        m_doOCR(pane);
    }

    @FXML
    protected void onClick_simpleOcr_button(ActionEvent evt) throws Exception {
        m_clearRectangles();

        Pane pane = (Pane) m_scene.lookup("#pnImageView");
        m_doSimpleOCR(pane);
    }

    private void m_doOCR(Pane pane) {
        Image fxBinImage = m_getBinImage();
        BufferedImage bBinImage = SwingFXUtils.fromFXImage(fxBinImage, null);

        m_cfPlayer.getRectangleList().forEach(pr -> {
            Rectangle ocrRect = m_getOcrRectanble(pr);
            BufferedImage bSubBinImage = getRectSubImage(bBinImage, ocrRect);
            // m_debugSavePng(pr, ocrRect);
            m_ocrPartImage(bSubBinImage, pane, ocrRect);
        });

        m_cfChip.getRectangleList().forEach(chRect -> {
            BufferedImage bSubBinImage = getRectSubImage(bBinImage, chRect);
            // m_debugSavePng(pr, ocrRect);
            m_ocrPartImage(bSubBinImage, pane, chRect);
        });
    }

    private Rectangle m_getOcrRectanble(Rectangle pr) {
        return new Rectangle(pr.getX(),
                        pr.getY() + pr.getHeight()/2,
                        pr.getWidth(),
                        pr.getHeight() / 2);
    }

    private void m_doSimpleOCR(Pane pane) {
        Image fxImage = m_getImage();
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
        Rectangle pr = new Rectangle(0, 0, bImage.getWidth(), bImage.getHeight());
        m_ocrPartImage(bImage, pane, pr);
    }

    private void m_debugSavePng(Rectangle pr, Rectangle subRect) {
        Image fxImage = m_getImage();
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
        BufferedImage bSubImage = getRectSubImage(bImage, subRect);
        String strDate = toNumStr(new Date());
        String filePathWithoutExt = String.format("D:\\MyProgram\\GitHub\\positive_list_maker\\temp_pic\\player_%s_%d-%d", strDate, (int)pr.getX(), (int)pr.getY());
        try {
            ImageUtils.savePng(bSubImage, filePathWithoutExt + ".png");
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void m_ocrPartImage(BufferedImage bSubImage, Pane pane, Rectangle subRect)
    {
        BufferedImage cleanedImage = (new ImageCleaner()).cleaning(bSubImage);
        // debug用に画像を保存
        try {
            String strDate = toNumStr(new Date());
            String filePathWithoutExt = String.format("D:\\MyProgram\\GitHub\\positive_list_maker\\temp_pic\\player_%s_%d-%d", strDate, (int) subRect.getX(), (int) subRect.getY());
            ImageUtils.saveTiff(cleanedImage, filePathWithoutExt + ".tif");
            ImageUtils.savePng(cleanedImage, filePathWithoutExt + ".png");
        } catch(IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        List<Word> word = m_getWords(cleanedImage);
        word.forEach(w -> {
            if (isWord(w)) {
                System.out.print(String.format("(%s)", w.getText()));
                System.out.println(w.toString());

                java.awt.Rectangle b = w.getBoundingBox();
                Rectangle r = new Rectangle(subRect.getX() + b.x, subRect.getY() + b.y, b.width, b.height);
                m_wordToRect(pane, w, r);
                m_wordToNum(pane, w, r);
           }
        });
    }

    private void m_wordToRect(Pane pane, Word w, Rectangle r) {
        java.awt.Rectangle b = w.getBoundingBox();
        r.setFill(Color.TRANSPARENT);
        r.setStroke(new Color(w.getConfidence() / 100, 0.8, 0.8, 1));
        r.setStrokeWidth(4 * w.getConfidence() / 100 + 1);
        pane.getChildren().add(r);
    }

    private void m_wordToNum(Pane pane, Word w, Rectangle r) {
        Text t = new javafx.scene.text.Text(r.getX() + r.getWidth() + 3, r.getY() + 3, w.getText());
        t.setFill(Color.RED);
        pane.getChildren().add(t);
    }

    private List<Word> m_getWords(BufferedImage bImage) {
        ITesseract tesseract = new Tesseract();
        String lang = m_getLanguage();
        tesseract.setLanguage(lang);
        //数字と一部の四則演算記号のみ認識させる
        tesseract.setTessVariable("tessedit_char_whitelist","0123456789,");
//        tesseract.setTessVariable("language_model_penalty_non_dict_word", "1");
        tesseract.setTessVariable("load_system_dawg", "0");
        tesseract.setTessVariable("user_words_suffix", "0");
        tesseract.setPageSegMode(PSM_AUTO);
        tesseract.setDatapath("D:\\MyProgram\\GitHub\\positive_list_maker\\tessdata");
        tesseract.setOcrEngineMode(1);
        List<Word> word = tesseract.getWords(bImage, m_getRIL());
        System.out.println(String.format("word num (%d)", word.size()));
        return word;
    }

    private boolean isWord(Word w) {
        double minConfidence = m_getConfidence();
        return ((w.getText().trim().length() > 0)
                && (w.getConfidence() > minConfidence)
                && !w.getText().contains(" ")
                && w.getText().matches(".*[0-9].*"));
    }

    @FXML
    protected void onClick_addTessBox_button(ActionEvent evt) throws Exception {
        System.out.println("positive_list_button");
        Stage parent = (Stage) ((Node) evt.getTarget()).getScene().getWindow();
        try {
            Stage stage = new Stage();
            stage.initOwner(parent);
            stage.setTitle("addTessBox");
            FXMLLoader loader = new FXMLLoader(Paths.get("src\\classifier_ui\\BoxMakerFormTess.fxml").toUri().toURL());
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

            //　コントローラにデータを渡しておく
            BoxMakerFormControllerTess m_controller = loader.getController();
            m_controller.setScene(scene);

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)  {
                    System.out.println("Stage is closing");
                }
            });

            stage.show();
            m_controller.onShow(m_getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClick_positive_list_button(ActionEvent evt) throws Exception {
        System.out.println("positive_list_button");
        Stage parent = (Stage) ((Node) evt.getTarget()).getScene().getWindow();
        try {
            Stage stage = new Stage();
            stage.initOwner(parent);
            stage.setTitle("new window example");
            FXMLLoader loader = new FXMLLoader(Paths.get("src\\classifier_ui\\BoxMakerFormPositivePic.fxml").toUri().toURL());
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

            //　コントローラにデータを渡しておく
            BoxMakerFormControllerPositivePic m_controller = loader.getController();
            m_controller.setScene(scene);

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)  {
                    System.out.println("Stage is closing");
                    try {
                        if (m_controller.isAutoSave()) {
                            Classifier cf = m_controller.getCurrentTargetClassifier();
                            m_controller.saveTextAndImage(cf.getDataDir(), cf::getPosListStr);
                            /*
                            Classifier cf = m_controller.getCurrentTargetClassifier();
                            File f = m_controller.m_saveImage(cf.getDataDir());
                            m_controller.m_saveText(cf, f.getPath());
                            */
                        }
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
        setResult();
    }

    @FXML
    protected void onClick_paste_button(ActionEvent evt) throws Exception {
        pasteImageAndClassify();
        setResult();
    }


    @PackageScope
    synchronized void captureImageAndClassify() throws Exception {
        System.out.println("captureImageAndClassify");
        m_doCapture();
        m_classify();
    }

    @PackageScope
    synchronized void pasteImageAndClassify() throws Exception {
        System.out.println("pasteImageAndClassify");
        m_doPaste();
        m_classify();
    }

    private void m_changeHideFullRect() {
        CheckBox chkHide = (CheckBox)m_scene.lookup("#cbxHideFullRect");
        boolean isHide = chkHide.isSelected();
        m_cfPlayer.changeHideFullRect(isHide);
        m_cfDealerButton.changeHideFullRect(isHide);
        m_cfChip.changeHideFullRect(isHide);
    }

    @PackageScope
    void setResult() {
        Pane pane = (Pane) m_scene.lookup("#pnImageView");
        System.out.println(String.format("m_classifyUI.classify resultRecultSize = %d, fullResutSize = %d",
                m_cfPlayer.getRectangleList().size(), m_cfPlayer.getFullRectangleList().size()));

        m_clearRectangles();

        // 画像を取得
        Image fxImage = m_getImage();
        m_cfPlayer.setResultToPane(fxImage, pane, true);
        m_cfDealerButton.setResultToPane(fxImage, pane, true);
        m_cfChip.setResultToPane(fxImage, pane, false);

        // 結果の表示
        m_changeHideFullRect();
    }

    @FXML
    private boolean m_getBinary() {
        CheckBox cbx = (CheckBox) m_scene.lookup("#cbxBinary");
        return cbx.isSelected();
    }

    @FXML
    protected void onClick_binary(ActionEvent evt) {
        boolean isBinary = m_getBinary();
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        ImageView imgBinView = (ImageView) m_scene.lookup("#imvBinPic");
        imgView.setVisible(!isBinary);
        imgBinView.setVisible(isBinary);
    }

    @FXML
    protected void onClick_hideFullRect(ActionEvent evt) {
        m_cfPlayer.changeHideFullRect(m_isHideFullRect());
        m_cfDealerButton.changeHideFullRect(m_isHideFullRect());
        m_cfChip.changeHideFullRect(m_isHideFullRect());
    }

    @FXML
    protected void onClick_autoCapture_button(ActionEvent evt) {
        m_changeAutoCapture();
    }

    private boolean m_isHideFullRect() {
        CheckBox chkHide = (CheckBox)m_scene.lookup("#cbxHideFullRect");
        return chkHide.isSelected();
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

    private void m_clearRectangles() {
        Pane pane = (Pane) m_scene.lookup("#pnImageView");
        Classifier.clearRectsFromPane(pane);
        Classifier.clearTextsFromPane(pane);
    }

    private void m_classify() throws Exception {
        // 画像を取得
        Image fxImage = m_getImage();

        // 検出
        m_cfPlayer.classify(fxImage, this::m_dummySetEvents);
        m_cfDealerButton.classify(fxImage, this::m_dummySetEvents);
        m_cfChip.classify(fxImage, this::m_dummySetEvents);
    }
    private void m_dummySetEvents(Rectangle rect)
    {
        ;
    }

    private Image m_getImage() {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        assert(imgView != null);
        return imgView.getImage();
    }

    private Image m_getBinImage() {
        ImageView imgView = (ImageView) m_scene.lookup("#imvBinPic");
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
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            m_initImageView(fxImage);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void m_initImageView(Image fxImage) {
        ImageView imgView = (ImageView)m_scene.lookup("#imvPic");
        imgView.setImage(fxImage);

//            Image fxGrayImage = toGrayScaleFxImage(fxImage);
        // Image fxGrayImage = toBinaryFxImage(fxImage,80 );
        Image fxGrayImage = toReverceBinaryFxImage(fxImage);
        ImageView imgGrayView = (ImageView)m_scene.lookup("#imvBinPic");
        imgGrayView.setImage(fxGrayImage);

        // 画面サイズを調整
        Pane pane = (Pane)imgView.getParent();
        pane.setMaxWidth(fxImage.getWidth());
        pane.setMaxHeight(fxImage.getHeight());
        imgView.setFitWidth(fxImage.getWidth());
        imgView.setFitHeight(fxImage.getHeight());
        imgGrayView.setFitWidth(fxImage.getWidth());
        imgGrayView.setFitHeight(fxImage.getHeight());
        javafx.stage.Window wnd = m_scene.getWindow();
        wnd.setWidth(fxImage.getWidth() + 30);
        wnd.setHeight(fxImage.getHeight() + 100);
    }

    protected void m_doPaste() throws Exception {
        // クリップボードの画像を取得
        Clipboard cb = Clipboard.getSystemClipboard();
        System.out.println(cb.getContentTypes());
        Image newImage = cb.getImage();
        m_initImageView(newImage);
    }
}
