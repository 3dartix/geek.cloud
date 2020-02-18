package JavaFXClient.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXClient  extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        //Date date = new Date();

        //Menu menu1 = new Menu("Меню");
       // MenuItem menuItem1 = new MenuItem("Сменить фон");
       // menu1.getItems().add(menuItem1);
       // MenuBar menuBar = new MenuBar();
       // menuBar.getMenus().add(menu1);

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        primaryStage.setTitle("Cloud client");
        primaryStage.setScene(new Scene(root, 400, 500));

        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.out.println("On Close"));
    }


    public static void main(String[] args) {
        launch(args);
    }

}
