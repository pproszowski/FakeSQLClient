package com.powder.Client.Logic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Type {
    private String name;
    private int limit;


    public Type(String _name, int _limit) {
        name = determineType(_name);
        limit = _limit;
    }

    public Type(JSONObject type) throws JSONException {
        name = determineType(type.getString("Name"));
        limit = type.getInt("Limit");
    }

    public static String determineType(String value) {
        switch (value.toLowerCase()) {
            case "string":
            case "varchar":
                return "string";
            case "number":
            case "int":
            case "integer":
            case "float":
            case "double":
                return "number";
            default:
                Pattern pattern = Pattern.compile("\\d+([.]\\d+)?");
                Matcher matcher = pattern.matcher(value);
                if (matcher.matches()) {
                    return "number";
                } else {
                    return "string";
                }
        }
    }

    public int getLimit() {
        return limit;
    }

    public String getName() {
        return Type.determineType(name.toLowerCase());
    }

    @Override
    public String toString() {
        return "(TYPE) : {" + name + " : " + limit + "}";
    }
}
