package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import com.breakabletoyii.flightfinder.dto.StopDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FlightOfferMapper {

    private final AirlineLookupService airlineLookupService;
    private final AirportSearchService airportSearchService;

    public FlightOfferMapper(
            AirlineLookupService airlineLookupService,
            AirportSearchService airportSearchService
    ) {
        this.airlineLookupService = airlineLookupService;
        this.airportSearchService = airportSearchService;
    }

    public List<FlightOfferDTO> mapFromJson(String amadeusResponseJson) throws Exception {
        List<FlightOfferDTO> offers = new ArrayList<>();
        JSONObject json = new JSONObject(amadeusResponseJson);
        JSONArray data = json.getJSONArray("data");



        for (int i = 0; i < data.length(); i++) {
            JSONObject offer = data.getJSONObject(i);
            JSONObject itinerary = offer.getJSONArray("itineraries").getJSONObject(0);
            JSONArray segments = itinerary.getJSONArray("segments");

            JSONObject firstSegment = segments.getJSONObject(0);
            JSONObject lastSegment = segments.getJSONObject(segments.length() - 1);

            FlightOfferDTO dto = new FlightOfferDTO();

            // Departure
            dto.setDepartureAirportCode(firstSegment.getJSONObject("departure").getString("iataCode"));
            dto.setDepartureDate(firstSegment.getJSONObject("departure").getString("at").split("T")[0]);
            dto.setDepartureTime(firstSegment.getJSONObject("departure").getString("at").split("T")[1]);

            // Arrival
            dto.setArrivalAirportCode(lastSegment.getJSONObject("arrival").getString("iataCode"));
            dto.setArrivalDate(lastSegment.getJSONObject("arrival").getString("at").split("T")[0]);
            dto.setArrivalTime(lastSegment.getJSONObject("arrival").getString("at").split("T")[1]);

            // Duration
            dto.setTotalFlightTime(itinerary.getString("duration"));

            // Airline codes
            String airlineCode = firstSegment.getString("carrierCode");
            String operatingCode = firstSegment.has("operating")
                    ? firstSegment.getJSONObject("operating").getString("carrierCode")
                    : airlineCode;

            dto.setAirlineCode(airlineCode);
            dto.setOperatingAirlineCode(operatingCode);

            // Airline names
            dto.setAirlineName(airlineLookupService.getAirlineName(airlineCode));
            dto.setOperatingAirlineName(airlineLookupService.getAirlineName(operatingCode));

            // Prices
            dto.setTotalPrice(offer.getJSONObject("price").getString("total"));
            dto.setPricePerAdult(offer.getJSONArray("travelerPricings")
                    .getJSONObject(0)
                    .getJSONObject("price")
                    .getString("total"));

            dto.setDepartureAirportName(airportSearchService.getAirportNameByCode(
                    firstSegment.getJSONObject("departure").getString("iataCode")
            ));

            dto.setArrivalAirportName(airportSearchService.getAirportNameByCode(
                    lastSegment.getJSONObject("arrival").getString("iataCode")
            ));


            // Stops
            dto.setHasStops(segments.length() > 1);
            List<StopDTO> stops = new ArrayList<>();
            for (int j = 1; j < segments.length(); j++) {
                JSONObject stop = segments.getJSONObject(j - 1).getJSONObject("arrival");
                StopDTO stopDTO = new StopDTO();
                stopDTO.setAirportCode(stop.getString("iataCode"));
                stopDTO.setArrivalTime(stop.getString("at").split("T")[1]);
                stopDTO.setDepartureTime(segments.getJSONObject(j).getJSONObject("departure").getString("at").split("T")[1]);
                stopDTO.setAirportName(airportSearchService.getAirportNameByCode(stop.getString("iataCode")));
                stops.add(stopDTO);
            }
            dto.setStops(stops);

            offers.add(dto);
        }

        return offers;
    }
}
