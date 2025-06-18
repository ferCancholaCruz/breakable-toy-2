package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.*;

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
        //parse the json
        List<FlightOfferDTO> offers = new ArrayList<>();
        JSONObject json = new JSONObject(amadeusResponseJson);
        JSONArray data = json.getJSONArray("data");

        //iterate through every flight
        for (int i = 0; i < data.length(); i++) {
            JSONObject offer = data.getJSONObject(i);
            System.out.println("======== RAW FLIGHT OFFER " + i + " ========");
            System.out.println(offer.toString(2)); // Pretty print JSON
            System.out.println("==========================================");

            JSONArray itineraries = offer.getJSONArray("itineraries");

            JSONArray firstItinerarySegments = itineraries.getJSONObject(0).getJSONArray("segments");

            JSONObject firstSegment = firstItinerarySegments.getJSONObject(0);
            JSONObject lastSegment = firstItinerarySegments.getJSONObject(firstItinerarySegments.length() - 1);

            JSONArray segments = new JSONArray();
            for (int k = 0; k < itineraries.length(); k++) {
                JSONArray segArray = itineraries.getJSONObject(k).getJSONArray("segments");
                for (int s = 0; s < segArray.length(); s++) {
                    segments.put(segArray.getJSONObject(s));
                }
            }


            //create new dto
            FlightOfferDTO dto = new FlightOfferDTO();
            dto.setId(offer.getString("id"));

            //establish the arrival and departure hours
            dto.setDepartureAirportCode(firstSegment.getJSONObject("departure").getString("iataCode"));
            dto.setDepartureDate(firstSegment.getJSONObject("departure").getString("at").split("T")[0]);
            dto.setDepartureTime(firstSegment.getJSONObject("departure").getString("at").split("T")[1]);

            //separate hours tht are in format: PT12H25M
            dto.setArrivalAirportCode(lastSegment.getJSONObject("arrival").getString("iataCode"));
            dto.setArrivalDate(lastSegment.getJSONObject("arrival").getString("at").split("T")[0]);
            dto.setArrivalTime(lastSegment.getJSONObject("arrival").getString("at").split("T")[1]);

            StringBuilder totalDuration = new StringBuilder();
            for (int d = 0; d < itineraries.length(); d++) {
                totalDuration.append(itineraries.getJSONObject(d).getString("duration")).append(" ");
            }
            dto.setTotalFlightTime(totalDuration.toString().trim());

            //check if the operating airline is different
            String airlineCode = firstSegment.has("carrierCode")
                    ? firstSegment.getString("carrierCode")
                    : "UNKNOWN";

            String operatingCode = firstSegment.has("operating") && firstSegment.getJSONObject("operating").has("carrierCode")
                    ? firstSegment.getJSONObject("operating").getString("carrierCode")
                    : airlineCode;

            dto.setAirlineCode(airlineCode);
            dto.setOperatingAirlineCode(operatingCode);

            //use the airlineLookupService to obtain the name of the airlines
            dto.setAirlineName(airlineLookupService.getAirlineName(airlineCode));
            dto.setOperatingAirlineName(airlineLookupService.getAirlineName(operatingCode));

            //in the offer object access the price subobject and the total as a string
            dto.setTotalPrice(offer.getJSONObject("price").getString("total"));
            dto.setPricePerAdult(offer.getJSONArray("travelerPricings")
                    .getJSONObject(0)
                    .getJSONObject("price")
                    .getString("total"));

            //get the airport name based on the iata code with the airportSearchService
            dto.setDepartureAirportName(airportSearchService.getAirportNameByCode(
                    firstSegment.getJSONObject("departure").getString("iataCode")
            ));

            dto.setArrivalAirportName(airportSearchService.getAirportNameByCode(
                    lastSegment.getJSONObject("arrival").getString("iataCode")
            ));

            //if theres more than 1 segment, there are stops
            dto.setHasStops(segments.length() > 1);

            //Create lists for the stops and segment details
            List<StopDTO> stops = new ArrayList<>();
            List<SegmentDetailsDTO> segmentDetailsList = new ArrayList<>();
            Map<String, JSONObject> fareDetailsBySegment = new HashMap<>();

            //iterate every traveler, and every price section
            JSONArray travelerPricings = offer.getJSONArray("travelerPricings");
            for (int t = 0; t < travelerPricings.length(); t++) {
                JSONArray fareDetails = travelerPricings.getJSONObject(t).getJSONArray("fareDetailsBySegment");
                for (int f = 0; f < fareDetails.length(); f++) {
                    JSONObject fd = fareDetails.getJSONObject(f);
                    String segId = fd.getString("segmentId");
                    fareDetailsBySegment.put(segId, fd);
                }
            }

            //foe each segment obtain relevant data such as flight, duration, arrival, departure
            for (int j = 0; j < segments.length(); j++) {
                JSONObject segment = segments.getJSONObject(j);
                SegmentDetailsDTO segmentDetails = new SegmentDetailsDTO();

                segmentDetails.setId(segment.optString("id", String.valueOf(j)));
                segmentDetails.setDepartureDateTime(segment.getJSONObject("departure").getString("at"));
                segmentDetails.setArrivalDateTime(segment.getJSONObject("arrival").getString("at"));
                segmentDetails.setCarrierCode(segment.optString("carrierCode", "UNKNOWN"));
                segmentDetails.setOperatingCarrierCode(segment.has("operating") ? segment.getJSONObject("operating").optString("carrierCode", null) : null);
                segmentDetails.setFlightNumber(segment.getString("number"));
                if (segment.has("aircraft") && segment.get("aircraft") instanceof JSONObject) {
                    segmentDetails.setAircraft(segment.getJSONObject("aircraft").optString("code", "N/A"));
                } else {
                    segmentDetails.setAircraft("N/A");
                }
                segmentDetails.setDuration(segment.optString("duration", null));
                //segmentDetails.setCabin(segment.optString("cabin", null));
                //segmentDetails.setBookingClass(segment.optString("bookingClass", null));
                //segmentDetails.setAmenities(new ArrayList<>());
                String segmentId = segment.optString("id", String.valueOf(j));
                segmentDetails.setId(segmentId); // ya estaba
                JSONObject fareDetail = fareDetailsBySegment.get(segmentId);

                //if theres info about fares, extract the info within
                if (fareDetail != null) {
                    segmentDetails.setCabin(fareDetail.optString("cabin", "N/A"));
                    segmentDetails.setBookingClass(fareDetail.optString("class", "N/A"));

                    List<AmenityDTO> amenities = new ArrayList<>();
                    JSONArray amenityArray = fareDetail.optJSONArray("amenities");
                    if (amenityArray != null) {
                        for (int a = 0; a < amenityArray.length(); a++) {
                            JSONObject amenity = amenityArray.getJSONObject(a);
                            AmenityDTO amenityDTO = new AmenityDTO();
                            amenityDTO.setName(amenity.optString("description", "Unknown"));
                            amenityDTO.setChargeable(amenity.optBoolean("isChargeable", false));
                            amenities.add(amenityDTO);
                        }
                    }
                    segmentDetails.setAmenities(amenities);
                } else {
                    segmentDetails.setCabin("N/A");
                    segmentDetails.setBookingClass("N/A");
                    segmentDetails.setAmenities(new ArrayList<>());
                }

                // airport info
                String depCode = segment.getJSONObject("departure").getString("iataCode");
                String arrCode = segment.getJSONObject("arrival").getString("iataCode");

                System.out.println("Looking up airport names: " + depCode + " -> " + arrCode);
                String depName = airportSearchService.getAirportNameByCode(depCode);
                String arrName = airportSearchService.getAirportNameByCode(arrCode);
                System.out.println("Resolved airport names: " + depName + " -> " + arrName);

                segmentDetails.setDepartureAirportCode(depCode);
                segmentDetails.setArrivalAirportCode(arrCode);
                segmentDetails.setDepartureAirportName(depName);
                segmentDetails.setArrivalAirportName(arrName);

                // airline name
                segmentDetails.setCarrierName(airlineLookupService.getAirlineName(segmentDetails.getCarrierCode()));
                if (segmentDetails.getOperatingCarrierCode() != null) {
                    segmentDetails.setOperatingCarrierName(airlineLookupService.getAirlineName(segmentDetails.getOperatingCarrierCode()));
                }

                segmentDetailsList.add(segmentDetails);
            }

            //get the info in every stop
            for (int j = 1; j < segments.length(); j++) {
                JSONObject stop = segments.getJSONObject(j - 1).getJSONObject("arrival");
                JSONObject nextDeparture = segments.getJSONObject(j).getJSONObject("departure");

                //generate and gather the required info
                StopDTO stopDTO = new StopDTO();
                stopDTO.setAirportCode(stop.getString("iataCode"));
                stopDTO.setAirportName(airportSearchService.getAirportNameByCode(stop.getString("iataCode")));

                //split the time and date of departure and arrival
                String[] arrivalParts = stop.getString("at").split("T");
                String[] departureParts = nextDeparture.getString("at").split("T");

                //assign the dates and time into the dto
                stopDTO.setArrivalDate(arrivalParts[0]);
                stopDTO.setArrivalTime(arrivalParts[1]);
                stopDTO.setDepartureDate(departureParts[0]);
                stopDTO.setDepartureTime(departureParts[1]);

                stops.add(stopDTO);
            }

            dto.setStops(stops);
            dto.setSegments(segmentDetailsList);
            JSONObject price = offer.getJSONObject("price");

            PriceDetailsDTO priceDetails = new PriceDetailsDTO();
            priceDetails.setBase(price.getString("base"));
            priceDetails.setTotal(price.getString("total"));
            priceDetails.setCurrency(price.getString("currency"));

            // fees, map them and send the into a dto
            List<FeeDTO> fees = new ArrayList<>();
            JSONArray feeArray = price.optJSONArray("fees");
            if (feeArray != null) {
                for (int f = 0; f < feeArray.length(); f++) {
                    JSONObject fee = feeArray.getJSONObject(f);
                    FeeDTO feeDTO = new FeeDTO();
                    feeDTO.setType(fee.getString("type"));
                    feeDTO.setAmount(fee.getString("amount"));
                    fees.add(feeDTO);
                }
            }
            priceDetails.setFees(fees);

            // traveler prices
            List<TravelerPriceDTO> travelerPrices = new ArrayList<>();
            JSONArray travelerArray = offer.getJSONArray("travelerPricings");
            for (int t = 0; t < travelerArray.length(); t++) {
                JSONObject traveler = travelerArray.getJSONObject(t);
                TravelerPriceDTO tp = new TravelerPriceDTO();
                tp.setTravelerType(traveler.getString("travelerType"));
                tp.setTotal(traveler.getJSONObject("price").getString("total"));
                travelerPrices.add(tp);
            }
            priceDetails.setTravelerPrices(travelerPrices);

            // Set to DTO
            dto.setPriceDetails(priceDetails);
            offers.add(dto);
        }

        return offers;
    }

    public FlightOfferDTO mapSingleOffer(JSONObject offer) throws Exception {
        List<FlightOfferDTO> offers = mapFromJson(new JSONObject().put("data", List.of(offer)).toString());
        return offers.get(0);
    }
}
