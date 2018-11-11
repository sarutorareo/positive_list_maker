package classifier_ui;

import application.RectPos;
import groovy.transform.PackageScope;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.converter.DoubleStringConverter;
import tess4j_client.StrRectangle;
import utils.ResizableRectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import static utils.EventUtil.getAxisStrFromEvent;

abstract public class BoxMakerFormControllerBase {
    protected Scene m_scene = null;
    protected MouseEvent m_mousePressEvent = null;
    // マウスダウンしたときのRect左上からの相対座標
    protected RectPos m_mousePressPos = null;
    protected Rectangle m_rect = null;
    protected boolean m_isShiftDown = false;
    protected Point2D m_bigPicturePoint = new Point2D(0, 0);

    public void setScene(Scene scene) {
        m_scene = scene;
    }

    protected void m_beforeSetImageOnShow() {}
    protected void m_afterSetImageOnShow() throws java.io.IOException {}

    protected void m_beforeSetImage() {}
    protected void m_afterSetImage() throws Exception {}

    protected void m_beforePaste() throws Exception {}
    protected void m_afterPaste() {}


    @PackageScope
    void onShow(Image img) throws Exception {
        m_beforeSetImageOnShow();

        if (img != null) {
            m_setImage(img);
        }

        m_afterSetImageOnShow();

        m_setTimer();
    }

