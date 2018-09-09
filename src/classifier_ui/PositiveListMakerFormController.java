package classifier_ui;
import application.RectPos;
import groovy.transform.PackageScope;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.scene.shape.Rectangle;
import javafx.util.converter.DoubleStringConverter;
import org.opencv.core.Size;

import javax.annotation.Resources;
import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;

public class PositiveListMakerFormController {
    private Label m_lblStatus = null;
    private MouseEvent m_mousePressEvent = null;
    private RectPos m_mousePressPos = null;
    private Rectangle m_rect = null;
    private Scene m_scene = null;

    /*
    private final ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList();
    private final ArrayList<Rectangle> m_classifierUI.getFullRectangleList() = new ArrayList<>();
    */
    private ClassifierPlayer m_cfPlayer = new ClassifierPlayer();
    private ClassifierDealerButton m_cfDealerButton = new ClassifierDealerButton();
    // private CFResultPlayer m_crPlayer = new CFResultPlayer();
    // private CFResultDealerButton m_crDealerButton = new CFResultDealerButton();

    public PositiveListMakerFormController() {
        System.out.println("in constructor");
    }

    @PackageScope
    void onShow(Image img) throws Exception {
        m_changeAutoSave();
        m_initTableView();
        m_initChoiceBoxTarget();

        if (img != null) {
            m_setImage(img);
        }

        // グローバル設定をロード
        GlobalSettings gs = GlobalSettings.load();
        m_initGlobalSettings(gs);

        // ターゲット毎の設定をロード
        m_chbTargetChanged(null);
    }

    private void m_initChoiceBoxTarget() {
        // Targetチョイスボックス
        ChoiceBox chbTarget = (ChoiceBox) m_scene.lookup("#chbTarget");
        EventHandler<ActionEvent> choiceBoxChanged = this::m_chbTargetChanged;
        chbTarget.addEventHandler(ActionEvent.ACTION, choiceBoxChanged);

        final ObservableList<String> cmbListTarget = FXCollections.observableArrayList();
        cmbListTarget.add("Player");
        cmbListTarget.add("Dealer");
        chbTarget.setItems(cmbListTarget);
        chbTarget.getSelectionModel().select(0);
    }

    @PackageScope
    void setLabel(Label lbl) {
        m_lblStatus = lbl;
    }

    public void setScene(Scene scene) {
        m_scene = scene;
    }

    @FXML
    protected void initialize(URL location, Resources resources) {
        System.out.println("in initialize");
    }

    @FXML
    protected void onClick_paste_button(ActionEvent evt) throws Exception {
        System.out.println("paste button current dir = " + new File(".").getAbsoluteFile());
        m_doPaste();
    }

    @FXML
    protected void onClick_save_button(ActionEvent evt) throws Exception {
        System.out.println("save button current dir = " + new File(".").getAbsoluteFile());
        m_doSaveSettings();
    }

    @FXML
    protected void onClick_detect_button(ActionEvent evt) throws Exception {
        m_classify();
    }

    private void m_doPaste() throws Exception {
        Image img = m_getImage();
        if (m_isAutoSave() && (img != null)) {
            // 変更前の画像とテキストをファイルに保存
            m_saveTextAndImage();
        }
        m_clearWindow();
        // クリップボードの画像を取得
        /****************
         * Clipboard.getImage()では、コピー元のContentTypes()によっては、
         * ImageViewに表示されない（classifyはできているので、画像自体は取得できていると思われる）
         * [[application/x-java-rawimage], [text/html], [cf17]]     ブラウザから「画像をコピー」⇒ ImageViewで表示できる
         * [[application/x-java-rawimage], [cf17]]      Alt + PrintScreenキー ⇒ ImageViewで表示できる
         * [[application/x-java-rawimage], [InShellDragLoop], [ms-stuff/preferred-drop-effect]]  Excelに貼り付けた画像をコピー ⇒ ImageViewで表示できない
         * [[application/x-java-rawimage], [Object Descriptor]] ペイントに貼り付けた画像をコピー ⇒ ImageViewで表示できない
         */
        Clipboard cb = Clipboard.getSystemClipboard();
        System.out.println(cb.getContentTypes());
        Image newImage = cb.getImage();
        m_setImage(newImage);

        m_chbTargetChanged(null);
    }

