package com.breakabletoyii.flightfinder.amadeus;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AirlineLookupService {

    private final AuthService authService;

    // cache in memory
    private final Map<String, String> airlineNameCache = new ConcurrentHashMap<>();

    public AirlineLookupService(AuthService authService) {
        this.authService = authService;
    }

    public String getAirlineName(String airlineCode) throws Exception {
        if (airlineNameCache.containsKey(airlineCode)) {
            return airlineNameCache.get(airlineCode);
        }

        String token = authService.getAccessToken();
        String url = "https://test.api.amadeus.com/v1/reference-data/airlines?airlineCodes=" + airlineCode;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            throw new RuntimeException("Amadeus rate limit exceeded (429): try again later.");
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Something wrong happened: " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray data = json.getJSONArray("data");

        String name = data.length() > 0
                ? data.getJSONObject(0).getString("businessName")
                : "Unknown airline";

        airlineNameCache.put(airlineCode, name);  // cache
        return name;
    }
}