    private void m_setTimer() {
        ImageView bigPictureView = m_getBigPictureView();
        // BigPicture再描画用のイベント
        Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                m_showBigPicture(bigPictureView);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void m_initImageView(Image img) {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        ImageView imvBigPicture = m_getBigPictureView();
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        assert (pane != null);

        if (imgView == null) {
            System.out.println("create new ImageView");
            imgView = new ImageView();
            imgView.setId("imvPic");
            // イメージビューに対するイベントを設定
            m_initImageViewEvent(imgView, imvBigPicture);

            //Paneにimageviewを載せる
            pane.getChildren().add(imgView);
        }

        // 両脇に黒い領域を追加する
        Image newImg = img;
        imgView.setImage(newImg);
        imgView.setVisible(true);
        // 画面サイズを調整
        pane.setMaxWidth(newImg.getWidth());
        pane.setMaxHeight(newImg.getHeight());
        Window wnd = m_scene.getWindow();
        final int TABLE_WIDTH = 240 + 120;
        wnd.setWidth(newImg.getWidth() + TABLE_WIDTH);
        wnd.setHeight(newImg.getHeight() + 145);
        SplitPane sp = (SplitPane) m_scene.lookup("#spImageTable");
        sp.setDividerPosition(0, (newImg.getWidth() + 35) / (newImg.getWidth() + TABLE_WIDTH));
    }

    protected void m_setAllRectangleStroke(boolean forceReset) {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");

        for (int i = 0; i < table.getItems().size(); i++) {
            Rectangle rect = (Rectangle) table.getItems().get(i);
            if (!forceReset && table.getSelectionModel().isSelected(i)) {
                rect.setStroke(Color.AQUA);
            } else {
                rect.setStroke(m_getDefaultRectColor());
            }
        }
    }

    abstract protected boolean m_isFixedSize();
    abstract protected Color m_getDefaultRectColor();
    abstract protected int m_getRectFixedWidth();
    abstract protected int m_getRectFixedHeight();

    private void m_setRectPosAndSize(MouseEvent evt, boolean isFixedSize) {
        if (m_rect == null) return;
        if (m_mousePressEvent == null) return;
        ImageView imgView = (ImageView) evt.getSource();

        if (isFixedSize) {
            m_rect.setX(Math.min(imgView.getImage().getWidth() - m_getRectFixedWidth(), Math.max(0, evt.getX())));
            m_rect.setY(Math.min(imgView.getImage().getHeight() - m_getRectFixedHeight(), Math.max(0, evt.getY())));
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

        rect.setX(Math.min(imgView.getImage().getWidth() - m_getRectFixedWidth(), Math.max(0, evt.getX() - m_mousePressPos.x)));
        rect.setY(Math.min(imgView.getImage().getHeight() - m_getRectFixedHeight(), Math.max(0, evt.getY() - m_mousePressPos.y)));
    }

    private void m_setRectPosAndSize_resizeRect(MouseEvent evt, ResizableRectangle rect) {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        double parentWidth = imgView.getImage().getWidth();
        double parentHeight = imgView.getImage().getHeight();
        /*
        System.out.println(String.format("evt.getX() = %d, m_mousePressPos.x = %d, rect.getX() = %d, rect.getWidth() = %d",
                (int)evt.getX(), (int)m_mousePressPos.x, (int)rect.getX(), (int)rect.getWidth()));
        */

        double newX;
        double newY;
        switch (rect.getDraggingCorner()){
            case N:
                newY = Math.min(parentHeight, Math.max(0, evt.getY()));
                rect.setHeight(rect.getHeight() + rect.getY() - newY);
                rect.setY(newY);
                break;
            case W:
                newX = Math.min(parentWidth, Math.max(0, evt.getX()));
                rect.setWidth(rect.getWidth() + rect.getX() - newX);
                rect.setX(newX);
                break;
            case E:
                rect.setWidth(evt.getX() - rect.getX());
                break;
            case S:
                rect.setHeight(evt.getY() - rect.getY());
                break;
            case NW:
                newX = Math.min(parentWidth, Math.max(0, evt.getX()));
                newY = Math.min(parentHeight, Math.max(0, evt.getY()));
                rect.setWidth(rect.getWidth() + rect.getX() - newX);
                rect.setHeight(rect.getHeight() + rect.getY() - newY);
                rect.setX(newX);
                rect.setY(newY);
                break;
            case NE:
                rect.setWidth(evt.getX() - rect.getX());
                newY = Math.min(parentHeight, Math.max(0, evt.getY()));
                rect.setHeight(rect.getHeight() + rect.getY() - newY);
                rect.setY(newY);
                break;
            case SW:
                rect.setHeight(evt.getY() - rect.getY());
                newX = Math.min(parentWidth, Math.max(0, evt.getX()));
                rect.setWidth(rect.getWidth() + rect.getX() - newX);
                rect.setX(newX);
                break;
            case SE:
                rect.setWidth(evt.getX() - rect.getX());
                rect.setHeight(evt.getY() - rect.getY());
                break;
        }
    }

    protected void m_setRectangleEvents(Rectangle newRect) {
        ImageView imvBigPicture = m_getBigPictureView();
        // マウスダウン
        newRect.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                assert (m_mousePressPos == null);
                System.out.println("mouse press rect start" + getAxisStrFromEvent(evt));
                m_mousePressPos = new RectPos( evt.getX() - newRect.getX(), evt.getY() - newRect.getY());
                m_setAllRectangleStroke(true);
                newRect.setStroke(Color.AQUA);
                boolean isCorner = m_isShiftDown;
                EnEdge c = ((ResizableRectangle)newRect).getEdge((int)(evt.getX() - newRect.getX()), (int)(evt.getY() - newRect.getY()), isCorner);
                ((ResizableRectangle)newRect).setDraggingCorner(c);
                m_setSelectedRectangle(newRect);
            }
        });