    private void m_setImage(Image image) throws Exception {
        //image viewの作成
        m_initImageView(image);

        // list view の初期化
        m_clearTableRows();

        // 検出器を動かして検出結果をリストに追加
        m_classify();
    }

    private ObservableList<Rectangle> m_getCurrentTargetItems() {
        System.out.println("m_getCurrentTargetItems curent target index = " + getCurrentTarget().toInt());
        switch (getCurrentTarget()) {
            case Player:
                System.out.println("m_getCurrentTargetItems  = " + m_cfPlayer.getRectangleList().size());
                return m_cfPlayer.getRectangleList();
            case DealerButton:
                System.out.println("m_getCurrentTargetItems  = " + m_cfDealerButton.getRectangleList().size());
                return m_cfDealerButton.getRectangleList();
            default:
                return null;
        }
    }

    @PackageScope
    EnTarget getCurrentTarget() {
        ChoiceBox chb = (ChoiceBox) m_scene.lookup("#chbTarget");
        return EnTarget.fromInt(chb.getSelectionModel().getSelectedIndex());
    }

    private void m_setTableRows(ObservableList items) {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");
        table.setItems(items);
    }

    @FXML
    protected void onClick_undo_button(ActionEvent evt) {
        System.out.println("undo button");
        m_undo();
    }

    @FXML
    protected void onClick_clear_button(ActionEvent evt) {
        System.out.println("clear button");
        m_clearTarget();
    }

    @FXML
    protected void onClick_saveTextAndImage_button(ActionEvent evt) throws IOException, Exception {
        m_saveTextAndImage();
    }

    private void m_saveTextAndImage() throws Exception {
        Classifier cf = getCurrentTargetClassifier();
        if (cf.getRectangleList().size() == 0) {
            System.out.println("!!! no rectangle");
            return;
        }

        File f = saveImage(cf.getDataDir());
        saveText(cf, f.getPath());
    }

    @PackageScope
    void saveText(Classifier cf, String imgPath) throws Exception {
        // テキストリストを保存
        File imgFile = new File(imgPath);
        m_savePositiveListOneLine(cf, m_scene, imgFile.getParent(), imgFile.getName());
    }

