package application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.scene.shape.Rectangle;
import opencv_client.CascadeClassify;
import org.opencv.core.Rect;

import javax.annotation.Resources;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

public class MainFormController {
    private Label m_lblStatus = null;
    private MouseEvent m_startEvent = null;
    private Rectangle m_rect = null;
    private Scene m_scene = null;
    private String m_imgPath = null;

    private final ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList(
    );

    public void setLabel(Label lbl) {
        m_lblStatus = lbl;
    }

    public void setScene(Scene scene) {
        m_scene = scene;
    }

    public MainFormController() {
        System.out.println("in constructor");
    }
    @FXML
    protected void initialize(URL location, Resources resources)
    {
        System.out.println("in initialize");
    }

    @FXML
    protected void onClick_paste_button(ActionEvent evt) throws Exception {
        System.out.println("paste button current dir = " + new File(".").getAbsoluteFile());
        Scene scene = ((Button)evt.getSource()).getScene();
        m_doPaste(scene);
    }

    private void m_doPaste(Scene scene) throws Exception {
        // 変更前のtextをファイルに保存
        if (m_isAutoSave(scene)) {
            saveText();
        }
        m_clearWindow(scene);
        // クリップボードの画像を取得
        Clipboard cb = Clipboard.getSystemClipboard();
        System.out.println(cb.getString());
        System.out.println(cb.getContentTypes());
        Image image = cb.getImage();
        //image viewの作成
        m_initImageView(scene, image);

        // list view の初期化
        m_initTableView(scene);

        // 変更後の画像をファイルに保存
        if (m_isAutoSave(scene)) {
            File f = saveImg();
            if (f == null) throw new Exception("!!! failed at save img file");
            m_imgPath = f.getPath();
        }
    }

    @FXML
    protected void onClick_load_button(ActionEvent evt) throws Exception {
        System.out.println("load button");
        if (m_isAutoSave(m_scene)) {
            saveText();
        }
        m_clearWindow(m_scene);
        // 画像ファイルを表示
        //imageの読み込み
        Image image = new Image( "file:d:/temp/160414_034715.jpg" );
        //image viewの作成
        m_initImageView(m_scene, image);

        // list view の初期化
        m_initTableView(m_scene);
    }

    @FXML
    protected void onClick_undo_button(ActionEvent evt) {
        System.out.println("undo button");
        Scene scene = ((Button)evt.getSource()).getScene();
        m_undo(scene);
    }

    @FXML
    protected void onClick_clear_button(ActionEvent evt) {
        System.out.println("clear button");
        m_clearWindow(m_scene);
    }

    @FXML
    protected void onClick_saveTxt_button(ActionEvent evt) throws IOException, Exception {
        saveText();
    }

    @FXML
    protected void onClick_saveImg_button(ActionEvent evt) throws IOException, Exception {
        saveImg();
    }

    public void saveText() throws Exception {
        if (m_imgPath == null) {
            System.out.println("!!! no imageFile");
            return;
        }
        if (m_rectangleList.size() == 0) {
            System.out.println("!!! no rectangle");
            return;
        }

        // テキストリストを保存
        m_savePositiveListOneLine(m_scene, m_getDataDir(), (new File(m_imgPath).getName()));
    }

    private File saveImg() throws Exception {
        final String dir = m_getDataDir();
        String picPath = m_getNewPicFileName(m_scene, dir);
        File f = null;
        try {
            // 画像を保存
            f = m_doSaveImg(m_scene, picPath, "png");
        }
        catch (IOException ex) {
            System.out.println("!!! IOException at saveImg " + ex.getMessage());
        }
        catch (Exception ex) {
            System.out.println("!!! Exception at saveImg " + ex.getMessage());
        }
        return f;
    }

    private String m_getDataDir() {
        return ((TextField) m_scene.lookup("#txtPosDir")).getText();
    }

    @FXML
    protected void onClick_autoSave(ActionEvent evt) {
        Scene scene = ((CheckBox)evt.getSource()).getScene();
        changeAutoSave(scene);
    }

    @FXML
    protected void onClick_classify(ActionEvent evt) {
        CascadeClassify cc = new CascadeClassify();
        Rect[] rects = cc.classify(m_imgPath);
        Pane pane = (Pane)((Button)evt.getSource()).getScene().lookup("#paneAnchorImage");
        for (int i = 0; i < rects.length; i++) {
            Rectangle newRect = m_rectToRectanble(rects[i]);
            pane.getChildren().add(newRect);
            m_rectangleList.add(newRect);
        }
    }

