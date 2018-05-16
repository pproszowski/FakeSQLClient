package com.powder.Client.Logic;

import com.powder.Client.Exception.UnexpectedResponseException;
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
    private static HttpManager instance = null;
    private URL url;
    private URLConnection con;

    public static HttpManager getInstance() {
        if (instance == null) {
            instance = new HttpManager();
        }
        return instance;
    }

    public void sendRequest(String message) throws IOException {
        con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        byte[] out = message.getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
            os.flush();
        }
    }

    public JSONObject getResponse() throws IOException, UnexpectedResponseException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        try {
            return new JSONObject(response.toString());
        } catch (JSONException e) {
            throw new UnexpectedResponseException();
        }
    }

    public JSONObject establishConnection(String urlInput, String password) throws IOException, UnexpectedResponseException {
        if (!urlInput.contains("http://")) {
            urlInput = "http://" + urlInput;
        }
        url = new URL(urlInput);
        String message = "{\"Type\":\"Authentication\",\"password\":\"" + password + "\"}";
        sendRequest(message);

        return getResponse();

    }

}
