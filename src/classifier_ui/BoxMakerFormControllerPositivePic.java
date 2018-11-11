package classifier_ui;
import groovy.transform.PackageScope;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Size;

import javax.annotation.Resources;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class BoxMakerFormControllerPositivePic extends BoxMakerFormControllerBase {
    /*
    private final ObservableList<Rectangle> m_rectangleList = FXCollections.observableArrayList();
    private final ArrayList<Rectangle> m_classifierUI.getFullRectangleList() = new ArrayList<>();
    */
    private ClassifierPlayer m_cfPlayer = new ClassifierPlayer();
    private ClassifierDealerButton m_cfDealerButton = new ClassifierDealerButton();
    private ClassifierChip m_cfChip = new ClassifierChip();
    private List<Classifier> m_lstClassifier = Arrays.asList(m_cfPlayer, m_cfDealerButton, m_cfChip);
    // private CFResultPlayer m_crPlayer = new CFResultPlayer();
    // private CFResultDealerButton m_crDealerButton = new CFResultDealerButton();

    public BoxMakerFormControllerPositivePic() {
        System.out.println("in constructor");
    }

    @Override
    protected void m_beforeSetImageOnShow() {
        super.m_beforeSetImageOnShow();
        m_changeAutoSave();
        m_initTableView();
        m_initChoiceBoxTarget();
    }

    @Override
    protected void m_afterSetImageOnShow() throws java.io.IOException {
        // グローバル設定をロード
        GlobalSettings gs = GlobalSettings.load();
        m_initGlobalSettings(gs);

        // ターゲット毎の設定をロード
        m_chbTargetChanged(null);

        super.m_afterSetImageOnShow();
    }

    @Override
    protected void m_afterSetImage() throws Exception {
        // list view の初期化
        m_clearTableRows();

        // 検出器を動かして検出結果をリストに追加
        m_classify();
    }

    @Override
    protected void m_beforePaste() throws Exception {
        if (m_isAutoSave()) {
            // 変更前の画像とテキストをファイルに保存
            Classifier cf = getCurrentTargetClassifier();
            saveTextAndImage(cf.getDataDir(), cf::getPosListStr);
        }
    }

    @Override
    protected void m_afterPaste() {
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
        cmbListTarget.add("Chip");
        chbTarget.setItems(cmbListTarget);
        chbTarget.getSelectionModel().select(0);
    }

    @FXML
    protected void initialize(URL location, Resources resources) {
        System.out.println("in initialize");
    }

    @FXML
    protected void onClick_paste_button(ActionEvent evt) throws Exception {
        System.out.println("paste button current dir = " + new File(".").getAbsoluteFile());
        m_doPaste(m_getImage());
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

    private ObservableList<Rectangle> m_getCurrentTargetItems() {
        System.out.println("m_getCurrentTargetItems curent target index = " + getCurrentTarget().toInt());
        switch (getCurrentTarget()) {
            case Player:
                System.out.println("m_getCurrentTargetItems  = " + m_cfPlayer.getRectangleList().size());
                return m_cfPlayer.getRectangleList();
            case DealerButton:
                System.out.println("m_getCurrentTargetItems  = " + m_cfDealerButton.getRectangleList().size());
                return m_cfDealerButton.getRectangleList();
            case Chip:
                System.out.println("m_getCurrentTargetItems  = " + m_cfChip.getRectangleList().size());
                return m_cfChip.getRectangleList();
            default:
                return null;
        }
    }

    @PackageScope
    EnTarget getCurrentTarget() {
        ChoiceBox chb = (ChoiceBox) m_scene.lookup("#chbTarget");
        return EnTarget.fromInt(chb.getSelectionModel().getSelectedIndex());
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
        Classifier cf = getCurrentTargetClassifier();
        saveTextAndImage(cf.getDataDir(), cf::getPosListStr);
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

        // Chip
        {
            // 検出
            m_cfChip.classify(fxImage, this::m_setRectangleEvents);
            m_cfChip.setResultToPane(fxImage, pane, m_isFixedSize());
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

    private void m_clearRectangles() {
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        m_lstClassifier.forEach(c -> c.clearRectsFromPane(pane));
        m_clearTableRows();
    }

    private void m_changeHideFullRect() {
        CheckBox chkHide = (CheckBox) m_scene.lookup("#cbxHideFullRect");
        boolean isHide = chkHide.isSelected();
        m_lstClassifier.forEach(c -> c.changeHideFullRect(isHide));
    }

    @Override
    protected void m_undo() {
        Pane pane = (Pane) m_scene.lookup("#paneAnchorImage");
        Classifier cf = getCurrentTargetClassifier();
        if (cf.getRectangleList().size() == 0)
            return;

        cf.clearRect(pane, cf.getRectangleList().get(cf.getRectangleList().size() - 1));
    }

    @FXML
    protected void onKeyPressed_paneMain(KeyEvent evt) throws Exception {
        super.m_onKeyPressed_paneMain(evt);
    }

    @FXML
    protected void onKeyReleased_paneMain(KeyEvent evt) throws Exception {
        super.m_onKeyReleased_paneMain(evt);
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

    @Override
    protected Color m_getDefaultRectColor() {
        return getCurrentTargetClassifier().getRectangleColor();
    }

    private void m_initTableView() {
        TableView table = (TableView) m_scene.lookup("#tblRectangles");

        table.setEditable(true);

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

    @Override
    protected int m_getRectFixedWidth() {
        return getCurrentTargetClassifier().getRectFixedWidth();
    }
    @Override
    protected int m_getRectFixedHeight() {
        return getCurrentTargetClassifier().getRectFixedHeight();
    }

    @Override
    protected ObservableList<Rectangle> m_getCurrentRectangleList() {
        return getCurrentTargetClassifier().getRectangleList();
    }

    private void m_doSaveSettings() {
        try {
            CFSettings cs;
            switch (getCurrentTarget()) {
                case Player:
                    cs = m_createClassifierSettingsFromGUI(new CFSettingsPlayer());
                    break;
                case DealerButton:
                    cs = m_createClassifierSettingsFromGUI(new CFSettingsDealerButton());
                    break;
                case Chip:
                    cs = m_createClassifierSettingsFromGUI(new CFSettingsChip());
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
            case Chip:
                return m_cfChip;
            default:
               assert(false);
               return null;
        }
    }

    private boolean m_isAutoSave() {
        CheckBox cbxAutoSave = (CheckBox)m_scene.lookup("#cbxAutoSave");
        return cbxAutoSave.isSelected();
    }

    @Override
    protected boolean m_isFixedSize() {
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
        final Classifier cf = getCurrentTargetClassifier();

        try {
            switch (getCurrentTarget()) {
                case Player:
                    cs = CFSettingsPlayer.load();
                    break;
                case DealerButton:
                    cs = CFSettingsDealerButton.load();
                    break;
                case Chip:
                    cs = CFSettingsChip.load();
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
        m_lstClassifier.forEach(c -> {
                if (c != cf) {
                    c.setRectClickable(false);
                }
        } );

        // イメージビューに対するイベントを設定
        ImageView imgView = (ImageView) m_scene.lookup("#imvPic");
        ImageView imgBigPictureView = (ImageView) m_scene.lookup("#imvBigPicture");
        m_initImageViewEvent(imgView, imgBigPictureView);
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

    private CFSettings m_createClassifierSettingsFromGUI(CFSettings cs)
    {
        TextField txtMinNeighbors = (TextField)m_scene.lookup("#txtMinNeighbors");
        TextField txtScaleFactor = (TextField)m_scene.lookup("#txtScaleFactor");
        TextField txtMinSizeWidth = (TextField)m_scene.lookup("#txtMinSizeWidth");
        TextField txtMinSizeHeight = (TextField)m_scene.lookup("#txtMinSizeHeight");
        TextField txtMaxSizeWidth = (TextField)m_scene.lookup("#txtMaxSizeWidth");
        TextField txtMaxSizeHeight = (TextField)m_scene.lookup("#txtMaxSizeHeight");

        cs.featureTypeIndex = m_getCurrentFeatureTypeIndex();
        cs.minNeighbors = Integer.parseInt(txtMinNeighbors.getText());
        cs.scaleFactor = Double.parseDouble(txtScaleFactor.getText());
        cs.setMinSize(new Size(Double.parseDouble(txtMinSizeWidth.getText()), Double.parseDouble(txtMinSizeHeight.getText())));
        cs.setMaxSize(new Size(Double.parseDouble(txtMaxSizeWidth.getText()), Double.parseDouble(txtMaxSizeHeight.getText())));

        return cs;
    }

/*
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

    private CFSettings m_createClassifierSettingsFromGUI_chip()
    {
        TextField txtMinNeighbors = (TextField)m_scene.lookup("#txtMinNeighbors");
        TextField txtScaleFactor = (TextField)m_scene.lookup("#txtScaleFactor");
        TextField txtMinSizeWidth = (TextField)m_scene.lookup("#txtMinSizeWidth");
        TextField txtMinSizeHeight = (TextField)m_scene.lookup("#txtMinSizeHeight");
        TextField txtMaxSizeWidth = (TextField)m_scene.lookup("#txtMaxSizeWidth");
        TextField txtMaxSizeHeight = (TextField)m_scene.lookup("#txtMaxSizeHeight");

        CFSettingsChip cs = new CFSettingsChip();
        cs.featureTypeIndex = m_getCurrentFeatureTypeIndex();
        cs.minNeighbors = Integer.parseInt(txtMinNeighbors.getText());
        cs.scaleFactor = Double.parseDouble(txtScaleFactor.getText());
        cs.setMinSize(new Size(Double.parseDouble(txtMinSizeWidth.getText()), Double.parseDouble(txtMinSizeHeight.getText())));
        cs.setMaxSize(new Size(Double.parseDouble(txtMaxSizeWidth.getText()), Double.parseDouble(txtMaxSizeHeight.getText())));

        return cs;
    }
*/

    private GlobalSettings m_createGlobalSettingsFromGUI()
    {
        GlobalSettings gs = new GlobalSettings();
        gs.setSelectedTargetIndex(getCurrentTarget().toInt());

        CheckBox chkHide = (CheckBox) m_scene.lookup("#cbxHideFullRect");
        gs.setIsHideFullRect(chkHide.isSelected());

        return gs;
    }

    protected String m_getTextFileName(String imgFileName) {
        final String POS_LIST_FILE_NAME = "pos_list.txt";
        return POS_LIST_FILE_NAME;
    }

}
