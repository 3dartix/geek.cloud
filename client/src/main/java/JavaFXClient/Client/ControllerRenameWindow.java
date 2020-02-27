package JavaFXClient.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerRenameWindow{
    private Controller mainController;
    private String oldNameFile;

    @FXML
    TextField textField;

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }

    public void setTextToTextField(String fileName){
        oldNameFile = fileName;
        textField.setText(oldNameFile);
    }

    @FXML
    public void Rename(ActionEvent event){
        mainController.RenameFle(oldNameFile, textField.getText());
        Close(event);
    }

    @FXML
    public void Close(ActionEvent event){
        //взвращаем кнопку
        Button button = (Button) event.getSource();
        //из кнопки возвращаем сцену
        //Scene scene = button.getScene();
        //возвращаем Stage
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
