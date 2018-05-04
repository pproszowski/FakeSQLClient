package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class LogInController {
    @FXML private PasswordField passwordField;
    @FXML private Text actiontarget;

    @FXML protected void handleSubmitButtonAction(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));
        Scene secondScene = new Scene(root, 600, 575);
        Stage newWindow = new Stage();
        newWindow.setTitle("FAKE SQL");
        newWindow.setScene(secondScene);
        newWindow.show();
        Stage stage = (Stage) actiontarget.getScene().getWindow();
        stage.close();
    }
}
