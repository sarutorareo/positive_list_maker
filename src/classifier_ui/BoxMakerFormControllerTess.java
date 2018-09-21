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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.*;

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

    private String m_dummyStr(String optStr) {
        return optStr + ":dummyStr";
    }

    @FXML
    protected void onClick_saveTextAndImage_button(ActionEvent evt) throws IOException, Exception {
        saveTextAndImage("d:\\temp\\saveTest", this::m_dummyStr);
    }

    private void m_initTableView() {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");

        table.setEditable(true);

        TableColumn colChar = new TableColumn("Char");
        m_setColumnEditable(colChar, "setChar");
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
        File result = super.m_saveImage(dir);
        Image fxImage = m_getImage();
        Image fxBinImage = ImageUtils.toBinaryFxImage(fxImage, 80);

        BufferedImage bImage = SwingFXUtils.fromFXImage(fxBinImage, null);
        ImageUtils.saveTiff("d:\\temp\\saveTest\\test.tiff", bImage);
        return result;
    }
}
