package com.powder.Client.GUI;

import com.powder.Client.Exception.UnexpectedResponseException;
import com.powder.Client.Exception.WrongPasswordException;
import com.powder.Client.Logic.HttpManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LogInController {

    @FXML
    private Button submitButton;
    @FXML
    private TextField urlInput;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Text actiontarget;

    public void initialize() {
        submitButton.setDefaultButton(true);
    }

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) {
        if (urlInput.getText().isEmpty()) {
            actiontarget.setText("URL cannot be empty.");
            actiontarget.setFill(Color.RED);
            return;
        }
        try {
            JSONObject jsonResponse = HttpManager.getInstance().establishConnection(urlInput.getText(), passwordField.getText());
            if (!jsonResponse.getBoolean("IsValid")) {
                throw new WrongPasswordException();
            }

            goToNewWindow(jsonResponse);

            Stage stage = (Stage) actiontarget.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            actiontarget.setText("Unable to connect with server");
            e.printStackTrace();
        } catch (WrongPasswordException | UnexpectedResponseException e) {
            actiontarget.setText(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            actiontarget.setText("Problem with response from server");
            e.printStackTrace();
        } finally {
            actiontarget.setFill(Color.RED);
        }
    }

    private void goToNewWindow(JSONObject jsonResponse) throws IOException, JSONException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_window.fxml"));
        Node root = loader.load();
        Stage newWindow = new Stage(StageStyle.DECORATED);
        newWindow.setScene(new Scene((Parent) root));
        newWindow.setTitle("FAKE SQL");
        MainWindowController controller = loader.getController();
        controller.initData(jsonResponse.getJSONObject("JsonObject"));
        newWindow.show();
    }
}
