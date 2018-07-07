package application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.scene.shape.Rectangle;

public class MainFormController {
    private Label m_lblStatus = null;
    private MouseEvent m_startEvent = null;
    private Rectangle m_rect = null;
    private final ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList(
    );

    public void setLabel(Label lbl) {
        m_lblStatus = lbl;
    }
    @FXML
    protected void onClick_paste_button(ActionEvent evt) {
        System.out.println("クリックされました！");
        m_clearWindow(evt);
        // クリップボードの画像を取得
        Clipboard cb = Clipboard.getSystemClipboard();
        System.out.println(cb.getString());
        System.out.println(cb.getContentTypes());
        Image image = cb.getImage();
        //image viewの作成
        Scene scene = ((Button)evt.getSource()).getScene();
        m_initImageView(scene, image);

        // list view の初期化
        m_initTableView(scene);
    }

    @FXML
    protected void onClick_load_button(ActionEvent evt) {
        System.out.println("load button");
        m_clearWindow(evt);
        // 画像ファイルを表示
        //imageの読み込み
        Image image = new Image( "file:d:/temp/160414_034715.jpg" );
        //image viewの作成
        Scene scene = ((Button)evt.getSource()).getScene();
        m_initImageView(scene, image);

        // list view の初期化
        m_initTableView(scene);
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
        ImageView imgView = (ImageView)scene.lookup("#ImgView");

        if (imgView != null) {
            imgView.setImage(img);
        }
        else {
            imgView = new ImageView( img );
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

        m_rect = new Rectangle(Math.min(m_startEvent.getX(), evt.getX()),
                Math.min(m_startEvent.getY(), evt.getY()),
                Math.abs(m_startEvent.getX() - evt.getX()),
                Math.abs(m_startEvent.getY() - evt.getY()));
        m_rect.setFill(Color.TRANSPARENT);
        m_rect.setStroke(Color.BLUE);
        // マウスイベントを透過させる
        m_rect.setMouseTransparent(true);
        ((Pane)((ImageView)evt.getSource()).getParent()).getChildren().add(m_rect);
        m_rectangleList.add(m_rect);
    }

    private String m_getAxisStrFromEvent(MouseEvent evt) {
        return String.format("x = %f, y = %f", evt.getX(), evt.getY());
    }
}
