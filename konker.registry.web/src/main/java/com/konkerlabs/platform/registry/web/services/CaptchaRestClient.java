package com.konkerlabs.platform.registry.web.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class CaptchaRestClient {

    public String request(URI uri) throws IOException {

        URL url = uri.toURL();
        HttpURLConnection conn = null;

        try {

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            OutputStream os = conn.getOutputStream();
            // os.write(byteOut.toByteArray());
            os.flush();

            if (conn.getResponseCode() != 200) {
                throw new IOException(readFullStream(conn.getErrorStream()));
            } else {
                return readFullStream(conn.getInputStream());
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    private String readFullStream(InputStream data) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data, "UTF-8"));

        StringBuilder readData = new StringBuilder();
        String readLine;

        while((readLine = bufferedReader.readLine()) != null) {
            readData.append(readLine);
        }

        return readData.toString();

    }

}
