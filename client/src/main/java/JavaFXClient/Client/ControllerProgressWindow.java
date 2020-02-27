package JavaFXClient.Client;

import CloudPackage.Helpers;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerProgressWindow {
    private Controller mainController;
    private Stage stage;

    @FXML
    ProgressBar progressBar;
    @FXML
    Label label;

    public void setMainController(Controller mainController, Stage stage) {
        this.mainController = mainController;
        this.stage = stage;
    }

    public void progressUpdate(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mainController.getHelpers().isProgressFinish()){
                    progressBar.setProgress((mainController.getHelpers().getProgress() * 0.01));
                    setLableText("Загрузка - " + mainController.getHelpers().getProgress() + "%");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mainController.getHelpers().ProgressReset();
                Close();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    public void setLableText(String text){
        Platform.runLater(() -> {
            label.setText(text);
        });
    }

    @FXML
    public void Close(){
        Platform.runLater(() -> {
            stage.close();
        });
    }
}