    private void m_clearWindow(Scene scene) {
        m_rectangleList.clear();
        //Paneにimageviewを載せる
        Pane pane = (Pane) scene.lookup("#paneAnchorImage");
        pane.getChildren().clear();

        Window wnd = scene.getWindow();
        wnd.setWidth(640);
        wnd.setHeight(480);
    }

    private void m_undo(Scene scene) {
        Pane pane = (Pane) scene.lookup("#paneAnchorImage");
        if (pane.getChildren().size() == 0)
            return;
        Node lastNode = pane.getChildren().get(pane.getChildren().size()-1);
        if (!(lastNode instanceof Rectangle))
            return;
        pane.getChildren().remove(lastNode);
        m_rectangleList.remove(lastNode);
    }

    @FXML
    protected void onKeyPressed_paneMain(KeyEvent evt) throws Exception {
        System.out.println("キー isControl " + evt.isControlDown());
        System.out.println("キー  text " + evt.getText());
        if (evt.isControlDown() && (evt.getCode() == KeyCode.Z)) {
            Scene scene = ((Pane)evt.getSource()).getScene();
            m_undo(scene);
            return;
        }
        if (evt.isControlDown() && (evt.getCode() == KeyCode.V)) {
            Scene scene = ((Pane)evt.getSource()).getScene();
            m_doPaste(scene);
        }
    }

    private void m_initImageView(Scene scene, Image img) {
        ImageView imgView = (ImageView)scene.lookup("#imvPic");

        if (imgView != null) {
            imgView.setImage(img);
        }
        else {
            imgView = new ImageView( img );
            imgView.setId("imvPic");
            // マウスムーブ
            imgView.setOnMouseMoved( new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    m_lblStatus.setText(m_getAxisStrFromEvent(evt));
                }
            });

            // マウスダウン
            imgView.setOnMousePressed( new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    assert(m_startEvent == null);
                    System.out.println("onMousePressed " + m_getAxisStrFromEvent(evt));
                    m_startEvent = evt;
                }
            });

            // マウスクリック
            imgView.setOnMouseClicked( new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    System.out.println("onMouseClicked " + m_getAxisStrFromEvent(evt));
                }
            });

            // ドラッグ開始
            imgView.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    if (m_startEvent == null) return ;
                    assert(m_rect == null);
                    System.out.println("drag start" + m_getAxisStrFromEvent(evt));
                    Pane pane = (Pane)((ImageView)evt.getSource()).getParent();
                    m_rect = m_initRectangle(evt, pane);
                    pane.getChildren().add(m_rect);
//                    evt.consume();
                }
            });

            // ドラッグ中
            imgView.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    if (m_startEvent == null) return ;
                    if (m_rect == null) return ;

                    System.out.println("dragging" + m_getAxisStrFromEvent(evt));
                    m_setRectPosAndSize(evt);
                }
            });

            // ドラッグ終了
            imgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    if (m_startEvent == null) return ;
                    if (m_rect == null) return ;

                    System.out.println("drag End" + m_getAxisStrFromEvent(evt));
                    m_setRectPosAndSize(evt);
                    m_rect.setStroke(Color.RED);
                    m_startEvent = null;
                    m_rectangleList.add(m_rect);
                    m_rect = null;
