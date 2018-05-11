
package com.powder.Client;

import com.powder.Client.Exception.IncorrectSyntaxException;
import com.powder.Client.Exception.InvalidKeyWordException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("Database", jsonObject.getString("Target"));

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
}
