package com.breakabletoyii.flightfinder.amadeus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;


import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONException;

public class AuthService {
    private static final String key_id = "AVcLtjI6so0WjWgqO8oo23vQf4m1GCDH";
    private static final String secret_id = "oTGfCPFhH4rrEf0F";
    private static final String token_url = "https://test.api.amadeus.com/v1/security/oauth2/token";

    private static String accessToken;
    private static long tokenExpiration = 0;

    //Getting access token
    public static String getAccessToken() {
        try {
            //If token is still valid
            if (accessToken != null && System.currentTimeMillis() < tokenExpiration) {
                return accessToken;
            }

            String bodyRequest = "grant_type=client_credentials&client_id=" +
                    URLEncoder.encode(key_id, StandardCharsets.UTF_8) +
                    "&client_secret=" +
                    URLEncoder.encode(secret_id, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(token_url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyRequest))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //if we cant have the token we send an exception
            if (response.statusCode() != 200) {
                throw new RuntimeException("Can not obtain token: " + response.body());
            }

            JSONObject json = new JSONObject(response.body());
            accessToken = json.getString("access_token");
            int expiration = json.getInt("expires_in");
            tokenExpiration = System.currentTimeMillis() + (expiration * 1000L) - 5000;

            return accessToken;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error while connecting with Amadeus API", e);
        } catch (JSONException e) {
            throw new RuntimeException("Error while processing the JSON", e);
        }
    }
}