//                    evt.consume();
                }
            });

            //Paneにimageviewを載せる
            Pane pane = (Pane) scene.lookup("#paneAnchorImage");
            assert(pane != null);
            pane.getChildren().add( imgView );
        }

        Window wnd = scene.getWindow();
        wnd.setWidth(img.getWidth() + 128);
        wnd.setHeight(img.getHeight() + 90);
    }

    private void m_initTableView(Scene scene) {
        TableView table = (TableView)scene.lookup("#tblRectangles");

        table.setEditable(true);

        TableColumn colX = new TableColumn("X");
        TableColumn colY = new TableColumn("Y");
        TableColumn colWidth = new TableColumn("Width");
        TableColumn colHeight = new TableColumn("Height");
        ObservableList columns =
                FXCollections.observableArrayList();
        columns.addAll(colX , colY, colWidth, colHeight);
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
        table.setItems(m_rectangleList);
    }

    private void m_setRectPosAndSize(MouseEvent evt) {
        if (m_rect == null) return;
        if (m_startEvent == null) return;
        Pane pane = (Pane)((ImageView)evt.getSource()).getParent();

        m_rect.setX(Math.max(0, Math.min(m_startEvent.getX(), evt.getX())));
        m_rect.setY(Math.max(0, Math.min(m_startEvent.getY(), evt.getY())));

        m_rect.setWidth(Math.min(pane.getWidth() - 1 - m_rect.getX(), Math.abs(m_startEvent.getX() - evt.getX())));
        m_rect.setHeight(Math.min(pane.getHeight() - 1 - m_rect.getY(), Math.abs(m_startEvent.getY() - evt.getY())));
    }

    private Rectangle m_initRectangle(MouseEvent evt, Pane pane) {
        Rectangle newRect = new Rectangle(
                Math.max(0, Math.min(m_startEvent.getX(), evt.getX())),
                Math.max(0, Math.min(m_startEvent.getY(), evt.getY())),
                Math.min(pane.getWidth() - 1 - Math.max(0, Math.min(m_startEvent.getX(), evt.getX())), Math.abs(m_startEvent.getX() - evt.getX())),
                Math.min(pane.getHeight() - 1 - Math.max(0, Math.min(m_startEvent.getY(), evt.getY())), Math.abs(m_startEvent.getY() - evt.getY())));
        newRect.setFill(Color.TRANSPARENT);
        newRect.setStroke(Color.BLUE);
        // マウスイベントを透過させる
        newRect.setMouseTransparent(true);

        return newRect;
    }

    private Rectangle m_rectToRectanble(Rect r) {
        Rectangle result = new Rectangle(r.x, r.y, r.width, r.height);
        result.setFill(Color.TRANSPARENT);
        result.setStroke(Color.RED);
        result.setMouseTransparent(true);
        return result;
    }

    private String m_getAxisStrFromEvent(MouseEvent evt) {
        return String.format("x = %f, y = %f", evt.getX(), evt.getY());
    }

    private File m_doSaveImg(Scene scene, String path, String fmt) throws java.io.IOException {
        ImageView imgView = (ImageView) scene.lookup("#imvPic");
        if (imgView == null) return null;
        Image img = imgView.getImage();
        File f = new File(path);
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), fmt, f);
        return f;
    }

    private String m_getNewPicFileName(Scene scene, String dir) throws Exception {
        String fileNameFmt = "positive_%d.png";
        for(int i = 1; i < 10000; i++) {
            File f = new File(dir, String.format(fileNameFmt, i));
            if (!f.exists()) {
                System.out.println("save file " + f.getAbsolutePath());
                return f.getPath();
            }
        }
        throw new Exception("can't make new pic filename");
    }

    private void m_savePositiveListOneLine(Scene scene, String dir, String picPath) throws IOException {
        final String POS_LIST_FILE_NAME = "pos_list.txt";

        File txtFile = new File(dir, POS_LIST_FILE_NAME);
        try (FileWriter filewriter = new FileWriter(txtFile, true)) {
            filewriter.write(String.format("%s\n", m_getPosListStr(picPath)));
        }
    }

    private String m_getPosListStr(String picPath) {
        StringBuilder sb = new StringBuilder();
        sb.append(picPath);
        sb.append("\t");
        sb.append(m_rectangleList.size());
        sb.append("\t");
        m_rectangleList.forEach(r -> {
            sb.append(String.format("%d %d %d %d\t",
                    Math.round(Math.ceil(r.getX())),
                    Math.round(Math.ceil(r.getY())),
                    Math.round(Math.ceil(r.getWidth())),
                    Math.round(Math.ceil(r.getHeight()))));
        });

        return sb.toString();
    }

    private boolean m_isAutoSave(Scene scene) {
        CheckBox cbxAutoSave = (CheckBox)scene.lookup("#cbxAutoSave");
        return cbxAutoSave.isSelected();
    }

    public void changeAutoSave(Scene scene)
    {
        CheckBox cbxAutoSave = (CheckBox)scene.lookup("#cbxAutoSave");
        Button btnSaveImg = (Button)scene.lookup("#btnSaveImg");
        btnSaveImg.setDisable(cbxAutoSave.isSelected());
        Button btnSaveTxt = (Button)scene.lookup("#btnSaveTxt");
        btnSaveTxt.setDisable(cbxAutoSave.isSelected());
    }
}
