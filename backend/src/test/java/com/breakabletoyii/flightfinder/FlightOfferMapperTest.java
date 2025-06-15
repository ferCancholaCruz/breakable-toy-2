package com.breakabletoyii.flightfinder;

import com.breakabletoyii.flightfinder.amadeus.AirlineLookupService;
import com.breakabletoyii.flightfinder.amadeus.AirportSearchService;
import com.breakabletoyii.flightfinder.amadeus.FlightOfferMapper;
import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FlightOfferMapperTest {

    private AirlineLookupService airlineServiceMock;
    private AirportSearchService airportServiceMock;
    private FlightOfferMapper flightOfferMapper;

    @BeforeEach
    void setUp() {
        airlineServiceMock = mock(AirlineLookupService.class);
        airportServiceMock = mock(AirportSearchService.class);
        flightOfferMapper = new FlightOfferMapper(airlineServiceMock, airportServiceMock);
    }

    @Test
    void shouldMapFlightOfferCorrectly() throws Exception {
        String json = """
        {
          "itineraries": [
            {
              "duration": "PT2H30M",
              "segments": [
                {
                  "departure": { "iataCode": "MAD", "at": "2025-12-01T08:00" },
                  "arrival": { "iataCode": "BCN", "at": "2025-12-01T10:30" },
                  "carrierCode": "IB",
                  "number": "123",
                  "aircraft": { "code": "320" },
                  "operating": { "carrierCode": "IB" }
                }
              ]
            }
          ],
          "price": {
            "total": "200.00"
          },
          "travelerPricings": [
            {
              "price": { "total": "200.00" }
            }
          ]
        }
        """;

        JSONObject offer = new JSONObject(json);
        when(airlineServiceMock.getAirlineName("IB")).thenReturn("Iberia");
        when(airportServiceMock.getAirportNameByCode("MAD")).thenReturn("Madrid");
        when(airportServiceMock.getAirportNameByCode("BCN")).thenReturn("Barcelona");

        FlightOfferDTO dto = flightOfferMapper.mapSingleOffer(offer);

        assertEquals("MAD", dto.getDepartureAirportCode());
        assertEquals("2025-12-01", dto.getDepartureDate());
        assertEquals("08:00", dto.getDepartureTime());

        assertEquals("BCN", dto.getArrivalAirportCode());
        assertEquals("2025-12-01", dto.getArrivalDate());
        assertEquals("10:30", dto.getArrivalTime());

        assertEquals("PT2H30M", dto.getTotalFlightTime());

        assertEquals("IB", dto.getAirlineCode());
        assertEquals("IB", dto.getOperatingAirlineCode());
        assertEquals("Iberia", dto.getAirlineName());
        assertEquals("Iberia", dto.getOperatingAirlineName());

        assertEquals("200.00", dto.getTotalPrice());
        assertEquals("200.00", dto.getPricePerAdult());

        assertEquals("Madrid", dto.getDepartureAirportName());
        assertEquals("Barcelona", dto.getArrivalAirportName());
    }
}
