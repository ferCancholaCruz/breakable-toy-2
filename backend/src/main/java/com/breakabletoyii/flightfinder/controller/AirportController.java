package com.breakabletoyii.flightfinder.controller;

import com.breakabletoyii.flightfinder.amadeus.AirportSearchService;
import com.breakabletoyii.flightfinder.dto.AirportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/airports")
public class AirportController {

    @Autowired
    private AirportSearchService airportSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<AirportDTO>> searchAirports (@RequestParam String keyword){
        try {
            List<AirportDTO> airports = airportSearchService.searchAirports(keyword);
            return ResponseEntity.ok(airports);
            
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

}
