package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import com.breakabletoyii.flightfinder.model.Flight;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class FlightSearchService {

    private final AuthService authService;
    private final FlightOfferMapper mapper;

    public FlightSearchService(FlightOfferMapper mapper, AuthService authService) {
        this.mapper = mapper;
        this.authService = authService;
    }

    public List<FlightOfferDTO> searchFlights(Flight flight) throws Exception {
        // Get the access token
        String token = authService.getAccessToken();
        System.out.println("Access token obtained.");

        // Build the JSON
        JSONObject requestBody = new JSONObject();

        requestBody.put("currencyCode", flight.getCurrency());

        JSONArray originDestinations = new JSONArray();

        // Add outbound flight
        JSONObject outbound = new JSONObject();
        outbound.put("id", "1");
        outbound.put("originLocationCode", flight.getDepartureCode());
        outbound.put("destinationLocationCode", flight.getArrivalCode());
        outbound.put("departureDateTimeRange", new JSONObject().put("date", flight.getDepartureDate()));
        originDestinations.put(outbound);

        // add return flight if provided
        if (flight.getReturnDate() != null && !flight.getReturnDate().isEmpty()) {
            JSONObject inbound = new JSONObject();
            inbound.put("id", "2");
            inbound.put("originLocationCode", flight.getArrivalCode());
            inbound.put("destinationLocationCode", flight.getDepartureCode());
            inbound.put("departureDateTimeRange", new JSONObject().put("date", flight.getReturnDate()));
            originDestinations.put(inbound);
        }

        requestBody.put("originDestinations", originDestinations);

        // Add travelers
        JSONArray travelers = new JSONArray();
        for (int i = 1; i <= flight.getNumberAdults(); i++) {
            JSONObject traveler = new JSONObject();
            traveler.put("id", String.valueOf(i));
            traveler.put("travelerType", "ADULT");
            travelers.put(traveler);
        }
        requestBody.put("travelers", travelers);

        // Other options
        requestBody.put("sources", new JSONArray().put("GDS"));

        JSONObject searchCriteria = new JSONObject();
        searchCriteria.put("maxFlightOffers", 50);

        JSONObject filters = new JSONObject();
        filters.put("connectionRestriction", new JSONObject().put("nonStopPreferred", flight.isNonStop()));
        searchCriteria.put("flightFilters", filters);

        requestBody.put("searchCriteria", searchCriteria);

        System.out.println("Sending request to Amadeus with body:\n" + requestBody.toString(2));

        // Prepare and send the POST request
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)) // ⏱️ Avoid hanging
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://test.api.amadeus.com/v2/shopping/flight-offers"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response status code: " + response.statusCode());

        if (response.statusCode() != 200) {
            System.err.println("API Error: " + response.body());
            throw new RuntimeException("Something went wrong. Response: " + response.body());
        }

        return mapper.mapFromJson(response.body());
    }
}
