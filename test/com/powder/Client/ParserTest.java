package com.powder.Client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.powder.Client.Exception.IncorrectSyntaxException;
import com.powder.Client.Exception.InvalidKeyWordException;
import com.powder.Client.Logic.Parser;
import com.powder.Client.Logic.Tuple;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    @Test
    void createTable() throws IncorrectSyntaxException, InvalidKeyWordException, JSONException {
        String command = "CREATE TABLe tesT ( one int, seven string, sth int)";

        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("Database", jsonObject.getString("Target"));

        JSONObject query = jsonObject.getJSONObject("Query");
        assertEquals("addtable", query.getString("Operation"));

        JSONObject table = query.getJSONObject("Table");
        assertEquals("tesT", table.getString("Name"));
        assertEquals(3, table.getJSONArray("Columns").length());
        assertEquals(0, table.getJSONArray("Records").length());
    }

    @Test
    void createDatabase() throws IncorrectSyntaxException, InvalidKeyWordException, JSONException {
        String command = "CREATE DAtAbasE daTaaBaseE";

        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("Storage", jsonObject.getString("Target"));

        JSONObject query = jsonObject.getJSONObject("Query");
        assertEquals("adddatabase", query.getString("Operation"));
        JSONObject database = query.getJSONObject("Database");
        assertEquals(database.getString("Name"), "daTaaBaseE");
        assertEquals(0, database.getJSONArray("Tables").length());
    }

    @Test
    void selectAll() throws JSONException, InvalidKeyWordException, IncorrectSyntaxException {
        String command = "select * from table";
        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("table", jsonObject.getString("Target"));

        JSONObject jsonQuery = jsonObject.getJSONObject("Query");
        assertEquals("table", jsonQuery.getString("Name"));
        assertEquals("select", jsonQuery.getString("Operation"));
        assertEquals(1, jsonQuery.getJSONArray("ColumnNames").length());
        assertEquals(0, jsonQuery.getJSONArray("Records").length());
        assertEquals(0, jsonQuery.getJSONArray("Conditions").length());
    }

    @Test
    void select() throws JSONException, InvalidKeyWordException, IncorrectSyntaxException {
        String command = "select one, two, other, three from anoTheRTable";
        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("table", jsonObject.getString("Target"));

        JSONObject jsonQuery = jsonObject.getJSONObject("Query");
        assertEquals("anoTheRTable", jsonQuery.getString("Name"));
        assertEquals("select", jsonQuery.getString("Operation"));
        assertEquals(4, jsonQuery.getJSONArray("ColumnNames").length());
        assertEquals(0, jsonQuery.getJSONArray("Records").length());
        assertEquals(0, jsonQuery.getJSONArray("Conditions").length());
    }

    @Test
    void selectWithWhereClause() throws JSONException, InvalidKeyWordException, IncorrectSyntaxException {
        String command = "select one, two, other, three " +
                "from anoTheRTable " +
                "wheRe one = 1";
        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("table", jsonObject.getString("Target"));

        JSONObject jsonQuery = jsonObject.getJSONObject("Query");
        assertEquals("anoTheRTable", jsonQuery.getString("Name"));
        assertEquals("select", jsonQuery.getString("Operation"));
        assertEquals(4, jsonQuery.getJSONArray("ColumnNames").length());
        assertEquals(0, jsonQuery.getJSONArray("Records").length());
        assertEquals(1, jsonQuery.getJSONArray("Conditions").length());
    }

    @Test
    void updateCaseWithWhereClause() throws IncorrectSyntaxException, InvalidKeyWordException, JSONException {
        String command = "update TableName set one = 7, two = eight where one = 1 OR two = 2";

        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("table", jsonObject.getString("Target"));

        JSONObject jsonQuery = jsonObject.getJSONObject("Query");
        assertEquals("update", jsonQuery.getString("Operation"));

        JSONObject jsonMap = jsonQuery.getJSONObject("Changes");
        Map<String, Tuple> values;
        Type type = new TypeToken<Map<String, Tuple>>() {
        }.getType();
        values = (new Gson()).fromJson(String.valueOf(jsonMap), type);

        assertEquals(2, values.size());
        assertEquals("TableName", jsonQuery.getString("Name"));
        assertEquals(2, jsonQuery.getJSONArray("Conditions").length());

    }

    @Test
    void use() throws IncorrectSyntaxException, InvalidKeyWordException, JSONException {
        String command = "use testDatabase";
        JSONObject jsonObject = Parser.parseSQLtoJSON(command);

        assertEquals("Storage", jsonObject.getString("Target"));

        JSONObject jsonQuery = jsonObject.getJSONObject("Query");
        assertEquals("setcurrentdatabase", jsonQuery.getString("Operation"));
        assertEquals("testDatabase", jsonQuery.getString("Name"));
    }

    @Test
    void delete() throws IncorrectSyntaxException, InvalidKeyWordException, JSONException {
        String command = "delete from tablENamE where one = 1, two = seven";

        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("table", jsonObject.getString("Target"));

        JSONObject jsonQuery = jsonObject.getJSONObject("Query");
        assertEquals("tablENamE", jsonQuery.getString("Name"));
        assertEquals(2, jsonQuery.getJSONArray("Conditions").length());
        assertEquals("delete", jsonQuery.getString("Operation"));
    }

    @Test
    void drop() throws IncorrectSyntaxException, InvalidKeyWordException, JSONException {
        String command = "drop table tAbLEname";
        JSONObject jsonObject = Parser.parseSQLtoJSON(command);
        assertEquals("Database", jsonObject.getString("Target"));

        JSONObject jsonQuery = jsonObject.getJSONObject("Query");
        assertEquals("droptable", jsonQuery.getString("Operation"));
        assertEquals("tAbLEname", jsonQuery.getString("Name"));
    }

}
