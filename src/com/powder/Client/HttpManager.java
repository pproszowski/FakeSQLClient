package com.powder.Client;

import com.powder.Exception.IncorrectSyntaxException;
import com.powder.Exception.InvalidKeyWordException;
import com.powder.Exception.UnknownTypeException;
import org.json.JSONException;

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
    private String login;
    private String password;

    private HttpManager() throws IOException {
        url = new URL("http://localhost:8080");
        con = url.openConnection();
    }

    public static HttpManager getInstance() throws IOException {
        if(instance == null){
            return new HttpManager();
        }else{
            return instance;
        }
    }
    public void sendRequest(String command){
        try {
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            byte[] out = Parser.parseSQLtoJSON(command).toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
                os.flush();
            }

        } catch (IncorrectSyntaxException | UnknownTypeException | JSONException | InvalidKeyWordException | IOException e) {
            e.printStackTrace();
        }
    }
    public String getResponse() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return(response.toString().replaceAll("<br>", "\n"));
    }
}
