package application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.scene.shape.Rectangle;
import javafx.util.converter.DoubleStringConverter;
import opencv_client.CascadeClassify;
import org.opencv.core.Rect;

import javax.annotation.Resources;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

public class MainFormController {
    private Label m_lblStatus = null;
    private MouseEvent m_mousePressEvent = null;
    private RectPos m_mousePressPos = null;
    private Rectangle m_rect = null;
    private Scene m_scene = null;
    private String m_imgPath = null;
    private final int RECT_WIDTH = 175;
    private final int RECT_HEIGHT = 70;
    private final int TABLE_WIDTH = 240;

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
        if (m_isAutoSave()) {
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

        if (m_isAutoSave()) {
            // 変更後の画像をファイルに保存
            File f = saveImg();
            if (f == null) throw new Exception("!!! failed at save img file");
            m_imgPath = f.getPath();
            // 検出器を動かして検出結果をリストに追加
            m_classify(scene);
        }
    }

    @FXML
    protected void onClick_load_button(ActionEvent evt) throws Exception {
        System.out.println("load button");
        if (m_isAutoSave()) {
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
        m_classify(((Button)evt.getSource()).getScene());
    }

    private void m_classify(Scene scene) {
        CascadeClassify cc = new CascadeClassify();
        Rect[] rects = cc.classify(m_imgPath);

        Pane pane = (Pane)scene.lookup("#paneAnchorImage");
        for (int i = 0; i < rects.length; i++) {
            Rectangle newRect = m_rectToRectanble(rects[i]);
            if (m_isFixedSize()) {
                newRect.setWidth(RECT_WIDTH);
                newRect.setHeight(RECT_HEIGHT);
            }
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
                    assert(m_mousePressEvent == null);
                    System.out.println("onMousePressed " + m_getAxisStrFromEvent(evt));
                    m_mousePressEvent = evt;
                }
            });

