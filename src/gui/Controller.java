package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import com.powder.Client.*;

import java.io.IOException;

public class Controller {
    @FXML
    public TextArea commandInput;
    @FXML
    public Button sendRequestButton;

    public void sendRequest(MouseEvent mouseEvent) {
        try {
            HttpManager httpManager = HttpManager.getInstance();
            httpManager.sendRequest(commandInput.getText());
            commandInput.setText(httpManager.getResponse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
