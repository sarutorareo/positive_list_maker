package application;
import classifier_ui.ClassifierViewFormController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    private ClassifierViewFormController m_controller;

    @Override
    public void start(Stage primaryStage) {
        try {
            // FXMLのレイアウトをロード
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../classifier_ui/ClassifierViewForm.fxml"));
            Parent root = loader.load();
            // タイトルセット
            primaryStage.setTitle("JavaFXSample");
            // シーン生成
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            m_controller = loader.getController();
            m_controller.setScene(scene);

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)  {
                    System.out.println("Stage is closing");
                    m_controller.stopThread();
                }
            });

            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stage is closing");
    }
}
