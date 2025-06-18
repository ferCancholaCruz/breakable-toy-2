package com.breakabletoyii.flightfinder.amadeus;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

@Service
public class HttpService {

    private final HttpClient client;

    public HttpService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    //method for sending the get petition
    public String sendGet(String url, String bearerToken) {
        try {
            //build petition with url and token
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + bearerToken)
                    .build();

            //keep the response and send exceptions if its an error
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("API Error: " + response.body());
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    //method for sending a post request with a body in json
    public String sendPost(String url, String bearerToken, String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("API Error: " + response.body());
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP POST request failed: " + e.getMessage(), e);
        }
    }

}
