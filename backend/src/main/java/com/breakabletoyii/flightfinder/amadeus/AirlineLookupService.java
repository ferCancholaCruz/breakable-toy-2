package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.amadeus.HttpService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//service to look for airlines with name, used in autocomplete
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

        //get the access token
        String token = authService.getAccessToken();

        //build the url with the airline code
        String url = "https://test.api.amadeus.com/v1/reference-data/airlines?airlineCodes=" + airlineCode;

        String responseBody = httpService.sendGet(url, token);

        JSONObject json = new JSONObject(responseBody);
        JSONArray data = json.getJSONArray("data");

        //if foud, return the airline name
        String name = data.length() > 0
                ? data.getJSONObject(0).getString("businessName")
                : "Unknown airline";

        airlineNameCache.put(airlineCode, name);
        return name;
    }
}
