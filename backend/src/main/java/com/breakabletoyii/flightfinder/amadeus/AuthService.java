package com.breakabletoyii.flightfinder.amadeus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import com.breakabletoyii.flightfinder.config.AmadeusApiConfig;

import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//service for getting access token
@Service
public class AuthService {

    private final AmadeusApiConfig config;
    private String accessToken;
    private long tokenExpiration = 0;

    public AuthService(AmadeusApiConfig config) {
        this.config = config;
    }

    //Getting access token
    public String getAccessToken() {
        try {
            //If token is still valid return the same token
            if (accessToken != null && System.currentTimeMillis() < tokenExpiration) {
                return accessToken;
            }

            //Build the request
            String bodyRequest = "grant_type=client_credentials" +
                    "&client_id=" + URLEncoder.encode(config.getKey(), StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(config.getSecret(), StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://test.api.amadeus.com/v1/security/oauth2/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyRequest))
                    .build();

            //Create a new client to send request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //if we cant have the token we send an exception
            if (response.statusCode() != 200) {
                throw new RuntimeException("Can not obtain token: " + response.body());
            }

            //get the token form the json and obtain expiration
            JSONObject json = new JSONObject(response.body());
            accessToken = json.getString("access_token");
            int expiration = json.getInt("expires_in");
            tokenExpiration = System.currentTimeMillis() + (expiration * 1000L) - 5000;

            return accessToken;

            //manage the exceptions
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error while connecting with Amadeus API", e);
        } catch (JSONException e) {
            throw new RuntimeException("Error while processing the JSON", e);
        }
    }
}

