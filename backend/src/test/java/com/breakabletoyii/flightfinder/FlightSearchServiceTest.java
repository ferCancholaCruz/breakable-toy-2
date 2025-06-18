package com.breakabletoyii.flightfinder;

import com.breakabletoyii.flightfinder.amadeus.AuthService;
import com.breakabletoyii.flightfinder.amadeus.ExtendedFlightOfferMapper;
import com.breakabletoyii.flightfinder.amadeus.FlightSearchService;
import com.breakabletoyii.flightfinder.amadeus.HttpService;
import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import com.breakabletoyii.flightfinder.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightSearchServiceTest {

    private AuthService authServiceMock;
    private ExtendedFlightOfferMapper mapperMock;
    private HttpService httpServiceMock;
    private FlightSearchService flightSearchService;

    @BeforeEach
    void setUp() {
        authServiceMock = mock(AuthService.class);
        mapperMock = mock(ExtendedFlightOfferMapper.class);
        httpServiceMock = mock(HttpService.class);
        flightSearchService = new FlightSearchService(mapperMock, authServiceMock, httpServiceMock);
    }

    @Test
    void shouldBuildRequestAndMapResponse() throws Exception {
        // Arrange
        when(authServiceMock.getAccessToken()).thenReturn("mock-token");

        String mockApiResponse = "{ \"data\": [] }";
        when(httpServiceMock.sendPost(anyString(), anyString(), anyString()))
                .thenReturn(mockApiResponse);

        FlightOfferDTO mockOffer = new FlightOfferDTO();
        when(mapperMock.mapFromJson(mockApiResponse))
                .thenReturn(List.of(mockOffer));

        Flight flight = new Flight();
        flight.setDepartureCode("MAD");
        flight.setArrivalCode("BCN");
        flight.setDepartureDate(LocalDate.now().toString());
        flight.setNumberAdults(1);
        flight.setCurrency("USD");
        flight.setNonStop(true);

        // Act
        List<FlightOfferDTO> result = flightSearchService.searchFlights(flight);

        // Assert
        assertEquals(1, result.size());
        assertSame(mockOffer, result.get(0));
        verify(authServiceMock).getAccessToken();
        verify(httpServiceMock).sendPost(anyString(), anyString(), anyString());
        verify(mapperMock).mapFromJson(mockApiResponse);
    }
}