    @PackageScope
    File saveImage(String dir) throws Exception {
        Image img = m_getImage();
        if (img == null) return null;
        String picPath = m_getNewPicFileName(m_scene, dir);
        File f = null;
        try {
            // 画像を保存
            f = m_doSaveImg(m_scene, img, picPath, "png");
        } catch (IOException ex) {
            System.out.println("!!! IOException at saveImage " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("!!! Exception at saveImage " + ex.getMessage());
        }
        return f;
    }

    @FXML
    protected void onClick_autoSave(ActionEvent evt) {
        m_changeAutoSave();
    }

    @FXML
    protected void onClick_hideFullRect(ActionEvent evt) {
        m_changeHideFullRect();
    }

    private void m_classify() throws Exception {
        m_clearRectangles();

        // 画像を取得
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        assert (imgView != null);
        javafx.scene.image.Image fxImage = imgView.getImage();
        Pane pane = (Pane) imgView.getParent();

        // Player
        {
            // 検出
            m_cfPlayer.classify(fxImage, this::m_setRectangleEvents);
            m_cfPlayer.setResultToPane(fxImage, pane, m_isFixedSize());
        }

        // DealerButton
        {
            // 検出
            m_cfDealerButton.classify(fxImage, this::m_setRectangleEvents);
            m_cfDealerButton.setResultToPane(fxImage, pane, m_isFixedSize());
        }

        // フルパワーの表示on/off
        m_changeHideFullRect();
        // テーブルに結果をセット
        m_setTableRows(m_getCurrentTargetItems());
    }

    private void m_clearTableRows() {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");
        table.getItems().clear();
    }

    private void m_clearTarget() {
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        Classifier cf = getCurrentTargetClassifier();
        cf.clearResult(pane);
        m_chbTargetChanged(null);
    }

    private void m_clearWindow() {
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        Classifier.clearRectsFromPane(pane);
    }

    private void m_clearRectangles() {
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        m_cfPlayer.clearRectsFromPane(pane);
        m_clearTableRows();
    }

    private void m_changeHideFullRect() {
        CheckBox chkHide = (CheckBox) m_scene.lookup("#cbxHideFullRect");
        boolean isHide = chkHide.isSelected();
        m_cfPlayer.changeHideFullRect(isHide);
        m_cfDealerButton.changeHideFullRect(isHide);
    }

    private void m_undo() {
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        Classifier cf = getCurrentTargetClassifier();
        if (cf.getRectangleList().size() == 0)
            return;

        cf.clearRect(pane, cf.getRectangleList().get(cf.getRectangleList().size()-1));
    }

    @FXML
    protected void onKeyPressed_paneMain(KeyEvent evt) throws Exception {
        System.out.println("キー isControl " + evt.isControlDown());
        System.out.println("キー  text " + evt.getText());
        if (evt.isControlDown() && (evt.getCode() == KeyCode.Z)) {
            Scene scene = ((Pane) evt.getSource()).getScene();
            m_undo();
            return;
        }
        if (evt.isControlDown() && (evt.getCode() == KeyCode.V)) {
            m_doPaste();
        }
    }

    private void m_initImageView(Image img) {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        assert (pane != null);

        if (imgView == null) {
            System.out.println("create new ImageView");
            imgView = new ImageView();
            imgView.setId("imvPic");
            // イメージビューに対するイベントを設定
            m_initImageViewEvent(imgView);

            //Paneにimageviewを載せる
            pane.getChildren().add(imgView);
        }

        // 両脇に黒い領域を追加する
        // WritableImage newImg = m_expandImage(img);
        Image newImg = img;
        imgView.setImage(newImg);
        imgView.setVisible(true);
        // 画面サイズを調整
        pane.setMaxWidth(newImg.getWidth());
        pane.setMaxHeight(newImg.getHeight());
        Window wnd = m_scene.getWindow();
        final int TABLE_WIDTH = 240;
        wnd.setWidth(newImg.getWidth() + TABLE_WIDTH);
        wnd.setHeight(newImg.getHeight() + 145);
        SplitPane sp = (SplitPane) m_scene.lookup("#spImageTable");
        sp.setDividerPosition(0, (newImg.getWidth() + 35) / (newImg.getWidth() + TABLE_WIDTH));
    }

    private void m_initImageViewEvent(ImageView imgView) {
        if (imgView == null) {
            return;
        }

        // マウスダウン
        imgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                assert (m_mousePressEvent == null);
                System.out.println("onMousePressed " + m_getAxisStrFromEvent(evt));
                m_mousePressEvent = evt;
            }
        });

        // ドラッグ開始
        imgView.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressEvent == null) return;
                assert (m_rect == null);
                System.out.println("drag start" + m_getAxisStrFromEvent(evt));
                m_setAllRectangleStroke(true);
                m_rect = m_initRectangle(evt, m_isFixedSize());
                Pane pane = (Pane) ((ImageView) evt.getSource()).getParent();
                pane.getChildren().add(m_rect);
            }
        });

        // ドラッグ中
        imgView.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressEvent == null) return;
                if (m_rect == null) return;

                System.out.println("dragging" + m_getAxisStrFromEvent(evt));
                m_setRectPosAndSize(evt, m_isFixedSize());
            }
        });

        // ドラッグ終了
        imgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressEvent == null) return;
                if (m_rect == null) return;

                System.out.println("drag End" + m_getAxisStrFromEvent(evt));
                m_setRectPosAndSize(evt, m_isFixedSize());
                m_mousePressEvent = null;
                Classifier cf = getCurrentTargetClassifier();
                m_rect.setStroke(cf.getRectangleColor());
                cf.getRectangleList().add(m_rect);
                m_rect = null;
            }
        });
    }

    private WritableImage m_expandImage(Image img) {
        int expandWidth = 100;
        WritableImage newImg = new WritableImage((int) img.getWidth() + expandWidth, (int) img.getHeight());
        PixelWriter pw = newImg.getPixelWriter();
        PixelReader pr = img.getPixelReader();
        // SwingFXUtils.toFXImage(expandedImg, img);
        for (int x = 0; x < newImg.getWidth(); x++) {
            for (int y = 0; y < newImg.getHeight(); y++) {
                Color color;
                if (x < (expandWidth / 2)) {
                    color = Color.BLACK;
                } else if (x >= newImg.getWidth() - (expandWidth / 2)) {
                    color = Color.BLACK;
                } else {
                    // srcのイメージのピクセルを読み込んで、destに書き込む
                    color = pr.getColor(x - expandWidth / 2, y);
                }
                pw.setColor(x, y, color);
            }
        }
        return newImg;
    }

    private void m_setAllRectangleStroke(boolean forceReset) {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");
        Classifier cf = getCurrentTargetClassifier();

        for (int i = 0; i < table.getItems().size(); i++) {
            Rectangle rect = (Rectangle) table.getItems().get(i);
            if (!forceReset && table.getSelectionModel().isSelected(i)) {
                rect.setStroke(Color.AQUA);
            } else {
                rect.setStroke(cf.getRectangleColor());
            }
        }
    }

    private void m_initTableView() {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");

        table.setEditable(true);

        TableColumn colX = new TableColumn("X");
        m_setColumnEditable(colX, "setX");
        TableColumn colY = new TableColumn("Y");
        m_setColumnEditable(colY, "setY");
        TableColumn colWidth = new TableColumn("Width");
        m_setColumnEditable(colWidth, "setWidth");
        TableColumn colHeight = new TableColumn("Height");
        m_setColumnEditable(colHeight, "setHeight");

        ObservableList columns =
                FXCollections.observableArrayList();
        columns.addAll(colX, colY, colWidth, colHeight);
        table.getColumns().clear();
        table.getColumns().addAll(columns);

        colX.setCellValueFactory(
                new PropertyValueFactory<>("X")
        );
        colY.setCellValueFactory(
                new PropertyValueFactory<>("Y")
        );
        colWidth.setCellValueFactory(
                new PropertyValueFactory<>("Width")
        );
        colHeight.setCellValueFactory(
                new PropertyValueFactory<>("Height")
        );

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) ->
        {
            Rectangle rect = (Rectangle) newVal;
            if (rect == null) return;
            m_setAllRectangleStroke(false);
        });

        table.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (newPropertyValue) {
                    m_setAllRectangleStroke(false);
                } else {
                    m_setAllRectangleStroke(true);
                }
            }
        });
    }

    private void m_setColumnEditable(TableColumn col, String mName) {
        col.setEditable(true);
        col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Double newVal = (Double) event.getNewValue();
                Rectangle rowRect = (Rectangle) event.getRowValue();
                try {
                    Method m = Rectangle.class.getMethod(mName, double.class);
                    m.invoke(rowRect, newVal);
                    rowRect.setStroke(Color.RED);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void m_setRectPosAndSize(MouseEvent evt, boolean isFixedSize) {
        if (m_rect == null) return;
        if (m_mousePressEvent == null) return;
        ImageView imgView = (ImageView) evt.getSource();
        Classifier cf = getCurrentTargetClassifier();

        if (isFixedSize) {
            m_rect.setX(Math.min(imgView.getImage().getWidth() - cf.getRectWidth(), Math.max(0, evt.getX())));
            m_rect.setY(Math.min(imgView.getImage().getHeight() - cf.getRectHeight(), Math.max(0, evt.getY())));
        } else {
            m_rect.setX(Math.min(imgView.getImage().getWidth(), Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX()))));
            m_rect.setY(Math.min(imgView.getImage().getHeight(), Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY()))));
            m_rect.setWidth(Math.min(imgView.getImage().getWidth() - 1 - m_rect.getX(), Math.abs(m_mousePressEvent.getX() - evt.getX())));
            m_rect.setHeight(Math.min(imgView.getImage().getHeight() - 1 - m_rect.getY(), Math.abs(m_mousePressEvent.getY() - evt.getY())));
        }
    }

    private void m_setRectPosAndSize_moveRect(MouseEvent evt, Rectangle rect) {
        if (m_mousePressPos == null) return;
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        Classifier cf = getCurrentTargetClassifier();

        rect.setX(Math.min(imgView.getImage().getWidth() - cf.getRectWidth(), Math.max(0, evt.getX() + m_mousePressPos.x)));
        rect.setY(Math.min(imgView.getImage().getHeight() - cf.getRectHeight(), Math.max(0, evt.getY() + m_mousePressPos.y)));
    }

    private Rectangle m_initRectangle(MouseEvent evt, boolean isFixedSize) {
        double x;
        double y;
        double width;
        double height;
        Image img = ((ImageView) evt.getSource()).getImage();
        Classifier cf = getCurrentTargetClassifier();

        if (isFixedSize) {
            x = Math.min(img.getWidth() - cf.getRectWidth(), Math.max(0, evt.getX()));
            y = Math.min(img.getHeight() - cf.getRectHeight(), Math.max(0, evt.getY()));
            width = cf.getRectWidth();
            height = cf.getRectHeight();
        } else {
            x = Math.min(img.getWidth(), Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX())));
            y = Math.min(img.getHeight(), Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY())));
            width = Math.min(img.getWidth() - 1 - Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX())), Math.abs(m_mousePressEvent.getX() - evt.getX()));
            height = Math.min(img.getHeight() - 1 - Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY())), Math.abs(m_mousePressEvent.getY() - evt.getY()));
        }

        Rectangle newRect = new Rectangle(x, y, width, height);
        newRect.setFill(Color.TRANSPARENT);
        newRect.setStroke(Color.AQUA);
        // マウスイベントを透過させる
        // newRect.setMouseTransparent(true);
        // イベントを設定する
        m_setRectangleEvents(newRect);

        return newRect;
    }

    private void m_setRectangleEvents(Rectangle newRect) {
        // マウスダウン
        newRect.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                assert (m_mousePressPos == null);
                System.out.println("mouse press rect start" + m_getAxisStrFromEvent(evt));
                m_mousePressPos = new RectPos(newRect.getX() - evt.getX(), newRect.getY() - evt.getY());
                m_setAllRectangleStroke(true);
                newRect.setStroke(Color.AQUA);
            }
        });

        // ドラッグ開始
        newRect.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return;
                System.out.println("drag rect start" + m_getAxisStrFromEvent(evt));
            }
        });

        // ドラッグ中
        newRect.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return;

                System.out.println("dragging rect " + m_getAxisStrFromEvent(evt));
                m_setRectPosAndSize_moveRect(evt, newRect);
            }
        });

        // ドラッグ終了
        newRect.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return;

                System.out.println("drag End rect" + m_getAxisStrFromEvent(evt));
                m_setRectPosAndSize_moveRect(evt, newRect);
                newRect.setStroke(getCurrentTargetClassifier().getRectangleColor());
                m_mousePressPos = null;
                TableView table = (TableView) m_scene.lookup("#tblRectangles");
                table.getItems().set(table.getItems().indexOf(newRect), newRect);
            }
        });
    }

    private String m_getAxisStrFromEvent(MouseEvent evt) {
        return String.format("x = %f, y = %f", evt.getX(), evt.getY());
    }

    private File m_doSaveImg(Scene scene, Image img, String path, String fmt) throws java.io.IOException {
        File f = new File(path);
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), fmt, f);
        return f;
    }

    private void m_doSaveSettings() {
        try {
            CFSettings cs;
            switch (getCurrentTarget()) {
                case Player:
                    cs = m_createClassifierSettingsFromGUI_player();
                    break;
                case DealerButton:
                    cs = m_createClassifierSettingsFromGUI_dealerButton();
                    break;
                default:
                    assert (false);
                    return;
            }
            cs.save();
            GlobalSettings gs = m_createGlobalSettingsFromGUI();
            gs.save();
        } catch (java.io.IOException ex) {
            System.out.println(ex.toString());
        }
    }

    private String m_getNewPicFileName(Scene scene, String dir) throws Exception {
        String fileNameFmt = "positive_%d.png";
        for (int i = 1; i < 10000; i++) {
            File f = new File(dir, String.format(fileNameFmt, i));
            if (!f.exists()) {
                System.out.println("save file " + f.getAbsolutePath());
                return f.getPath();
            }
        }
        throw new Exception("can't make new pic filename");
    }

    public Classifier getCurrentTargetClassifier() {
        EnTarget t = getCurrentTarget();
        return m_getTargetClassifier(t);
    }

    private Classifier m_getTargetClassifier(EnTarget t)
    {
        switch(t) {
            case Player:
                return m_cfPlayer;
            case DealerButton:
                return m_cfDealerButton;
            default:
               assert(false);
               return null;
        }
    }

    private void m_savePositiveListOneLine(Classifier cf, Scene scene, String dir, String picPath) throws IOException {
        final String POS_LIST_FILE_NAME = "pos_list.txt";
        StringBuilder sb = new StringBuilder();
        File txtFile = new File(dir, POS_LIST_FILE_NAME);
        // 読む
        try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
            String str;
            while((str = br.readLine()) != null){
                sb.append(String.format("%s\n", str));
            }
        }catch(FileNotFoundException e) {
            System.out.println(e);
        }

        // 書く
        try (FileWriter filewriter = new FileWriter(txtFile, false)) {
            filewriter.write(String.format("%s\n", cf.getPosListStr(picPath)));
            filewriter.write(sb.toString());
        }
    }

    private boolean m_isAutoSave() {
        CheckBox cbxAutoSave = (CheckBox)m_scene.lookup("#cbxAutoSave");
        return cbxAutoSave.isSelected();
    }

    private boolean m_isFixedSize() {
        CheckBox cbxFixedSize = (CheckBox)m_scene.lookup("#cbxFixedSize");
        return cbxFixedSize.isSelected();
    }

    @PackageScope
    boolean isAutoSave() {
        CheckBox cbxAutoSave = (CheckBox)m_scene.lookup("#cbxAutoSave");
        return cbxAutoSave.isSelected();
    }

    private void m_changeAutoSave()
    {
        Button btnSaveTxt = (Button)m_scene.lookup("#btnSaveTxtImg");
        btnSaveTxt.setDisable(isAutoSave());
    }

    private void m_chbTargetChanged(ActionEvent event)
    {
        System.out.println(" in m_chbTargetChanged");
        CFSettings cs = new CFSettingsPlayer();
        Classifier cf = null;
        Classifier otherCf = null;
        try {
            switch (getCurrentTarget()) {
                case Player:
                    cs = CFSettingsPlayer.load();
                    cf = m_cfPlayer;
                    otherCf = m_cfDealerButton;
                    break;
                case DealerButton:
                    cs = CFSettingsDealerButton.load();
                    cf = m_cfDealerButton;
                    otherCf = m_cfPlayer;
                    break;
                default:
                    assert (false);
                    return;
            }
        } catch(IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        // 設定をロード
        m_initParameterSettings(cs);

        // list view の初期化
        m_setTableRows(m_getCurrentTargetItems());

        // 矩形をクリック可能か否かを変える
        cf.setRectClickable(true);
        otherCf.setRectClickable(false);

        // イメージビューに対するイベントを設定
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        m_initImageViewEvent(imgView);
    }

    private int m_getCurrentFeatureTypeIndex()
    {
        ChoiceBox chbFeatureType = (ChoiceBox)m_scene.lookup("#chbFeatureType");
        return chbFeatureType.getSelectionModel().getSelectedIndex();
    }

    private void m_initGlobalSettings(GlobalSettings gs) {
        int targetIndex = gs.getSelectedTargetIndex();
        ChoiceBox chbTarget = (ChoiceBox) m_scene.lookup("#chbTarget");
        if (targetIndex < 0  || targetIndex >= chbTarget.getItems().size()) {
            return;
        }
        chbTarget.getSelectionModel().select(targetIndex);

        boolean isHideFullRect = gs.getIsHideFullRect();
        CheckBox chkHide = (CheckBox) m_scene.lookup("#cbxHideFullRect");
        chkHide.setSelected(isHideFullRect);
        m_changeHideFullRect();
    }

    private void m_initParameterSettings(CFSettings cs)
    {
        // FeatureTypeチョイスボックス
        ChoiceBox chbFeatureType = (ChoiceBox)m_scene.lookup("#chbFeatureType");

        final ObservableList<String> cmbList = FXCollections.observableArrayList();
        cmbList.add("HAAR");
        cmbList.add("LBP");
        chbFeatureType.setItems(cmbList);
        chbFeatureType.getSelectionModel().select(cs.featureTypeIndex);

        // 各種パラメータ
        TextField txtMinNeighbors = (TextField)m_scene.lookup("#txtMinNeighbors");
        txtMinNeighbors.setText(String.valueOf(cs.minNeighbors));
        TextField txtScaleFactor = (TextField)m_scene.lookup("#txtScaleFactor");
        txtScaleFactor.setText(String.valueOf(cs.scaleFactor));
        TextField txtMinSizeWidth = (TextField)m_scene.lookup("#txtMinSizeWidth");
        txtMinSizeWidth.setText(String.valueOf(cs.getMinSize().width));
        TextField txtMinSizeHeight = (TextField)m_scene.lookup("#txtMinSizeHeight");
        txtMinSizeHeight.setText(String.valueOf(cs.getMinSize().height));
        TextField txtMaxSizeWidth = (TextField)m_scene.lookup("#txtMaxSizeWidth");
        txtMaxSizeWidth.setText(String.valueOf(cs.getMaxSize().width));
        TextField txtMaxSizeHeight = (TextField)m_scene.lookup("#txtMaxSizeHeight");
        txtMaxSizeHeight.setText(String.valueOf(cs.getMaxSize().height));
    }

    private CFSettings m_createClassifierSettingsFromGUI_player()
    {
        TextField txtMinNeighbors = (TextField)m_scene.lookup("#txtMinNeighbors");
        TextField txtScaleFactor = (TextField)m_scene.lookup("#txtScaleFactor");
        TextField txtMinSizeWidth = (TextField)m_scene.lookup("#txtMinSizeWidth");
        TextField txtMinSizeHeight = (TextField)m_scene.lookup("#txtMinSizeHeight");
        TextField txtMaxSizeWidth = (TextField)m_scene.lookup("#txtMaxSizeWidth");
        TextField txtMaxSizeHeight = (TextField)m_scene.lookup("#txtMaxSizeHeight");

        CFSettingsPlayer cs = new CFSettingsPlayer();
        cs.featureTypeIndex = m_getCurrentFeatureTypeIndex();
        cs.minNeighbors = Integer.parseInt(txtMinNeighbors.getText());
        cs.scaleFactor = Double.parseDouble(txtScaleFactor.getText());
        cs.setMinSize(new Size(Double.parseDouble(txtMinSizeWidth.getText()), Double.parseDouble(txtMinSizeHeight.getText())));
        cs.setMaxSize(new Size(Double.parseDouble(txtMaxSizeWidth.getText()), Double.parseDouble(txtMaxSizeHeight.getText())));

        return cs;
    }

    private CFSettings m_createClassifierSettingsFromGUI_dealerButton()
    {
        TextField txtMinNeighbors = (TextField)m_scene.lookup("#txtMinNeighbors");
        TextField txtScaleFactor = (TextField)m_scene.lookup("#txtScaleFactor");
        TextField txtMinSizeWidth = (TextField)m_scene.lookup("#txtMinSizeWidth");
        TextField txtMinSizeHeight = (TextField)m_scene.lookup("#txtMinSizeHeight");
        TextField txtMaxSizeWidth = (TextField)m_scene.lookup("#txtMaxSizeWidth");
        TextField txtMaxSizeHeight = (TextField)m_scene.lookup("#txtMaxSizeHeight");

        CFSettingsDealerButton cs = new CFSettingsDealerButton();
        cs.featureTypeIndex = m_getCurrentFeatureTypeIndex();
        cs.minNeighbors = Integer.parseInt(txtMinNeighbors.getText());
        cs.scaleFactor = Double.parseDouble(txtScaleFactor.getText());
        cs.setMinSize(new Size(Double.parseDouble(txtMinSizeWidth.getText()), Double.parseDouble(txtMinSizeHeight.getText())));
        cs.setMaxSize(new Size(Double.parseDouble(txtMaxSizeWidth.getText()), Double.parseDouble(txtMaxSizeHeight.getText())));

        return cs;
    }

    private GlobalSettings m_createGlobalSettingsFromGUI()
    {
        GlobalSettings gs = new GlobalSettings();
        gs.setSelectedTargetIndex(getCurrentTarget().toInt());

        CheckBox chkHide = (CheckBox) m_scene.lookup("#cbxHideFullRect");
        gs.setIsHideFullRect(chkHide.isSelected());

        return gs;
    }


    private Image m_getImage() {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        if (imgView == null) return null;
        return imgView.getImage();
    }
}
