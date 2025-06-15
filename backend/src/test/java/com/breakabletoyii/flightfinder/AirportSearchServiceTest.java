package com.breakabletoyii.flightfinder;

import com.breakabletoyii.flightfinder.amadeus.AirportSearchService;
import com.breakabletoyii.flightfinder.amadeus.AuthService;
import com.breakabletoyii.flightfinder.amadeus.HttpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AirportSearchServiceTest {

    private AuthService authServiceMock;
    private HttpService httpServiceMock;
    private AirportSearchService airportSearchService;

    @BeforeEach
    void setUp() {
        authServiceMock = mock(AuthService.class);
        httpServiceMock = mock(HttpService.class);
        airportSearchService = new AirportSearchService(authServiceMock, httpServiceMock);
    }

    @Test
    void shouldReturnAirportNameFromCache() throws Exception {

        Field cacheField = AirportSearchService.class.getDeclaredField("airportNameCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, String> cache = (ConcurrentHashMap<String, String>) cacheField.get(airportSearchService);
        cache.put("MAD", "Madrid Barajas Airport");

        String result = airportSearchService.getAirportNameByCode("MAD");

        assertEquals("Madrid Barajas Airport", result);
        verify(authServiceMock, never()).getAccessToken();
        verify(httpServiceMock, never()).sendGet(anyString(), anyString());
    }
}
