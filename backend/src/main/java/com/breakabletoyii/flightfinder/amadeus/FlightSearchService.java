package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import com.breakabletoyii.flightfinder.model.Flight;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class FlightSearchService {

    private final AuthService authService;
    private final ExtendedFlightOfferMapper mapper;
    private final HttpService httpService;

    public FlightSearchService(ExtendedFlightOfferMapper mapper, AuthService authService, HttpService httpService) {
        this.mapper = mapper;
        this.authService = authService;
        this.httpService = httpService;
    }

    public List<FlightOfferDTO> searchFlights(Flight flight) {
        try {
            String token = authService.getAccessToken();
            JSONObject requestBody = buildRequestBody(flight);

            String responseBody = httpService.sendPost(
                    "https://test.api.amadeus.com/v2/shopping/flight-offers",
                    token,
                    requestBody.toString()
            );

            return mapper.mapFromJson(responseBody);

        } catch (Exception e) {
            throw new RuntimeException("Error while calling Amadeus API", e);
        }
    }

    private JSONObject buildRequestBody(Flight flight) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("currencyCode", flight.getCurrency());

        JSONArray originDestinations = new JSONArray();
        originDestinations.put(createFlightSegment("1", flight.getDepartureCode(), flight.getArrivalCode(), flight.getDepartureDate()));

        if (flight.getReturnDate() != null && !flight.getReturnDate().isEmpty()) {
            originDestinations.put(createFlightSegment("2", flight.getArrivalCode(), flight.getDepartureCode(), flight.getReturnDate()));
        }

        requestBody.put("originDestinations", originDestinations);
        requestBody.put("travelers", createTravelers(flight.getNumberAdults()));
        requestBody.put("sources", new JSONArray().put("GDS"));
        requestBody.put("searchCriteria", buildSearchCriteria(flight));

        System.out.println("Sending request to Amadeus with body:\n" + requestBody.toString(2));
        return requestBody;
    }

    private JSONObject createFlightSegment(String id, String origin, String destination, String date) {
        JSONObject segment = new JSONObject();
        segment.put("id", id);
        segment.put("originLocationCode", origin);
        segment.put("destinationLocationCode", destination);
        segment.put("departureDateTimeRange", new JSONObject().put("date", date));
        return segment;
    }

    private JSONArray createTravelers(int numberOfAdults) {
        JSONArray travelers = new JSONArray();
        for (int i = 1; i <= numberOfAdults; i++) {
            JSONObject traveler = new JSONObject();
            traveler.put("id", String.valueOf(i));
            traveler.put("travelerType", "ADULT");
            travelers.put(traveler);
        }
        return travelers;
    }

    private JSONObject buildSearchCriteria(Flight flight) {
        JSONObject searchCriteria = new JSONObject();
        searchCriteria.put("maxFlightOffers", 50);

        if (flight.isNonStop()) {
            JSONObject filters = new JSONObject();
            filters.put("connectionRestriction", new JSONObject().put("maxNumberOfConnections", 0));
            searchCriteria.put("flightFilters", filters);
        }

        return searchCriteria;
    }
}
