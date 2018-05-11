package com.powder.Client.Gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.powder.Client.Exception.IncorrectSyntaxException;
import com.powder.Client.Exception.InvalidKeyWordException;
import com.powder.Client.HttpManager;
import com.powder.Client.Parser;
import com.powder.Client.Tuple;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindowController {
    @FXML private VBox vbox;
    @FXML private ComboBox chooseDatabaseComboBox;
    @FXML private ListView tablesListView;
    @FXML private Label labelResponse;
    @FXML private TextArea commandInput;
    @FXML private Button sendRequestButton;
    private TableView tableResponse;

    public void initialize(){
        commandInput.setWrapText(true);
        labelResponse.setVisible(false);
        labelResponse.setWrapText(true);
    }

    public void sendRequest(MouseEvent mouseEvent) throws InvalidKeyWordException {
        try {
            HttpManager httpManager = HttpManager.getInstance();
            String message = Parser.parseSQLtoJSON(commandInput.getText()).toString();
            httpManager.sendRequest(message);
            JSONObject jsonResponse = httpManager.getResponse();
            if(jsonResponse.getBoolean("IsValid")){
                if(tableResponse != null){
                    vbox.getChildren().remove(tableResponse);
                }
                if(jsonResponse.isNull("JsonObject")){
                    labelResponse.setVisible(true);
                    labelResponse.setText(jsonResponse.getString("Message"));
                }else{
                    tableResponse = new TableView<ObservableList<String>>();
                    labelResponse.setVisible(false);
                    tableResponse.setVisible(true);
                    JSONObject jsonTable = jsonResponse.getJSONObject("JsonObject");
                    JSONArray jsonColumns = jsonTable.getJSONArray("Columns");
                    for(int i = 0; i < jsonColumns.length(); i++){
                        final int finalIndex = i;
                        TableColumn<ObservableList<String>, String> tableColumn = new TableColumn<>(jsonColumns.getJSONObject(i).getString("Name"));
                        tableColumn.setCellValueFactory(param ->
                                new ReadOnlyObjectWrapper<>(param.getValue().get(finalIndex))
                        );
                        tableResponse.getColumns().add(tableColumn);
                    }

                    JSONArray jsonRecords = jsonTable.getJSONArray("Records");
                    Map<String, Tuple> values;
                    for(int i = 0; i < jsonRecords.length(); i++){
                        JSONObject record = jsonRecords.getJSONObject(i);
                        values = new Gson().fromJson(
                                record.toString(), new TypeToken<HashMap<String, Tuple>>(){}.getType()
                        );
                        List<Tuple> listOfValues = new ArrayList<>(values.values());
                        List<String> listOfStrings = new ArrayList<>();
                        for(Tuple tuple : listOfValues){
                            if(tuple.getValue() != null){
                                listOfStrings.add(tuple.getValue().toString());
                            }else{
                                listOfStrings.add("null");
                            }
                        }
                        tableResponse.getItems().add(
                                FXCollections.observableArrayList(listOfStrings)
                        );
                    }
                    tableResponse.setPrefHeight(400.0);
                    tableResponse.setPrefWidth(700.0);
                    vbox.getChildren().add(tableResponse);
                }
            }else{
                return;
            }
            message = "{\"Type\":\"Update\"}";
            httpManager.sendRequest(message);
            jsonResponse = httpManager.getResponse().getJSONObject("JsonObject");
            JSONArray databases = jsonResponse.getJSONArray("DatabaseNames");
            List<String> databaseNames = getStringListFromJSONArray(databases);
            Object currentDatabase = chooseDatabaseComboBox.getValue();
            chooseDatabaseComboBox.getItems().clear();
            chooseDatabaseComboBox.setItems(FXCollections.observableList(databaseNames));
            if(chooseDatabaseComboBox.getItems().contains(currentDatabase)){
                chooseDatabaseComboBox.setValue(currentDatabase);
            }else{
                if(chooseDatabaseComboBox.getItems().size() > 0){
                    chooseDatabaseComboBox.setValue(chooseDatabaseComboBox.getItems().get(0));
                }
            }

            JSONArray tables = jsonResponse.getJSONArray("TableNames");
            List<String> tableNames = getStringListFromJSONArray(tables);
            tablesListView.setItems(FXCollections.observableList(tableNames));
        } catch (IOException | InvalidKeyWordException | JSONException | IncorrectSyntaxException e) {
            System.out.println("elo");
            labelResponse.setText(e.getMessage());
            if(tableResponse != null){
                vbox.getChildren().remove(tableResponse);
            }
            labelResponse.setTextFill(Color.RED);
            labelResponse.setVisible(true);
        }
    }

    void initData(JSONObject jsonResponse) throws JSONException {
        JSONArray jsonArray = jsonResponse.getJSONArray("DatabaseNames");
        List<String> databaseNames = getStringListFromJSONArray(jsonArray);
        ObservableList<String> databases = FXCollections.observableArrayList(databaseNames);
        chooseDatabaseComboBox.setItems(databases);
        chooseDatabaseComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            tablesListView.getItems().clear();
            try {
                JSONObject message = Parser.parseSQLtoJSON("use " + newValue);
                HttpManager.getInstance().sendRequest(message.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyWordException | IncorrectSyntaxException e) {
                if(tableResponse != null){
                    vbox.getChildren().remove(tableResponse);
                }
                labelResponse.setVisible(true);
                labelResponse.setText(e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                JSONObject _jsonResponse = HttpManager.getInstance().getResponse();
                if(_jsonResponse.getBoolean("IsValid")){
                    JSONObject jsonDatabase = _jsonResponse.getJSONObject("JsonObject");
                    JSONArray jsonTables = jsonDatabase.getJSONArray("Tables");
                    List<String> tableNames = getStringListFromJSONArray(jsonTables);
                    ObservableList<String> observableList = FXCollections.observableList(tableNames);
                    tablesListView.setItems(observableList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        chooseDatabaseComboBox.setValue(databases.get(0));
    }

    private List<String> getStringListFromJSONArray(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for(int i = 0; i <jsonArray.length(); i++){
            list.add(jsonArray.getString(i));
        }

        return list;
    }
}
