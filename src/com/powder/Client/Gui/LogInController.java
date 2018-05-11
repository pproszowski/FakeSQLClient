package com.powder.Client.Gui;

import com.powder.Client.HttpManager;
import com.powder.Client.Exception.WrongPasswordException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LogInController {

    @FXML private Button submitButton;
    @FXML private TextField urlInput;
    @FXML private PasswordField passwordField;
    @FXML private Text actiontarget;

    public void initialize(){
        submitButton.setDefaultButton(true);

    }
    @FXML protected void handleSubmitButtonAction(ActionEvent event) throws IOException {
        if(urlInput.getText().isEmpty()){
            actiontarget.setText("URL cannot be empty.");
            actiontarget.setFill(Color.RED);
            return;
        }
        try {
            JSONObject jsonResponse = HttpManager.getInstance().establishConnection(urlInput.getText(), passwordField.getText());
            if(!jsonResponse.getBoolean("IsValid")){
                throw new WrongPasswordException();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main_window.fxml"));
            Stage newWindow = new Stage(StageStyle.DECORATED);
            newWindow.setScene(new Scene( (Pane) loader.load()));
            newWindow.setTitle("FAKE SQL");
            MainWindowController controller = loader.<MainWindowController>getController();
            controller.initData(jsonResponse.getJSONObject("JsonObject"));
            newWindow.show();
            Stage stage = (Stage) actiontarget.getScene().getWindow();
            stage.close();
        }catch (IOException e){
            actiontarget.setText("Unable to connect with server");
        } catch (WrongPasswordException e) {
            actiontarget.setText(e.getMessage());
        } catch (JSONException e) {
            actiontarget.setText("Problem with response from server");
        }finally {
            actiontarget.setFill(Color.RED);
        }
    }
}