        // マウスアップ
        newRect.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                m_mousePressPos = null;
                ((ResizableRectangle)newRect).setDraggingCorner(EnEdge.NONE);
            }
        });

        // ドラッグ開始
        newRect.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return;
                System.out.println("drag rect start" + getAxisStrFromEvent(evt));
            }
        });

        // ドラッグ中
        newRect.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return;

                System.out.println("dragging rect " + getAxisStrFromEvent(evt));
                EnEdge c = ((ResizableRectangle)newRect).getDraggingCorner();
                if (c == EnEdge.NONE) {
                    m_setRectPosAndSize_moveRect(evt, newRect);
                }
                else {
                    m_setRectPosAndSize_resizeRect(evt, (ResizableRectangle)newRect);
                }

                m_showBigPicture(imvBigPicture, (int)evt.getX(), (int)evt.getY());
            }
        });

        // ドラッグ終了
        newRect.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressPos == null) return;

                System.out.println("drag End rect" + getAxisStrFromEvent(evt));
               // m_setRectPosAndSiz
                newRect.setStroke(m_getDefaultRectColor());
                m_mousePressPos = null;
                TableView table = (TableView) m_scene.lookup("#tblRectangles");
                table.getItems().set(table.getItems().indexOf(newRect), newRect);
            }
        });

        // マウスムーブ
        newRect.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                boolean isCorner = m_isShiftDown;
                m_showBigPicture(imvBigPicture, (int)evt.getX(), (int)evt.getY());
                ((ResizableRectangle)newRect).setCursorByPoint( (int)(evt.getX() - newRect.getX()), (int)(evt.getY() - newRect.getY()), isCorner);
            }
        });
    }

    protected ImageView m_getBigPictureView() {
        return (ImageView) m_scene.lookup("#imvBigPicture");
    }

    protected ResizableRectangle newRectangle(double x, double y, double width, double height)
    {
       return new ResizableRectangle(x, y, width, height);
    }

    private Rectangle m_initRectangle(MouseEvent evt, boolean isFixedSize) {
        double x;
        double y;
        double width;
        double height;
        Image img = ((ImageView) evt.getSource()).getImage();

        if (isFixedSize) {
            x = Math.min(img.getWidth() - m_getRectFixedWidth(), Math.max(0, evt.getX()));
            y = Math.min(img.getHeight() - m_getRectFixedHeight(), Math.max(0, evt.getY()));
            width = m_getRectFixedWidth();
            height = m_getRectFixedHeight();
        } else {
            x = Math.min(img.getWidth(), Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX())));
            y = Math.min(img.getHeight(), Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY())));
            width = Math.min(img.getWidth() - 1 - Math.max(0, Math.min(m_mousePressEvent.getX(), evt.getX())), Math.abs(m_mousePressEvent.getX() - evt.getX()));
            height = Math.min(img.getHeight() - 1 - Math.max(0, Math.min(m_mousePressEvent.getY(), evt.getY())), Math.abs(m_mousePressEvent.getY() - evt.getY()));
        }

        ResizableRectangle newRect = newRectangle(x, y, width, height);
        newRect.setFill(Color.TRANSPARENT);
        newRect.setStroke(Color.AQUA);
        // マウスイベントを透過させる
        // newRect.setMouseTransparent(true);
        // イベントを設定する
        m_setRectangleEvents(newRect);

        return newRect;
    }

    abstract protected ObservableList<Rectangle> m_getCurrentRectangleList();

    protected void m_initImageViewEvent(ImageView imgView, ImageView imvBigPicture) {
        if (imgView == null) {
            return;
        }

        // マウスダウン
        imgView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                assert (m_mousePressEvent == null);
                System.out.println("onMousePressed " + getAxisStrFromEvent(evt));
                m_mousePressEvent = evt;
            }
        });

        // ドラッグ開始
        imgView.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressEvent == null) return;
                assert (m_rect == null);
                System.out.println("drag start" + getAxisStrFromEvent(evt));
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

                System.out.println("dragging" + getAxisStrFromEvent(evt));
                m_setRectPosAndSize(evt, m_isFixedSize());
                m_showBigPicture(imvBigPicture, (int)evt.getX(), (int)evt.getY());
            }
        });

        // ドラッグ終了
        imgView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                if (m_mousePressEvent == null) return;
                if (m_rect == null) return;

                System.out.println("drag End" + getAxisStrFromEvent(evt));
                m_setRectPosAndSize(evt, m_isFixedSize());
                m_mousePressEvent = null;
                m_rect.setStroke(m_getDefaultRectColor());
                m_getCurrentRectangleList().add(m_rect);
                m_setSelectedRectangle(m_rect);
                m_rect = null;
            }
        });

        // マウスムーブ
        imgView.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent evt) {
                m_showBigPicture(imvBigPicture, (int)evt.getX(), (int)evt.getY());
            }
        });
    }

    protected void m_setImage(Image image) throws Exception {
        // 前処理
        m_beforeSetImage();

        //image viewの作成
        m_initImageView(image);

        // 後処理
        m_afterSetImage();
    }

    private void m_clearWindow() {
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        Classifier.clearRectsFromPane(pane);
    }

    protected void m_doPaste(Image img) throws Exception {
        m_beforePaste();

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

        m_afterPaste();
    }

    abstract protected void m_undo();

    protected Image m_getImage() {
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        if (imgView == null) return null;
        return imgView.getImage();
    }

    protected void m_onKeyPressed_paneMain(KeyEvent evt) throws Exception {
        if (evt.isControlDown() && (evt.getCode() == KeyCode.Z)) {
            Scene scene = ((Pane) evt.getSource()).getScene();
            m_undo();
            return;
        }
        if (evt.isControlDown() && (evt.getCode() == KeyCode.V)) {
            m_doPaste(m_getImage());
        }
        m_isShiftDown = evt.isShiftDown();
        if (m_isMoveKey(evt.getCode())) {
            m_cursorKeyPressed(evt.getCode(), evt.isShiftDown());
        }

    }

    protected void m_onKeyReleased_paneMain(KeyEvent evt) throws Exception {
        m_isShiftDown = evt.isShiftDown();
    }
    private void m_cursorKeyPressed(KeyCode key, boolean isShiftDown) {
        ObservableList<Rectangle> rectList = m_getSelectedRectangle();
        Image img = m_getImage();

        if (rectList == null) return;

        rectList.forEach(rect -> {
            if (isShiftDown) {
                m_resizeRect(key, rect, img);
            }
            else {
                m_moveRect(key, rect, img);
            }
        });
    }

    private boolean m_isMoveKey(KeyCode key) {
        return ((key == KeyCode.LEFT) ||
                (key == KeyCode.RIGHT) ||
                (key == KeyCode.UP) ||
                (key == KeyCode.DOWN) ||
                (key == KeyCode.W) ||
                (key == KeyCode.A) ||
                (key == KeyCode.S) ||
                (key == KeyCode.D) );
    }

    private void m_moveRect(KeyCode key, Rectangle rect, Image img) {
        double newX = rect.getX();
        double newY = rect.getY();
        switch (key) {
            case W:
            case UP:
                newY = (rect.getY() - 1);
                break;
            case A:
            case LEFT:
                newX = (rect.getX() - 1);
                break;
            case S:
            case DOWN:
                newY = (rect.getY() + 1);
                break;
            case D:
            case RIGHT:
                newX = (rect.getX() + 1);
                break;
        }
        newX = Math.min(Math.max(0, newX), img.getWidth() - rect.getWidth());
        newY = Math.min(Math.max(0, newY), img.getHeight() - rect.getHeight());
        rect.setX(newX);
        rect.setY(newY);
        m_showBigPicture(m_getBigPictureView());
    }

    private void m_resizeRect(KeyCode key, Rectangle rect, Image img) {
        double newWidth = rect.getWidth();
        double newHeight = rect.getHeight();
        switch (key) {
            case W:
            case UP:
                newHeight = (rect.getHeight() - 1);
                break;
            case A:
            case LEFT:
                newWidth = (rect.getWidth() - 1);
                break;
            case S:
            case DOWN:
                newHeight = (rect.getHeight() + 1);
                break;
            case D:
            case RIGHT:
                newWidth = (rect.getWidth() + 1);
                break;
        }
        newWidth = Math.min(Math.max(1, newWidth), img.getWidth() - rect.getX());
        newHeight = Math.min(Math.max(1, newHeight), img.getHeight() - rect.getY());
        rect.setWidth(newWidth);
        rect.setHeight(newHeight);
        m_showBigPicture(m_getBigPictureView());
    }

    protected ObservableList<Rectangle> m_getSelectedRectangle()
    {
        return null;
    }
    protected void m_setSelectedRectangle(Rectangle rect) {
        ;
    }

    protected void m_setColumnEditable(TableColumn col, String mName, boolean isDouble) {
        col.setEditable(true);
        if (isDouble) {
            col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        }
        else {
            col.setCellFactory(TextFieldTableCell.forTableColumn());
        }
        col.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
            @Override
            public void handle(TableColumn.CellEditEvent event) {
                Object newVal = event.getNewValue();
                Rectangle rowRect = (Rectangle) event.getRowValue();
                try {
                    Method m;
                    if (isDouble) {
                         m = Rectangle.class.getMethod(mName, double.class);
                    }
                    else {
                         m = StrRectangle.class.getMethod(mName, String.class);
                    }
                    m.invoke(rowRect, newVal);
                    rowRect.setStroke(Color.RED);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void m_setTableRows(ObservableList items) {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");
        table.setItems(items);
    }

    protected void m_saveText(List<String> appendLines, String dir, String fileName) throws IOException {
        StringBuilder sbOrg = new StringBuilder();
        StringBuilder sbNew = new StringBuilder();
        File txtFile = new File(dir, fileName);
        // 読む
        try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
            String str;
            while((str = br.readLine()) != null){
                sbOrg.append(String.format("%s\n", str));
            }
        }catch(FileNotFoundException e) {
            // System.out.println(e);
        }
        // 新しい行
        for (String appendLine : appendLines) {
            sbNew.append(String.format("%s\n", appendLine));
        }

        // 書く
        try (FileWriter filewriter = new FileWriter(txtFile, false)) {
            filewriter.write(sbNew.toString());
            filewriter.write(sbOrg.toString());
        }
    }

    protected String m_getNewPicFileName(Scene scene, String dir, String fmtFileName) throws Exception {
        for (int i = 0; i < 10000; i++) {
            File f = new File(dir, String.format(fmtFileName, i));
            if (!f.exists()) {
                System.out.println("save file " + f.getAbsolutePath());
                return f.getPath();
            }
        }
        throw new Exception("can't make new pic filename");
    }

    private File m_doSaveImg(Image img, String path, String fmt) throws java.io.IOException {
        File f = new File(path);
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), fmt, f);
        return f;
    }

    protected File m_saveImage(String dir) throws Exception {
        Image img = m_getImage();
        if (img == null) return null;
        String picPath = m_getNewPicFileName(m_scene, dir, "positive_%d.png");
        File f = null;
        try {
            // 画像を保存
            f = m_doSaveImg(img, picPath, "png");
        } catch (IOException ex) {
            System.out.println("!!! IOException at m_saveImage " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("!!! Exception at m_saveImage " + ex.getMessage());
        }
        return f;
    }

    abstract protected String m_getTextFileName(String imgFileName);

    @PackageScope
    void saveTextAndImage(String dataDir, Function<String, List<String>>makeAppendLines) throws Exception {
        if (m_getCurrentRectangleList().size() == 0) {
            System.out.println("!!! no rectangle");
            return;
        }
        File imgFile = m_saveImage(dataDir);

        // テキストリストを保存
        List<String>appendLine = makeAppendLines.apply(imgFile.getName());
        m_saveText(appendLine, imgFile.getParent(), m_getTextFileName(imgFile.getName()));
    }

    private void m_showBigPicture(ImageView v, int x, int y) {
        if (v == null) return;
        Image img = m_getImage();
        if (img == null) return;

        int width = 80;
        int height = 80;
        int partX = (int)Math.min(img.getWidth() - width, Math.max(0, x - width/2));
        int partY = (int)Math.min(img.getHeight() - height, Math.max(0, y - height/2));
        WritableImage subImage;
        /*
        subImage = new WritableImage(img.getPixelReader(),
                partX, partY, width, height);
        */

        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        javafx.geometry.Point2D screen = imgView.localToScreen(partX, partY);

        try {
            // キャプチャの範囲 (starsの画面はwidth = 795, height = 579)
            java.awt.Rectangle bounds = new java.awt.Rectangle((int)screen.getX(), (int)screen.getY(), width, height);

            // これで画面キャプチャ
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(bounds);
            subImage = SwingFXUtils.toFXImage(image, null);
        } catch( java.awt.AWTException ex) {
            System.out.println("!!! IOException at m_saveImage " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        v.setImage(subImage);

        // 前回の座標を記憶
        m_bigPicturePoint = new Point2D(x, y);
    }

    private void m_showBigPicture(ImageView v) {
        m_showBigPicture(v, (int)m_bigPicturePoint.getX(), (int)m_bigPicturePoint.getY());
    }
}