            // ドラッグ開始
            imgView.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    if (m_mousePressEvent == null) return ;
                    assert(m_rect == null);
                    System.out.println("drag start" + m_getAxisStrFromEvent(evt));
                    m_setAllRectangleStroke(true);
                    Pane pane = (Pane)((ImageView)evt.getSource()).getParent();
                    m_rect = m_initRectangle(evt, pane, m_isFixedSize());
                    pane.getChildren().add(m_rect);
//                    evt.consume();
                }
            });

            // ドラッグ中
            imgView.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    if (m_mousePressEvent == null) return ;
                    if (m_rect == null) return ;

                    System.out.println("dragging" + m_getAxisStrFromEvent(evt));
                    m_setRectPosAndSize(evt, m_isFixedSize());
                }
            });

            // ドラッグ終了
            imgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent evt) {
                    if (m_mousePressEvent == null) return ;
                    if (m_rect == null) return ;

                    System.out.println("drag End" + m_getAxisStrFromEvent(evt));
                    m_setRectPosAndSize(evt, m_isFixedSize());
                    m_rect.setStroke(Color.RED);
                    m_mousePressEvent = null;
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
        wnd.setWidth(img.getWidth() + TABLE_WIDTH);
        wnd.setHeight(img.getHeight() + 100);
        SplitPane sp = (SplitPane)scene.lookup("#spImageTable");
        sp.setDividerPosition(0,(img.getWidth() + 35) / (img.getWidth() + TABLE_WIDTH));
    }

    private void m_setAllRectangleStroke(boolean forceReset) {
        TableView table = (TableView)m_scene.lookup("#tblRectangles");

        for(int i = 0; i < table.getItems().size(); i++)
        {
            Rectangle rect = (Rectangle)table.getItems().get(i);
            if (!forceReset && table.getSelectionModel().isSelected(i)) {
                rect.setStroke(Color.AQUA);
            }
            else {
                rect.setStroke(Color.RED);
            }
        }
    }

    private void m_initTableView(Scene scene) {
        TableView table = (TableView)scene.lookup("#tblRectangles");

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

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) ->
        {
            Rectangle rect = (Rectangle)newVal;
            if (rect == null) return;
            m_setAllRectangleStroke(false);
        });

        table.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                {
                    m_setAllRectangleStroke(false);
                }
                else
                {
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
                Double newVal = (Double)event.getNewValue();
                Rectangle rowRect = (Rectangle)event.getRowValue();
                try {
                    Method m = Rectangle.class.getMethod(mName, double.class);
                    m.invoke(rowRect, newVal);
                    rowRect.setStroke(Color.RED);
                }
                catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        });
        /*
        col.setOnEditStart(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                if (m_isFixedSize()) {
                    Rectangle rowRect = (Rectangle)event.getRowValue();
                    rowRect.setWidth(RECT_WIDTH);
                    rowRect.setHeight(RECT_HEIGHT);
                }
            }
        });
        */
    }

    private void m_setRectPosAndSize(MouseEvent evt, boolean isFixedSize) {
        if (m_rect == null) return;
        if (m_mousePressEvent == null) return;
        Pane pane = (Pane)((ImageView)evt.getSource()).getParent();

        if (isFixedSize) {
            m_rect.setX(Math.min(pane.getWidth(), Math.max(0, evt.getX())));
            m_rect.setY(Math.min(pane.getHeight(), Math.max(0, evt.getY())));
        }
        else {
            m_rect.setX(Math.min(pane.getWidth(), Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX()))));
            m_rect.setY(Math.min(pane.getHeight(), Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY()))));
            m_rect.setWidth(Math.min(pane.getWidth() - 1 - m_rect.getX(), Math.abs(m_mousePressEvent.getX() - evt.getX())));
            m_rect.setHeight(Math.min(pane.getHeight() - 1 - m_rect.getY(), Math.abs(m_mousePressEvent.getY() - evt.getY())));
        }
    }

    private void m_setRectPosAndSize_moveRect(MouseEvent evt, Rectangle rect) {
        if (m_mousePressPos == null) return;
        Pane pane = (Pane)((Rectangle)evt.getSource()).getParent();

        rect.setX(Math.min(pane.getWidth(), Math.max(0, evt.getX() + m_mousePressPos.x)));
        rect.setY(Math.min(pane.getHeight(), Math.max(0, evt.getY() + m_mousePressPos.y)));
    }

    private Rectangle m_initRectangle(MouseEvent evt, Pane pane, boolean isFixedSize) {
        double x;
        double y;
        double width;
        double height;
        if (isFixedSize) {
            x = Math.min(pane.getWidth(), Math.max(0, evt.getX()));
            y = Math.min(pane.getHeight(), Math.max(0, evt.getY()));
            width = RECT_WIDTH;
            height = RECT_HEIGHT;
        }
        else {
            x = Math.min(pane.getWidth(), Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX())));
            y = Math.min(pane.getHeight(), Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY())));
            width = Math.min(pane.getWidth() - 1 - Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX())), Math.abs(m_mousePressEvent.getX() - evt.getX()));
            height = Math.min(pane.getHeight() - 1 - Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY())), Math.abs(m_mousePressEvent.getY() - evt.getY()));
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
        newRect.setOnMousePressed( new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                assert(m_mousePressPos == null);
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
                if (m_mousePressPos == null) return ;
                System.out.println("drag rect start" + m_getAxisStrFromEvent(evt));
//                m_rectangleList.remove(newRect);
            }
        });

        // ドラッグ中
        newRect.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return ;

                System.out.println("dragging rect " + m_getAxisStrFromEvent(evt));
                m_setRectPosAndSize_moveRect(evt, newRect);
            }
        });

        // ドラッグ終了
        newRect.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return ;

                System.out.println("drag End rect" + m_getAxisStrFromEvent(evt));
                m_setRectPosAndSize_moveRect(evt, newRect);
                newRect.setStroke(Color.RED);
                m_mousePressPos = null;
//                m_rectangleList.add(newRect);
                TableView table = (TableView)m_scene.lookup("#tblRectangles");
                table.getItems().set(table.getItems().indexOf(newRect), newRect);
            }
        });
    }

    private Rectangle m_rectToRectanble(Rect r) {
        Rectangle result = new Rectangle(r.x, r.y, r.width, r.height);
        result.setFill(Color.TRANSPARENT);
        result.setStroke(Color.RED);
        // result.setMouseTransparent(true);
        m_setRectangleEvents(result);
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

    private boolean m_isAutoSave() {
        CheckBox cbxAutoSave = (CheckBox)m_scene.lookup("#cbxAutoSave");
        return cbxAutoSave.isSelected();
    }
    private boolean m_isFixedSize() {
        CheckBox cbxFixedSize = (CheckBox)m_scene.lookup("#cbxFixedSize");
        return cbxFixedSize.isSelected();
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
