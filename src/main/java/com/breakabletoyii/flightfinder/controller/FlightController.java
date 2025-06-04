package com.breakabletoyii.flightfinder.controller;
import com.breakabletoyii.flightfinder.amadeus.FlightSearchService;
import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import com.breakabletoyii.flightfinder.model.Flight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private FlightSearchService flightSearchService;

    @PostMapping("/search")
    public ResponseEntity<List<FlightOfferDTO>> searchFlights(@RequestBody Flight request){
        try{
            List<FlightOfferDTO> offers = flightSearchService.searchFlights(request);
            return ResponseEntity.ok(offers);
        } catch (Exception e) {

            return ResponseEntity.status(500).body("Error at searching flights: " + e.getMessage());
    }

}
}
