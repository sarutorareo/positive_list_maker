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
        if (m_isAutoSave(scene)) {
            doSave();
        }
        m_clearWindow(evt);
        // クリップボードの画像を取得
        Clipboard cb = Clipboard.getSystemClipboard();
        System.out.println(cb.getString());
        System.out.println(cb.getContentTypes());
        Image image = cb.getImage();
        //image viewの作成
        m_initImageView(scene, image);

        // list view の初期化
        m_initTableView(scene);
    }

    @FXML
    protected void onClick_load_button(ActionEvent evt) throws Exception {
        System.out.println("load button");
        if (m_isAutoSave(m_scene)) {
            doSave();
        }
        m_clearWindow(evt);
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
        m_clearWindow(evt);
    }

    @FXML
    protected void onClick_save_button(ActionEvent evt) throws IOException, Exception {
        doSave();
    }

    public void doSave() throws Exception {
        if (m_rectangleList.size() == 0) {
            System.out.println("!!! no rectangle");
            return;
        }

        TextField txtPosDir = (TextField) m_scene.lookup("#txtPosDir");
        String dir = txtPosDir.getText();
        String picPath = m_getNewPicFileName(m_scene, dir);
        try {
            // 画像を保存
            m_savePic(m_scene, picPath, "png");
        }
        catch (IOException ex) {
            System.out.println("!!! IOException at m_savePic " + ex.getMessage());
        }
        catch (Exception ex) {
            System.out.println("!!! Exception at m_savePic " + ex.getMessage());
        }

        // テキストリストを保存
        m_savePositiveListOneLine(m_scene, dir, picPath);
    }

    @FXML
    protected void onClick_autoSave(ActionEvent evt) {
        Scene scene = ((CheckBox)evt.getSource()).getScene();
        changeAutoSave(scene);
    }

    private void m_clearWindow(ActionEvent evt) {
        m_rectangleList.clear();
        Scene scene = ((Button)evt.getSource()).getScene();
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
    protected void onKeyPressed_paneMain(KeyEvent evt) {
        System.out.println("キー isControl " + evt.isControlDown());
        System.out.println("キー  text " + evt.getText());
        if (evt.isControlDown() && (evt.getCode() == KeyCode.Z)) {
            Scene scene = ((Pane)evt.getSource()).getScene();
            m_undo(scene);
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
                    m_initRectangle(evt);
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

        m_rect.setX(Math.min(m_startEvent.getX(), evt.getX()));
        m_rect.setY(Math.min(m_startEvent.getY(), evt.getY()));
        m_rect.setWidth(Math.abs(m_startEvent.getX() - evt.getX()));
        m_rect.setHeight(Math.abs(m_startEvent.getY() - evt.getY()));
    }

    private void m_initRectangle(MouseEvent evt) {
        if (m_startEvent == null) return;
        assert(m_rect == null);

        Pane pane = (Pane)((ImageView)evt.getSource()).getParent();
        m_rect = new Rectangle(
                Math.max(0, Math.min(m_startEvent.getX(), evt.getX())),
                Math.max(0, Math.min(m_startEvent.getY(), evt.getY())),
                Math.min(pane.getWidth() - 1, Math.abs(m_startEvent.getX() - evt.getX())),
                Math.min(pane.getHeight() - 1, Math.abs(m_startEvent.getY() - evt.getY())));
        m_rect.setFill(Color.TRANSPARENT);
        m_rect.setStroke(Color.BLUE);
        // マウスイベントを透過させる
        m_rect.setMouseTransparent(true);
        pane.getChildren().add(m_rect);
    }

    private String m_getAxisStrFromEvent(MouseEvent evt) {
        return String.format("x = %f, y = %f", evt.getX(), evt.getY());
    }

    private void m_savePic(Scene scene, String path, String fmt) throws java.io.IOException {
        ImageView imgView = (ImageView) scene.lookup("#imvPic");
        if (imgView == null) return;
        Image img = imgView.getImage();
        File f = new File(path);
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), fmt, f);
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
            filewriter.write(String.format("%s\n", m_getPosListLine(picPath)));
        }
    }

    private String m_getPosListLine(String picPath) {
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
        Button btnSave = (Button)scene.lookup("#btnSave");

        btnSave.setDisable(cbxAutoSave.isSelected());
    }
}
