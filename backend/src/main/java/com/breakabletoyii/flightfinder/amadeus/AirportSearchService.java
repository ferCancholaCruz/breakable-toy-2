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

//service used for looking for a specific airport
@Service
public class AirportSearchService {

    private final AuthService authService;
    private final HttpService httpService;

    private final ConcurrentHashMap<String, String> airportNameCache = new ConcurrentHashMap<>();

    public AirportSearchService(AuthService authService, HttpService httpService) {
        this.authService = authService;
        this.httpService = httpService;
    }

    //method used for looking for airport based on a keyword given by the user
    public List<AirportDTO> searchAirports(String keyword) {
        String token = authService.getAccessToken();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://test.api.amadeus.com/v1/reference-data/locations?subType=AIRPORT&keyword=" + encodedKeyword + "&page[limit]=5";

        //send the get petition
        String body = httpService.sendGet(url, token);

        JSONObject json = new JSONObject(body);

        //get the json info we need
        JSONArray data = json.getJSONArray("data");

        //iterate though each airport and get the info we need
        //and create a DTO
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

    //method for looking for airports based on the code
    public String getAirportNameByCode(String iataCode) {

        //check in cache if th name is available there
        if (airportNameCache.containsKey(iataCode)) {
            return airportNameCache.get(iataCode);
        }

        //get the token and create the url
        String token = authService.getAccessToken();
        String url = "https://test.api.amadeus.com/v1/reference-data/locations?subType=AIRPORT&keyword=" + iataCode;

        //send a get petition
        String body = httpService.sendGet(url, token);

        JSONObject json = new JSONObject(body);
        JSONArray data = json.getJSONArray("data");

        //if found, return the name
        String airportName = data.length() > 0
                ? data.getJSONObject(0).getString("name")
                : "Unknown Airport";

        airportNameCache.put(iataCode, airportName);
        return airportName;
    }
}


