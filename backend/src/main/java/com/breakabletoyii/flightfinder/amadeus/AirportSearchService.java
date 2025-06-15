package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.AirportDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
public class AirportSearchService {

    private final AuthService authService;
    private final HttpService httpService;

    private final ConcurrentHashMap<String, String> airportNameCache = new ConcurrentHashMap<>();

    public AirportSearchService(AuthService authService, HttpService httpService) {
        this.authService = authService;
        this.httpService = httpService;
    }

    public List<AirportDTO> searchAirports(String keyword) {
        String token = authService.getAccessToken();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://test.api.amadeus.com/v1/reference-data/locations?subType=AIRPORT&keyword=" + encodedKeyword + "&page[limit]=5";

        String body = httpService.sendGet(url, token);

        JSONObject json = new JSONObject(body);
        JSONArray data = json.getJSONArray("data");

        List<AirportDTO> airports = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject airport = data.getJSONObject(i);
            String code = airport.getString("iataCode");
            String name = airport.getString("name");
            String city = airport.getJSONObject("address").getString("cityName");
            airports.add(new AirportDTO(code, name, city));
        }

        return airports;
    }

    public String getAirportNameByCode(String iataCode) {
        if (airportNameCache.containsKey(iataCode)) {
            return airportNameCache.get(iataCode);
        }

        String token = authService.getAccessToken();
        String url = "https://test.api.amadeus.com/v1/reference-data/locations?subType=AIRPORT&keyword=" + iataCode;

        String body = httpService.sendGet(url, token);

        JSONObject json = new JSONObject(body);
        JSONArray data = json.getJSONArray("data");

        String airportName = data.length() > 0
                ? data.getJSONObject(0).getString("name")
                : "Unknown Airport";

        airportNameCache.put(iataCode, airportName);
        return airportName;
    }
}


