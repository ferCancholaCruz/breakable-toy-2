package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.AirportDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class AirportSearchService {


    private final AuthService authService;

    private final ConcurrentHashMap<String, String> airportNameCache = new ConcurrentHashMap<>();

    public AirportSearchService(AuthService authService) {
        this.authService = authService;
    }

    public List<AirportDTO> searchAirports(String keyword) throws Exception{


        // Get the access token
        String token = authService.getAccessToken();

        //Build the url
       // keyword = "mad";  //the user types the keyword
        String url = "https://test.api.amadeus.com/v1/reference-data/locations?subType=AIRPORT&keyword=" + keyword + "&page[limit]=5";

        //build the petition
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)

                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Amadeus API Response:");
        System.out.println(response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Something wrong happened " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray data = json.getJSONArray("data");

        List <AirportDTO> airports = new ArrayList<>();
        for (int i=0; i<data.length(); i++){
            JSONObject airport = data.getJSONObject(i);
            String code = airport.getString("iataCode");
            String name = airport.getString("name");
            String city = airport.getJSONObject("address").getString("cityName");

            airports.add(new AirportDTO(code,name,city));
        }

        return airports;
    }

    public String getAirportNameByCode(String iataCode) throws Exception {
        if (airportNameCache.containsKey(iataCode)) {
            return airportNameCache.get(iataCode);
        }

        String token = authService.getAccessToken();
        String url = "https://test.api.amadeus.com/v1/reference-data/locations?subType=AIRPORT&keyword=" + iataCode;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error fetching airport name: " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray data = json.getJSONArray("data");

        String airportName = data.length() > 0
                ? data.getJSONObject(0).getString("name")
                : "Unknown Airport";

        airportNameCache.put(iataCode, airportName); // Store in cache
        return airportName;
    }

}


