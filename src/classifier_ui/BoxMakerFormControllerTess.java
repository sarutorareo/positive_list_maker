package classifier_ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import tess4j_client.StrRectangle;

import utils.FileUtil;
import utils.ImageUtils;
import utils.ResizableRectangle;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BoxMakerFormControllerTess extends BoxMakerFormControllerBase {
    private final int RECT_FIXED_WIDTH = 40;
    private final int RECT_FIXED_HEIGHT = 40;
    private ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList();

    @Override
    protected boolean m_isFixedSize() {
        return false;
    }
    @Override
    protected Color m_getDefaultRectColor() {
        return Color.YELLOW;
    }
    @Override
    protected int m_getRectFixedWidth() {
        return RECT_FIXED_WIDTH;
    }
    @Override
    protected int m_getRectFixedHeight() {
        return RECT_FIXED_HEIGHT;
    }
    @Override
    protected ObservableList<Rectangle> m_getCurrentRectangleList() {
        return m_rectangleList;
    }

    @Override
    protected void m_undo() { }

    @FXML
    protected void onClick_paste_button(ActionEvent evt) throws Exception {
        System.out.println("paste button current dir = " + new File(".").getAbsoluteFile());
        m_doPaste(m_getImage());
    }

    @FXML
    protected void onKeyPressed_paneMain(KeyEvent evt) throws Exception {
        m_onKeyPressed_paneMain(evt);
    }

    @Override
    protected ResizableRectangle newRectangle(double x, double y, double width, double height)
    {
        return new StrRectangle("a", x, y, width, height);
    }

    private List<String> m_makeAppendLines(String optStr) {
        List<String> lines = new ArrayList<>();
        
        TableView table = (TableView) m_scene.lookup("#tblRectangles");
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        for (Object o : table.getItems()) {
            StrRectangle rect = (StrRectangle)o;
            lines.add(rect.toBoxString((int)pane.getHeight()));
        }
        
        return lines;
    }

    @FXML
    protected void onClick_saveTextAndImage_button(ActionEvent evt) throws IOException, Exception {
        saveTextAndImage("D:\\MyProgram\\GitHub\\positive_list_maker\\train_tess4j\\saveTest",
                this::m_makeAppendLines);
    }

    private void m_initTableView() {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");

        table.setEditable(true);

        TableColumn<Rectangle, String> colChar = new TableColumn("Char");
        m_setColumnEditable(colChar, "setChar", false);
        TableColumn colX = new TableColumn("X");
        m_setColumnEditable(colX, "setX", true);
        TableColumn colY = new TableColumn("Y");
        m_setColumnEditable(colY, "setY", true);
        TableColumn colWidth = new TableColumn("Width");
        m_setColumnEditable(colWidth, "setWidth", true);
        TableColumn colHeight = new TableColumn("Height");
        m_setColumnEditable(colHeight, "setHeight", true);

        ObservableList columns =
                FXCollections.observableArrayList();
        columns.addAll(colChar, colX, colY, colWidth, colHeight);
        table.getColumns().clear();
        table.getColumns().addAll(columns);

        colChar.setCellValueFactory(
                new PropertyValueFactory<>("Char")
        );
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
            m_setAllRectangleStroke(false);
        });
    }

    @Override
    protected void m_beforeSetImageOnShow() {
        super.m_beforeSetImageOnShow();
        m_initTableView();
    }

    @Override
    protected void m_afterSetImageOnShow() throws java.io.IOException {
        // list view の初期化
        m_setTableRows(m_getCurrentRectangleList());

        super.m_afterSetImageOnShow();
    }

    @Override
    protected File m_saveImage(String dir) throws Exception {
        // File result = super.m_saveImage(dir);
        Image fxImage = m_getImage();
        Image fxGrayImage = ImageUtils.toReverceBinaryFxImage(fxImage);

        BufferedImage bGrayImage = SwingFXUtils.fromFXImage(fxGrayImage, null);
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);

        String fontNameGray = "active_player_stack_gray";
        String fontName = "active_player_stack";
        String fileNameGray = m_getNewPicFileName(m_scene, dir, "eng." + fontNameGray + ".exp%d.tif");
        String fileName = m_getNewPicFileName(m_scene, dir, "eng." + fontName + ".exp%d.tif");
        ImageUtils.saveTiff(bGrayImage, fileNameGray);
        ImageUtils.saveTiff(bImage, fileName);

        return new File(fileName);
    }

    protected String m_getTextFileName(String imgFileName) {
        String withOutExtention = FileUtil.getWithoutExtention(imgFileName);
        return withOutExtention + ".box";
    }
}
