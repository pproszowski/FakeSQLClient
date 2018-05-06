package com.powder.Client;

import com.powder.Exception.IncorrectSyntaxException;
import com.powder.Exception.InvalidKeyWordException;
import com.powder.Exception.UnknownTypeException;
import com.powder.Exception.WrongPasswordException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class HttpManager {
    private URL url;
    private URLConnection con;
    private static HttpManager instance = null;

    public static HttpManager getInstance() {
        if(instance == null){
            instance = new HttpManager();
        }
        return instance;
    }
    public void sendRequest(String message) throws IOException {
        con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        byte[] out = message.getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
            os.flush();
        }
}
    public JSONObject getResponse() throws IOException, JSONException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONObject(response.toString());
    }

    public JSONObject establishConnection(String urlInput, String password) throws IOException, WrongPasswordException, JSONException {
        if(!urlInput.contains("http://")){
            urlInput = "http://" + urlInput;
        }
        url = new URL(urlInput);
        String message = "{\"Type\":\"Authentication\",\"password\":\"" + password + "\"}";
        sendRequest(message);

        return getResponse();

    }

}
