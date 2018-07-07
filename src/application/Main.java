package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javax.swing.*;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // FXMLのレイアウトをロード
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainForm.fxml"));
            Parent root = loader.load();

            // タイトルセット
            primaryStage.setTitle("JavaFXSample");

            // シーン生成
            Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);

            //　ラベルをコントローラに渡しておく
            Label lbl = (Label)scene.lookup("#lblStatus");
            assert(lbl != null);
            MainFormController controller = loader.getController();
            controller.setLabel(lbl);

            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        /*
        JFrame frame = new JFrame("タイトル");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        */
        launch(args);
    }
}
