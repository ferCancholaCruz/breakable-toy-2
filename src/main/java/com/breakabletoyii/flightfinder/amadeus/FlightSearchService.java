package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.breakabletoyii.flightfinder.model.Flight;

@Service
public class FlightSearchService {

    public List<FlightOfferDTO> searchFlights(Flight flight) throws Exception {

        // Get the access token
        String token = AuthService.getAccessToken();

        // Build the JSON
        JSONObject requestBody = new JSONObject();

        requestBody.put("currencyCode", flight.getCurrency());

        JSONArray originDestinations = new JSONArray();
        JSONObject firstLeg = new JSONObject();

        firstLeg.put("id", "1");
        firstLeg.put("originLocationCode", flight.getDepartureCode());
        firstLeg.put("destinationLocationCode", flight.getArrivalCode());

        JSONObject departureRange = new JSONObject();
        departureRange.put("date", flight.getDepartureDate());
        firstLeg.put("departureDateTimeRange", departureRange);

        originDestinations.put(firstLeg);
        requestBody.put("originDestinations", originDestinations);

        JSONArray travelers = new JSONArray();
        for (int i = 1; i <= flight.getNumberAdults(); i++) {
            JSONObject traveler = new JSONObject();
            traveler.put("id", String.valueOf(i));
            traveler.put("travelerType", "ADULT");
            travelers.put(traveler);
        }
        requestBody.put("travelers", travelers);

        requestBody.put("sources", new JSONArray().put("GDS"));

        JSONObject searchCriteria = new JSONObject();
        searchCriteria.put("maxFlightOffers", 50);

        JSONObject filters = new JSONObject();
        JSONObject connection = new JSONObject();
        connection.put("nonStopPreferred", flight.isNonStop());
        filters.put("connectionRestriction", connection);

        searchCriteria.put("flightFilters", filters);
        requestBody.put("searchCriteria", searchCriteria);

        //Prepare and send the post petition
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://test.api.amadeus.com/v2/shopping/flight-offers"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //Check if theres any error
        if (response.statusCode() != 200) {
            throw new RuntimeException("Something wrong happened " + response.body());
        }

        return FlightOfferMapper.mapFromJson(response.body());
    }
}
