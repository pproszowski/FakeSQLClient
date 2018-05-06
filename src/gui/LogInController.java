package gui;

import com.powder.Client.HttpManager;
import com.powder.Client.Parser;
import com.powder.Exception.WrongPasswordException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class LogInController {

    @FXML private Button submitButton;
    @FXML private TextField urlInput;
    @FXML private PasswordField passwordField;
    @FXML private Text actiontarget;

    public void initialize(){
        submitButton.setDefaultButton(true);

    }
    @FXML protected void handleSubmitButtonAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_window.fxml"));

        try {
            JSONObject jsonResponse = HttpManager.getInstance().establishConnection(urlInput.getText(), passwordField.getText());
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
            e.printStackTrace();
        }
    }
}
