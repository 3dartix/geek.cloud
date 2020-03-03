package com.artem.clientNetty;

import com.artem.helpers.ICallback;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class ControllerProgressWindow implements ICallback {
    private Controller mainController;
    private Stage stage;

    @FXML
    ProgressBar progressBar;
    @FXML
    Label label;

    public void setMainController(Controller mainController, Stage stage) {
        this.mainController = mainController;
        this.stage = stage;
        this.mainController.getHelpers().registerUpdateProgressCallBack(this);
    }

    @Override
    public void progressCallback() {
        Platform.runLater(() -> {
            progressBar.setProgress((mainController.getHelpers().getProgress() * 0.01));
            label.setText("Загрузка - " + mainController.getHelpers().getProgress() + "%");
        });
    }

    @Override
    public void closeModalWindow() {
        Platform.runLater(() -> {
            stage.close();
        });
    }
}
