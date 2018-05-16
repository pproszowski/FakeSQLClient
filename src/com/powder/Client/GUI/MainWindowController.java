package com.powder.Client.GUI;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.powder.Client.Exception.IncorrectSyntaxException;
import com.powder.Client.Exception.InvalidKeyWordException;
import com.powder.Client.Exception.UnexpectedResponseException;
import com.powder.Client.Logic.HttpManager;
import com.powder.Client.Logic.Parser;
import com.powder.Client.Logic.Tuple;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class MainWindowController {
    @FXML
    private Button sendRequestButton;
    @FXML
    private VBox vbox;
    @FXML
    private ComboBox chooseDatabaseComboBox;
    @FXML
    private ListView<String> tablesListView;
    @FXML
    private Label labelResponse;
    @FXML
    private TextArea commandInput;
    private TableView tableResponse;
    private HttpManager httpManager;
    private String message;

    public void initialize() {
        commandInput.setWrapText(true);
        labelResponse.setVisible(false);
        labelResponse.setWrapText(true);
        httpManager = HttpManager.getInstance();
        message = "";
    }

    public void sendRequest(ActionEvent actionEvent) {
        try {
            message = Parser.parseSQLtoJSON(commandInput.getText()).toString();
            httpManager.sendRequest(message);
            JSONObject jsonResponse = httpManager.getResponse();
            System.out.println(jsonResponse.toString());
            handleResponse(jsonResponse);
        } catch (JSONException e) {
            setLabelMessage(new UnexpectedResponseException().getMessage(), Color.RED);
            e.printStackTrace();
        } catch (IncorrectSyntaxException | InvalidKeyWordException | UnexpectedResponseException e) {
            setLabelMessage(e.getMessage(), Color.RED);
        } catch (IOException e) {
            setLabelMessage("Unable to connect with server. Please, check connection with internet", Color.RED);
            e.printStackTrace();
        }
    }

    private void handleResponse(JSONObject jsonResponse) throws JSONException {

        boolean isValid = jsonResponse.getBoolean("IsValid");
        String currentDatabaseName = null;

        if (isValid) {
            if (jsonResponse.isNull("JsonObject")) {
                setLabelMessage(jsonResponse.getString("Message"), Color.GREEN);
            } else {
                JSONObject jsonObject = jsonResponse.getJSONObject("JsonObject");
                if (!jsonObject.isNull("Columns")) {
                    setTableMessage(jsonObject.getJSONArray("Columns"), jsonObject.getJSONArray("Records"));
                } else {
                    currentDatabaseName = jsonObject.getString("Name");
                }
            }
            updateTables(currentDatabaseName);
        } else {
            setLabelMessage(jsonResponse.getString("Message"), Color.RED);
        }
    }

    private void updateTables(String currentDatabaseName) throws JSONException {
        Object currentDatabase = chooseDatabaseComboBox.getValue();
        message = "{\"Type\":\"Update\"}";
        try {
            httpManager.sendRequest(message);
            JSONObject jsonObject = httpManager.getResponse();
            if(jsonObject.isNull("JsonObject")){
                chooseDatabaseComboBox.getItems().clear();
                return;
            }
            JSONObject jsonResponse = jsonObject.getJSONObject("JsonObject");
            JSONArray databases = jsonResponse.getJSONArray("DatabaseNames");
            List<String> databaseNames = getStringListFromJSONArray(databases);

            chooseDatabaseComboBox.getItems().clear();
            chooseDatabaseComboBox.setItems(FXCollections.observableList(databaseNames));
            if (chooseDatabaseComboBox.getItems().contains(currentDatabaseName)) {
                chooseDatabaseComboBox.setValue(currentDatabaseName);
            }else{
                if(chooseDatabaseComboBox.getItems().contains(currentDatabase)){
                    chooseDatabaseComboBox.setValue(currentDatabase);
                }else{
                    if(chooseDatabaseComboBox.getItems().size() > 0){
                        chooseDatabaseComboBox.setValue(chooseDatabaseComboBox.getItems().get(0));
                    }
                }
            }

            JSONArray tables = jsonResponse.getJSONArray("TableNames");
            List<String> tableNames = getStringListFromJSONArray(tables);
            tablesListView.setItems(FXCollections.observableList(tableNames));
        } catch (IOException e) {
            setLabelMessage(e.getMessage(), Color.RED);
            e.printStackTrace();
        } catch (UnexpectedResponseException e) {
            e.printStackTrace();
        }
    }

    private void addRecordsToResponseTable(JSONArray jsonRecords) throws JSONException {
        if (!Objects.nonNull(jsonRecords)) {
            return;
        }
        Map<String, Tuple> values;
        for (int i = 0; i < jsonRecords.length(); i++) {
            JSONObject record = jsonRecords.getJSONObject(i);
            values = new Gson().fromJson(
                    record.toString(), new TypeToken<HashMap<String, Tuple>>() {
                    }.getType()
            );
            List<Tuple> listOfValues = new ArrayList<>(values.values());
            List<String> listOfStrings = new ArrayList<>();
            for (Tuple tuple : listOfValues) {
                if (tuple.getValue() != null) {
                    listOfStrings.add(tuple.getValue().toString());
                } else {
                    listOfStrings.add("null");
                }
            }
            tableResponse.getItems().add(
                    FXCollections.observableArrayList(listOfStrings)
            );
        }
    }

    private void addColumnsToResponseTable(JSONArray jsonColumns) throws JSONException {
        for (int i = 0; i < jsonColumns.length(); i++) {
            final int finalIndex = i;
            TableColumn<ObservableList<String>, String> tableColumn = new TableColumn<>(jsonColumns.getJSONObject(i).getString("Name"));
            tableColumn.setCellValueFactory(param -> {
                if (!param.getValue().isEmpty()) {
                    return new ReadOnlyObjectWrapper<>(param.getValue().get(finalIndex));
                } else {
                    return null;
                }
            });
            tableResponse.getColumns().add(tableColumn);
        }
    }

    void initData(JSONObject jsonResponse) throws JSONException {
        JSONArray jsonArray = jsonResponse.getJSONArray("DatabaseNames");
        List<String> databaseNames = getStringListFromJSONArray(jsonArray);
        ObservableList<String> databases = FXCollections.observableArrayList(databaseNames);
        commandInput.requestFocus();
        chooseDatabaseComboBox.setItems(databases);
        chooseDatabaseComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            tablesListView.getItems().clear();
            try {
                JSONObject message = Parser.parseSQLtoJSON("use " + newValue);
                HttpManager.getInstance().sendRequest(message.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyWordException | IncorrectSyntaxException e) {
                if (tableResponse != null) {
                    vbox.getChildren().remove(tableResponse);
                }
                labelResponse.setVisible(true);
                labelResponse.setText(e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject _jsonResponse = HttpManager.getInstance().getResponse();
                if (_jsonResponse.getBoolean("IsValid")) {
                    JSONObject jsonDatabase = _jsonResponse.getJSONObject("JsonObject");
                    JSONArray jsonTables = jsonDatabase.getJSONArray("Tables");
                    List<String> tableNames = getStringListFromJSONArray(jsonTables);
                    ObservableList<String> observableList = FXCollections.observableList(tableNames);
                    tablesListView.setItems(observableList);
                }
            } catch (IOException e) {
                setLabelMessage(e.getMessage(), Color.RED);
                e.printStackTrace();
            } catch (UnexpectedResponseException | JSONException e) {
                setLabelMessage(new UnexpectedResponseException().getMessage(), Color.RED);
                e.printStackTrace();
            }
        });

        if (databases.size() > 0) {
            chooseDatabaseComboBox.setValue(databases.get(0));
        }

        Scene scene = sendRequestButton.getScene();
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {
                sendRequestButton.fire();
            }
        });

        commandInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {
                sendRequestButton.fire();
            }
        });
    }

    private List<String> getStringListFromJSONArray(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }

        return list;
    }

    private void setLabelMessage(String message) {
        setLabelMessage(message, Color.BLACK);
    }

    private void setLabelMessage(String message, Paint color) {
        clearResponseView();
        labelResponse.setVisible(true);
        labelResponse.setText(message);
        labelResponse.setTextFill(color);
    }

    private void setTableMessage(JSONArray jsonColumns, JSONArray jsonRecords) throws JSONException {
        clearResponseView();
        tableResponse = new TableView<ObservableList<String>>();
        labelResponse.setVisible(false);
        tableResponse.setVisible(true);
        addColumnsToResponseTable(jsonColumns);
        addRecordsToResponseTable(jsonRecords);
        tableResponse.setPrefHeight(400.0);
        tableResponse.setPrefWidth(900.0);
        vbox.getChildren().add(tableResponse);
    }

    private void clearResponseView() {
        labelResponse.setVisible(false);
        if (tableResponse != null) {
            vbox.getChildren().remove(tableResponse);
        }
    }
}
