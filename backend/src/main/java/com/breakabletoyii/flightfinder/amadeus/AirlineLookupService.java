package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.amadeus.HttpService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AirlineLookupService {

    private final AuthService authService;
    private final HttpService httpService;

    private final Map<String, String> airlineNameCache = new ConcurrentHashMap<>();

    public AirlineLookupService(AuthService authService, HttpService httpService) {
        this.authService = authService;
        this.httpService = httpService;
    }

    public String getAirlineName(String airlineCode) {
        if (airlineNameCache.containsKey(airlineCode)) {
            return airlineNameCache.get(airlineCode);
        }

        String token = authService.getAccessToken();
        String url = "https://test.api.amadeus.com/v1/reference-data/airlines?airlineCodes=" + airlineCode;

        String responseBody = httpService.sendGet(url, token);

        JSONObject json = new JSONObject(responseBody);
        JSONArray data = json.getJSONArray("data");

        String name = data.length() > 0
                ? data.getJSONObject(0).getString("businessName")
                : "Unknown airline";

        airlineNameCache.put(airlineCode, name);
        return name;
    }
}
