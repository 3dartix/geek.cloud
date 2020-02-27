package JavaFXClient.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXClient  extends Application {
    public static Stage PRIMARI_STAGE;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("mainWindow.fxml"));
        primaryStage.setTitle("Cloud client");
        primaryStage.setScene(new Scene(root, 400, 500));

        PRIMARI_STAGE = primaryStage;
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.out.println("On Close"));
    }


    public static void main(String[] args) {
        launch(args);
    }

}
