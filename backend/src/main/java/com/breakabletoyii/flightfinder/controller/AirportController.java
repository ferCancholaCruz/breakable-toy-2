package com.breakabletoyii.flightfinder.controller;

import com.breakabletoyii.flightfinder.amadeus.AirportSearchService;
import com.breakabletoyii.flightfinder.dto.AirportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/airports")
public class AirportController {

    @Autowired
    private AirportSearchService airportSearchService;

    //exit is a list of airports in json format
    @GetMapping("/search")
    public ResponseEntity<List<AirportDTO>> searchAirports(@RequestParam String keyword) {
        List<AirportDTO> airports = airportSearchService.searchAirports(keyword);
        return ResponseEntity.ok(airports);
    }
}
