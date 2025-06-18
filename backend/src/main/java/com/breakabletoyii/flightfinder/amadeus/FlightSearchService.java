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
    private String latestSearchResponse;

    public FlightSearchService(ExtendedFlightOfferMapper mapper, AuthService authService, HttpService httpService) {
        this.mapper = mapper;
        this.authService = authService;
        this.httpService = httpService;
    }

    //use the cache
    //create a json object and get the data array
    public JSONObject getFlightById(String id) throws Exception {
        String cachedJson = this.latestSearchResponse;
        JSONArray data = new JSONObject(cachedJson).getJSONArray("data");

        //get the object with the desired id
        for (int i = 0; i < data.length(); i++) {
            JSONObject offer = data.getJSONObject(i);
            if (offer.getString("id").equals(id)) {
                return offer;
            }
        }

        throw new RuntimeException("Flight not found with id: " + id);
    }

    //build he json that is sent and het the auth token
    public List<FlightOfferDTO> searchFlights(Flight flight) {
        System.out.println("NUEVO CÓDIGO: prueba");
        JSONObject requestBody = buildRequestBody(flight);
        String token = authService.getAccessToken();

        try {
            String responseBody = httpService.sendPost(
                    "https://test.api.amadeus.com/v2/shopping/flight-offers",
                    token,
                    requestBody.toString()

            );

            System.out.println("Amadeus API Response:");
            System.out.println(responseBody);
            this.latestSearchResponse = responseBody;
            return mapper.mapFromJson(responseBody);

        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("\"status\": 429")) {
                System.out.println("Amadeus rate limit hit. Retrying after 1 second...");
                try {
                    Thread.sleep(2000); // wait for retry
                    String retryResponse = httpService.sendPost(
                            "https://test.api.amadeus.com/v2/shopping/flight-offers",
                            token,
                            requestBody.toString()
                    );
                    this.latestSearchResponse = retryResponse;
                    return mapper.mapFromJson(retryResponse);
                } catch (Exception retryEx) {
                    throw new RuntimeException("Retry after 429 failed.", retryEx);
                }
            }
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

        //System.out.println("Sending request to Amadeus with body:\n" + requestBody.toString(2));
        return requestBody;
    }

    //iterate every segment and gte the info we need for searching and displaying
    private JSONObject createFlightSegment(String id, String origin, String destination, String date) {
        JSONObject segment = new JSONObject();
        segment.put("id", id);
        segment.put("originLocationCode", origin);
        segment.put("destinationLocationCode", destination);
        segment.put("departureDateTimeRange", new JSONObject().put("date", date));
        return segment;
    }

    //create json object that allows to create a list of travelers
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

    //create a json with the searching criteria tha is needed
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
