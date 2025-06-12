package com.breakabletoyii.flightfinder.dto;

import java.util.List;

public class ExtendedFlightOfferDTO {
    private FlightOfferDTO summary;
    private List<SegmentDetailsDTO> segments;
    private PriceDetailsDTO priceDetails;

    // Getters and setters
    public FlightOfferDTO getSummary() {
        return summary;
    }

    public void setSummary(FlightOfferDTO summary) {
        this.summary = summary;
    }

    public List<SegmentDetailsDTO> getSegments() {
        return segments;
    }

    public void setSegments(List<SegmentDetailsDTO> segments) {
        this.segments = segments;
    }

    public PriceDetailsDTO getPriceDetails() {
        return priceDetails;
    }

    public void setPriceDetails(PriceDetailsDTO priceDetails) {
        this.priceDetails = priceDetails;
    }
}

