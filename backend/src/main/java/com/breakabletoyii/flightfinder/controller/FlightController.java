package com.breakabletoyii.flightfinder.controller;
import com.breakabletoyii.flightfinder.amadeus.FlightSearchService;
import com.breakabletoyii.flightfinder.dto.FlightOfferDTO;
import com.breakabletoyii.flightfinder.amadeus.FlightOfferMapper;
import com.breakabletoyii.flightfinder.model.Flight;

import com.breakabletoyii.flightfinder.controller.GlobalExceptionHandler;

import org.json.JSONObject;
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

    @Autowired
    private FlightOfferMapper flightOfferMapper;

    //lists of posible flights
    @GetMapping("/details/{id}")
    public ResponseEntity<FlightOfferDTO> getFlightDetails(@PathVariable String id) throws Exception {
        JSONObject flightJson = flightSearchService.getFlightById(id);
        FlightOfferDTO offer = flightOfferMapper.mapSingleOffer(flightJson);
        return ResponseEntity.ok(offer);
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightOfferDTO>> searchFlights(@RequestBody Flight request){
        List<FlightOfferDTO> offers = flightSearchService.searchFlights(request);
        return ResponseEntity.ok(offers);
    }

}
