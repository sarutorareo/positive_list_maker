package application;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import positive_list_maker.PositiveListMakerFormController;

import java.io.*;
import java.nio.file.Paths;

public class ViewFormController {
    private PositiveListMakerFormController m_controller;

    @FXML
    protected void onClick_positive_list_button(ActionEvent evt) throws Exception {
        System.out.println("positive_list_button");
        Stage parent = (Stage) ((Node) evt.getTarget()).getScene().getWindow();
        try {
            Stage stage = new Stage();
            stage.initOwner(parent);
            stage.setTitle("new window example");
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("PositiveListMakerForm.fxml"));
            FXMLLoader loader = new FXMLLoader(Paths.get("src\\positive_list_maker\\PositiveListMakerForm.fxml").toUri().toURL());
            Scene scene = new Scene(loader.load());// new Scene(FXMLLoader.load(Paths.get("PositiveListMakerForm.fxml").toUri().toURL())
            stage.setScene(scene);

            //　ラベルをコントローラに渡しておく
            Label lbl = (Label)scene.lookup("#lblStatus");
            assert(lbl != null);
            PositiveListMakerFormController m_controller = loader.getController();
            m_controller.setLabel(lbl);
            m_controller.setScene(scene);

            // FXMLLoader loader = new FXMLLoader(getClass().getResource("PositiveListMakerForm.fxml"));
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)  {
                    System.out.println("Stage is closing");
                    try {
                        m_controller.saveText();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            });

            stage.show();
            m_controller.onShow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
